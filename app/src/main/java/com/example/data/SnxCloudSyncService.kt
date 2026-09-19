package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.util.SynchronizedAviatorEngine
import com.example.model.PaymentMethod
import com.example.model.TransactionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * High-Availability Real-Time Internet Cloud Synchronization Service.
 *
 * Connects the 3 separate apps:
 * 1) SNX 777 Gaming Website App (com.aistudio.snx777.game)
 * 2) SNX Admin Panel App (com.example.admin.panel)
 * 3) SNX Signal App (com.aistudio.snx777.signal)
 *
 * Enables instant two-way real-time data exchange across different devices
 * anywhere with an active internet connection (WiFi or Mobile Data):
 * - Real-time Deposit approvals & instant balance crediting
 * - Real-time Withdrawal approvals & status updates
 * - Instant Payment Numbers sync (bKash & Nagad Agent numbers)
 * - User account registrations and status changes
 * - Shared millisecond-accurate Aviator crash round numbers and multipliers for Signal App
 */
object SnxCloudSyncService {

    private const val TAG = "SnxCloudSyncService"

    // Supabase Cloud Configuration
    const val SUPABASE_REST_URL = "https://mbzflwqkvzocrkpmepxf.supabase.co/rest/v1/"
    const val SUPABASE_ANON_KEY = "sb_publishable_A4LIP6Tk1jzlfsWvT7BjzQ_G2NiYbd4"

    // Default High-Availability Cloud Bridge Channel
    private const val DEFAULT_OBJECT_ID = "ff808181a09d98f701a0a9a7b167194a"
    private const val BASE_CLOUD_URL = "https://api.restful-api.dev/objects/"

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val syncMutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)

    // Observable status flows for UI
    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow("Cloud Sync: Active")
    val syncStatusMessage: StateFlow<String> = _syncStatusMessage.asStateFlow()

    private val _cloudActiveRoundNumber = MutableStateFlow(0L)
    val cloudActiveRoundNumber: StateFlow<Long> = _cloudActiveRoundNumber.asStateFlow()

    private val _cloudCrashMultiplier = MutableStateFlow(1.00)
    val cloudCrashMultiplier: StateFlow<Double> = _cloudCrashMultiplier.asStateFlow()

    private var isLoopStarted = false

    fun getCloudEndpoint(context: Context): String {
        val prefs = SharedDataStore.getAdminPrefs(context)
        val customUrl = prefs.getString("key_custom_cloud_endpoint", "")?.trim()
        if (!customUrl.isNullOrEmpty() && customUrl.startsWith("http")) {
            return customUrl
        }
        val customId = prefs.getString("key_cloud_channel_id", DEFAULT_OBJECT_ID)?.trim()
        val objectId = if (!customId.isNullOrEmpty()) customId else DEFAULT_OBJECT_ID
        return "$BASE_CLOUD_URL$objectId"
    }

    fun isInternetAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = cm.activeNetwork ?: return false
            val cap = cm.getNetworkCapabilities(network) ?: return false
            cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Starts continuous cloud synchronization loop in the background.
     */
    fun startAutoSync(context: Context, intervalMs: Long = 1200L) {
        if (isLoopStarted) return
        isLoopStarted = true
        scope.launch {
            // Initial sync immediately
            pullFromCloud(context)
            while (true) {
                kotlinx.coroutines.delay(intervalMs)
                try {
                    pullFromCloud(context)
                } catch (e: Exception) {
                    Log.w(TAG, "Auto sync pull error: ${e.message}")
                }
            }
        }
    }

    private fun fetchExistingCloudData(endpoint: String): JSONObject? {
        for (attempt in 1..3) {
            try {
                val cacheBustUrl = if (endpoint.contains("?")) "$endpoint&_cb=${System.currentTimeMillis()}" else "$endpoint?_cb=${System.currentTimeMillis()}"
                val getReq = Request.Builder()
                    .url(cacheBustUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                    .header("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
                    .header("Pragma", "no-cache")
                    .get()
                    .build()
                httpClient.newCall(getReq).execute().use { res ->
                    if (res.isSuccessful) {
                        val bStr = res.body?.string() ?: ""
                        if (bStr.isNotBlank()) {
                            val j = JSONObject(bStr)
                            val d = j.optJSONObject("data")
                            if (d != null) return d
                        }
                    }
                }
            } catch (_: Exception) {}
            try { Thread.sleep(100) } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Pushes all local state (deposits, withdrawals, numbers, users, game status) to the Cloud Bridge.
     */
    fun pushToCloud(context: Context) {
        scope.launch {
            syncMutex.withLock {
                if (!isInternetAvailable(context)) {
                    _isCloudConnected.value = false
                    _syncStatusMessage.value = "Offline: Waiting for Internet"
                    return@withLock
                }

                // CRITICAL SAFETY GUARD:
                // Regular user devices MUST NOT pushToCloud to overwrite global admin settings or payment numbers!
                // Regular users only sync deposits, withdrawals, or account profile via their dedicated endpoints.
                val isAdmin = AdminManager.getInstance(context).isAdminLoggedIn.value ||
                        context.packageName.contains("admin")
                if (!isAdmin) {
                    return@withLock
                }

                try {
                    val endpoint = getCloudEndpoint(context)
                    val localDataJson = SharedDataStore.exportAllDataJson(context)
                    val localObj = JSONObject(localDataJson)

                    var existingCloudData = fetchExistingCloudData(endpoint)
                    if (existingCloudData == null) {
                        existingCloudData = JSONObject()
                    }

                    // Include live Aviator game snapshot for Signal App synchronization
                    val aviatorSnapshot = SynchronizedAviatorEngine.getCurrentGlobalSnapshot()
                    val signalData = JSONObject().apply {
                        put("roundNumber", aviatorSnapshot.roundNumber)
                        put("phase", aviatorSnapshot.phase.name)
                        put("currentMultiplier", aviatorSnapshot.currentMultiplier)
                        put("crashMultiplier", aviatorSnapshot.crashMultiplier)
                        put("waitingSecondsRemaining", aviatorSnapshot.waitingSecondsRemaining)
                        put("serverTimestamp", System.currentTimeMillis())
                    }

                    // Payment Numbers: ONLY Admin can publish payment numbers.
                    val localVal = localObj.optString("payment_numbers", "")
                    val paymentNumbersVal = if (localVal.isNotBlank() && localVal != "[]") {
                        localVal
                    } else {
                        existingCloudData.optString("payment_numbers", "")
                    }

                    // Deposit Requests: Always merge cloud and local safely
                    val cloudDep = existingCloudData.optString("deposit_requests", "")
                    val localDep = localObj.optString("deposit_requests", "")
                    val mergedDeposits = SharedDataStore.mergeJsonListsById(cloudDep, localDep)

                    // Withdraw Requests: Always merge cloud and local safely
                    val cloudWd = existingCloudData.optString("withdraw_requests", "")
                    val localWd = localObj.optString("withdraw_requests", "")
                    val mergedWithdrawals = SharedDataStore.mergeJsonListsById(cloudWd, localWd)

                    // Registered Users: Merge users safely
                    val cloudUsers = existingCloudData.optString("registered_users", "")
                    val localUsers = localObj.optString("registered_users", "")
                    val mergedUsers = SharedDataStore.mergeUsersByPhone(cloudUsers, localUsers, isAdmin)

                    // Site Config & Games: Admin is authority
                    val localSite = localObj.optString("site_config", "")
                    val siteConfigVal = if (localSite.isNotBlank() && localSite != "{}") localSite else existingCloudData.optString("site_config", "")

                    val localGames = localObj.optString("games_list", "")
                    val gamesListVal = if (localGames.isNotBlank() && localGames != "[]") localGames else existingCloudData.optString("games_list", "")

                    val innerData = JSONObject().apply {
                        put("version", 2)
                        put("last_updated_time", System.currentTimeMillis())
                        put("last_updated_by", "admin")
                        put("payment_numbers", paymentNumbersVal)
                        put("deposit_requests", mergedDeposits)
                        put("withdraw_requests", mergedWithdrawals)
                        put("registered_users", mergedUsers)
                        put("site_config", siteConfigVal)
                        put("games_list", gamesListVal)
                        put("signal_data", signalData.toString())
                        put("payload", JSONObject().apply {
                            put("payment_numbers", paymentNumbersVal)
                            put("deposit_requests", mergedDeposits)
                            put("withdraw_requests", mergedWithdrawals)
                            put("registered_users", mergedUsers)
                            put("site_config", siteConfigVal)
                            put("games_list", gamesListVal)
                        }.toString())
                    }

                    val cloudDataWrapper = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", innerData)
                    }

                    val requestBody = cloudDataWrapper.toString().toRequestBody(JSON_MEDIA_TYPE)
                    val request = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        .put(requestBody)
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            _isCloudConnected.value = true
                            _lastSyncTimestamp.value = System.currentTimeMillis()
                            _syncStatusMessage.value = "Cloud Sync: Connected"
                            Log.d(TAG, "pushToCloud: Success (${response.code})")
                        } else {
                            Log.w(TAG, "pushToCloud: Response error ${response.code}")
                        }
                    }

                    // Attempt secondary sync to Supabase table if created/available
                    try {
                        val supabaseUrl = "${SUPABASE_REST_URL}snx_store"
                        val sbPayload = JSONObject().apply {
                            put("key", "global_state")
                            put("value", innerData.toString())
                            put("updated_at", System.currentTimeMillis())
                        }.toString().toRequestBody(JSON_MEDIA_TYPE)

                        val sbReq = Request.Builder()
                            .url(supabaseUrl)
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                            .header("Prefer", "resolution=merge-duplicates")
                            .post(sbPayload)
                            .build()

                        httpClient.newCall(sbReq).execute().close()
                    } catch (_: Exception) {}
                } catch (e: Exception) {
                    _isCloudConnected.value = false
                    _syncStatusMessage.value = "Cloud Sync Error"
                    Log.w(TAG, "pushToCloud failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Pulls latest cloud state and merges it with local storage.
     */
    fun pullFromCloud(context: Context) {
        scope.launch {
            if (!isInternetAvailable(context)) {
                _isCloudConnected.value = false
                _syncStatusMessage.value = "Offline: Waiting for Internet"
                return@launch
            }

            try {
                var cloudData: JSONObject? = null

                // 1. Try pulling from Supabase first if snx_store table is present
                try {
                    val sbReq = Request.Builder()
                        .url("${SUPABASE_REST_URL}snx_store?key=eq.global_state")
                        .header("apikey", SUPABASE_ANON_KEY)
                        .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        .get()
                        .build()
                    httpClient.newCall(sbReq).execute().use { sbRes ->
                        if (sbRes.isSuccessful) {
                            val sbBody = sbRes.body?.string() ?: ""
                            if (sbBody.isNotBlank() && sbBody != "[]") {
                                val sbArr = JSONArray(sbBody)
                                if (sbArr.length() > 0) {
                                    val row = sbArr.getJSONObject(0)
                                    val valStr = row.optString("value", "")
                                    if (valStr.isNotBlank()) {
                                        cloudData = JSONObject(valStr)
                                    }
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}

                // 2. If Supabase global_state was not found or not created yet, pull from high-availability Cloud Bridge
                if (cloudData == null) {
                    val endpoint = getCloudEndpoint(context)
                    cloudData = fetchExistingCloudData(endpoint)
                }

                val data = cloudData ?: return@launch

                // Helper to extract string or JSONArray from direct key or nested payload
                val payloadObj = if (data.has("payload")) {
                    try {
                        val p = data.opt("payload")
                        when (p) {
                            is JSONObject -> p
                            is String -> if (p.trim().startsWith("{")) JSONObject(p) else null
                            else -> null
                        }
                    } catch (_: Exception) { null }
                } else null

                fun extractField(key: String): String {
                    val direct = data.opt(key)
                    if (direct != null) {
                        if (direct is JSONArray) return direct.toString()
                        val s = direct.toString()
                        if (s.isNotBlank() && s != "[]" && s != "{}") return s
                    }
                    if (payloadObj != null) {
                        val nested = payloadObj.opt(key)
                        if (nested != null) {
                            if (nested is JSONArray) return nested.toString()
                            val s = nested.toString()
                            if (s.isNotBlank() && s != "[]" && s != "{}") return s
                        }
                    }
                    return if (direct != null) direct.toString() else ""
                }

                val paymentNumbersVal = extractField("payment_numbers")
                val depositRequestsVal = extractField("deposit_requests")
                val withdrawRequestsVal = extractField("withdraw_requests")
                val registeredUsersVal = extractField("registered_users")
                val siteConfigVal = extractField("site_config")
                val gamesListVal = extractField("games_list")

                // Format into payload compatible with SharedDataStore.importAllDataJson
                val formattedPayload = JSONObject().apply {
                    put("payment_numbers", paymentNumbersVal)
                    put("deposit_requests", depositRequestsVal)
                    put("withdraw_requests", withdrawRequestsVal)
                    put("registered_users", registeredUsersVal)
                    put("site_config", siteConfigVal)
                    put("games_list", gamesListVal)
                    put("timestamp", data.optLong("last_updated_time", System.currentTimeMillis()))
                }

                // Check for Signal App live game sync
                if (data.has("signal_data")) {
                    val signalStr = data.optString("signal_data", "")
                    if (signalStr.isNotEmpty()) {
                        try {
                            val sObj = JSONObject(signalStr)
                            val round = sObj.optLong("roundNumber", 0L)
                            val mult = sObj.optDouble("crashMultiplier", 1.00)
                            if (round > 0) {
                                _cloudActiveRoundNumber.value = round
                                _cloudCrashMultiplier.value = mult
                            }
                        } catch (_: Exception) {}
                    }
                }

                // Direct instant update for Payment Numbers if present
                if (paymentNumbersVal.isNotBlank() && paymentNumbersVal != "[]") {
                    AdminManager.getInstance(context).applyPaymentNumbersFromCloud(paymentNumbersVal)
                }

                val updated = SharedDataStore.importAllDataJson(context, formattedPayload.toString())
                if (updated || paymentNumbersVal.isNotBlank()) {
                    _isCloudConnected.value = true
                    _lastSyncTimestamp.value = System.currentTimeMillis()
                    _syncStatusMessage.value = "Cloud Sync: Connected"
                }
            } catch (e: Exception) {
                _isCloudConnected.value = false
                Log.w(TAG, "pullFromCloud error: ${e.message}")
            }
        }
    }

    /**
     * Force immediate two-way synchronization now.
     */
    fun syncNow(context: Context) {
        scope.launch {
            pullFromCloud(context)
            kotlinx.coroutines.delay(200)
            pushToCloud(context)
        }
    }

    /**
     * Instantly pushes payment numbers update to the cloud from Admin Panel.
     */
    fun pushPaymentNumbersToCloud(context: Context, paymentNumbersJson: String) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    var existingData = fetchExistingCloudData(endpoint)
                    if (existingData == null) {
                        existingData = JSONObject()
                    }

                    existingData.put("version", 2)
                    existingData.put("payment_numbers", paymentNumbersJson)
                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "admin")

                    // Also embed into payload so standalone admin app also reads it!
                    val fullPayload = JSONObject().apply {
                        put("payment_numbers", paymentNumbersJson)
                        put("deposit_requests", existingData.optString("deposit_requests", ""))
                        put("withdraw_requests", existingData.optString("withdraw_requests", ""))
                        put("registered_users", existingData.optString("registered_users", ""))
                        put("site_config", existingData.optString("site_config", ""))
                        put("games_list", existingData.optString("games_list", ""))
                    }
                    existingData.put("payload", fullPayload.toString())

                    val wrapper = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }

                    val putReq = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        .put(wrapper.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    httpClient.newCall(putReq).execute().use { res ->
                        if (res.isSuccessful) {
                            _isCloudConnected.value = true
                            _lastSyncTimestamp.value = System.currentTimeMillis()
                            _syncStatusMessage.value = "Payment numbers published to Cloud"
                            Log.d(TAG, "pushPaymentNumbersToCloud success: ${res.code}")
                        }
                    }

                    // Also upsert to Supabase snx_store table
                    try {
                        val sbPayload = JSONObject().apply {
                            put("key", "global_state")
                            put("value", existingData.toString())
                            put("updated_at", System.currentTimeMillis())
                        }.toString().toRequestBody(JSON_MEDIA_TYPE)

                        val sbReq = Request.Builder()
                            .url("${SUPABASE_REST_URL}snx_store")
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                            .header("Prefer", "resolution=merge-duplicates")
                            .post(sbPayload)
                            .build()

                        httpClient.newCall(sbReq).execute().close()
                    } catch (_: Exception) {}

                    // Also upsert directly to Supabase payment_numbers table if available
                    try {
                        val array = JSONArray(paymentNumbersJson)
                        val sbArray = JSONArray()
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            sbArray.put(JSONObject().apply {
                                put("id", item.getString("id"))
                                put("method", item.getString("method"))
                                put("number", item.getString("number"))
                                put("agent_label", item.optString("agentLabel", item.optString("agent_label", "")))
                                put("is_active", item.optBoolean("isActive", item.optBoolean("is_active", true)))
                                put("created_at", item.optString("createdAt", item.optString("created_at", "")))
                            })
                        }
                        val sbNumsReq = Request.Builder()
                            .url("${SUPABASE_REST_URL}payment_numbers")
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                            .header("Prefer", "resolution=merge-duplicates")
                            .post(sbArray.toString().toRequestBody(JSON_MEDIA_TYPE))
                            .build()

                        httpClient.newCall(sbNumsReq).execute().close()
                    } catch (_: Exception) {}

                } catch (e: Exception) {
                    Log.e(TAG, "pushPaymentNumbersToCloud error: ${e.message}")
                }
            }
        }
    }

    /**
     * Instantly pushes user deposit request directly to cloud so Admin can view it in real time.
     */
    fun pushDepositRequestDirect(context: Context, req: DepositRequest) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    var existingData = fetchExistingCloudData(endpoint)
                    if (existingData == null) {
                        existingData = JSONObject()
                    }

                    val currentCloudDeposits = existingData.optString("deposit_requests", "")
                    val reqObj = JSONObject().apply {
                        put("id", req.id)
                        put("username", req.username)
                        put("userPhone", req.userPhone)
                        put("method", req.method.name)
                        put("amount", req.amount)
                        put("agentNumberUsed", req.agentNumberUsed)
                        put("userAccountNo", req.userAccountNo)
                        put("trxId", req.trxId)
                        put("status", req.status.name)
                        put("createdAt", req.createdAt)
                        put("reviewNote", req.reviewNote)
                    }
                    val newArray = JSONArray().put(reqObj).toString()
                    val mergedDeposits = SharedDataStore.mergeJsonListsById(currentCloudDeposits, newArray)

                    existingData.put("deposit_requests", mergedDeposits)
                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "user_${req.username}")

                    // Preserve payment numbers from payload if missing at top level
                    if (!existingData.has("payment_numbers") || existingData.optString("payment_numbers").isBlank() || existingData.optString("payment_numbers") == "[]") {
                        try {
                            val p = existingData.opt("payload")
                            val pObj = when (p) {
                                is JSONObject -> p
                                is String -> if (p.startsWith("{")) JSONObject(p) else null
                                else -> null
                            }
                            val pNums = pObj?.optString("payment_numbers", "") ?: ""
                            if (pNums.isNotBlank() && pNums != "[]") {
                                existingData.put("payment_numbers", pNums)
                            }
                        } catch (_: Exception) {}
                    }

                    val wrapper = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }

                    val putReq = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        .put(wrapper.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    httpClient.newCall(putReq).execute().use { res ->
                        if (res.isSuccessful) {
                            _isCloudConnected.value = true
                            _lastSyncTimestamp.value = System.currentTimeMillis()
                            _syncStatusMessage.value = "Deposit request sent to Admin"
                            Log.d(TAG, "pushDepositRequestDirect success: ${res.code}")
                        }
                    }

                    // Also push to Supabase snx_store table
                    try {
                        val sbPayload = JSONObject().apply {
                            put("key", "global_state")
                            put("value", existingData.toString())
                            put("updated_at", System.currentTimeMillis())
                        }.toString().toRequestBody(JSON_MEDIA_TYPE)

                        val sbReq = Request.Builder()
                            .url("${SUPABASE_REST_URL}snx_store")
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                            .header("Prefer", "resolution=merge-duplicates")
                            .post(sbPayload)
                            .build()

                        httpClient.newCall(sbReq).execute().close()
                    } catch (_: Exception) {}

                } catch (e: Exception) {
                    Log.e(TAG, "pushDepositRequestDirect error: ${e.message}")
                }
            }
        }
    }

    /**
     * Updates deposit status on the cloud (e.g. APPROVED or REJECTED) so user sees status & receives balance.
     */
    fun pushDepositStatusUpdate(
        context: Context,
        requestId: String,
        newStatus: TransactionStatus,
        reviewNote: String = ""
    ) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    var existingData = fetchExistingCloudData(endpoint)
                    if (existingData == null) {
                        existingData = JSONObject()
                    }

                    val currentCloudDeposits = existingData.optString("deposit_requests", "")
                    if (currentCloudDeposits.isNotBlank()) {
                        val arr = JSONArray(currentCloudDeposits)
                        val updatedArr = JSONArray()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            if (obj.optString("id") == requestId) {
                                obj.put("status", newStatus.name)
                                if (reviewNote.isNotBlank()) {
                                    obj.put("reviewNote", reviewNote)
                                }
                            }
                            updatedArr.put(obj)
                        }
                        existingData.put("deposit_requests", updatedArr.toString())
                    }

                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "admin")

                    val wrapper = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }

                    val putReq = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        .put(wrapper.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    httpClient.newCall(putReq).execute().use { res ->
                        Log.d(TAG, "pushDepositStatusUpdate success: ${res.code}")
                    }

                    // Also push update to Supabase snx_store table
                    try {
                        val sbPayload = JSONObject().apply {
                            put("key", "global_state")
                            put("value", existingData.toString())
                            put("updated_at", System.currentTimeMillis())
                        }.toString().toRequestBody(JSON_MEDIA_TYPE)

                        val sbReq = Request.Builder()
                            .url("${SUPABASE_REST_URL}snx_store")
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                            .header("Prefer", "resolution=merge-duplicates")
                            .post(sbPayload)
                            .build()

                        httpClient.newCall(sbReq).execute().close()
                    } catch (_: Exception) {}

                } catch (e: Exception) {
                    Log.e(TAG, "pushDepositStatusUpdate error: ${e.message}")
                }
            }
        }
    }

    /**
     * Instantly pushes user withdrawal request directly to cloud.
     */
    fun pushWithdrawRequestDirect(context: Context, req: WithdrawalRequest) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    var existingData = fetchExistingCloudData(endpoint)
                    if (existingData == null) {
                        existingData = JSONObject()
                    }

                    val currentCloudWd = existingData.optString("withdraw_requests", "")
                    val reqObj = JSONObject().apply {
                        put("id", req.id)
                        put("username", req.username)
                        put("userPhone", req.userPhone)
                        put("method", req.method.name)
                        put("amount", req.amount)
                        put("targetAccountNo", req.targetAccountNo)
                        put("trxId", req.trxId)
                        put("status", req.status.name)
                        put("createdAt", req.createdAt)
                        put("reviewNote", req.reviewNote)
                    }
                    val newArray = JSONArray().put(reqObj).toString()
                    val mergedWd = SharedDataStore.mergeJsonListsById(currentCloudWd, newArray)

                    existingData.put("withdraw_requests", mergedWd)
                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "user_${req.username}")

                    val wrapper = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }

                    val putReq = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        .put(wrapper.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    httpClient.newCall(putReq).execute().use { res ->
                        Log.d(TAG, "pushWithdrawRequestDirect success: ${res.code}")
                    }

                    // Also push to Supabase snx_store table
                    try {
                        val sbPayload = JSONObject().apply {
                            put("key", "global_state")
                            put("value", existingData.toString())
                            put("updated_at", System.currentTimeMillis())
                        }.toString().toRequestBody(JSON_MEDIA_TYPE)

                        val sbReq = Request.Builder()
                            .url("${SUPABASE_REST_URL}snx_store")
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                            .header("Prefer", "resolution=merge-duplicates")
                            .post(sbPayload)
                            .build()

                        httpClient.newCall(sbReq).execute().close()
                    } catch (_: Exception) {}

                } catch (e: Exception) {
                    Log.e(TAG, "pushWithdrawRequestDirect error: ${e.message}")
                }
            }
        }
    }

    /**
     * Updates withdrawal status on the cloud (e.g. APPROVED or REJECTED).
     */
    fun pushWithdrawStatusUpdate(
        context: Context,
        requestId: String,
        newStatus: TransactionStatus,
        reviewNote: String = ""
    ) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    var existingData = fetchExistingCloudData(endpoint)
                    if (existingData == null) {
                        existingData = JSONObject()
                    }

                    val currentCloudWd = existingData.optString("withdraw_requests", "")
                    if (currentCloudWd.isNotBlank()) {
                        val arr = JSONArray(currentCloudWd)
                        val updatedArr = JSONArray()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            if (obj.optString("id") == requestId) {
                                obj.put("status", newStatus.name)
                                if (reviewNote.isNotBlank()) {
                                    obj.put("reviewNote", reviewNote)
                                }
                            }
                            updatedArr.put(obj)
                        }
                        existingData.put("withdraw_requests", updatedArr.toString())
                    }

                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "admin")

                    val wrapper = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }

                    val putReq = Request.Builder()
                        .url(endpoint)
                        .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        .put(wrapper.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    httpClient.newCall(putReq).execute().use { res ->
                        Log.d(TAG, "pushWithdrawStatusUpdate success: ${res.code}")
                    }

                    // Also push update to Supabase snx_store table
                    try {
                        val sbPayload = JSONObject().apply {
                            put("key", "global_state")
                            put("value", existingData.toString())
                            put("updated_at", System.currentTimeMillis())
                        }.toString().toRequestBody(JSON_MEDIA_TYPE)

                        val sbReq = Request.Builder()
                            .url("${SUPABASE_REST_URL}snx_store")
                            .header("apikey", SUPABASE_ANON_KEY)
                            .header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                            .header("Prefer", "resolution=merge-duplicates")
                            .post(sbPayload)
                            .build()

                        httpClient.newCall(sbReq).execute().close()
                    } catch (_: Exception) {}

                } catch (e: Exception) {
                    Log.e(TAG, "pushWithdrawStatusUpdate error: ${e.message}")
                }
            }
        }
    }
}
