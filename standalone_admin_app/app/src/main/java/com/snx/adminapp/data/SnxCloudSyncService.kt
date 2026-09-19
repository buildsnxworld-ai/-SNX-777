package com.snx.adminapp.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * SnxCloudSyncService (Standalone Admin Panel)
 * Connects the Admin Panel App with the Website App and Signal App
 * via real-time internet cloud synchronization (Supabase + High-Availability Cloud Bridge).
 */
object SnxCloudSyncService {

    private const val TAG = "SnxCloudSyncService"

    // Supabase Cloud Configuration
    const val SUPABASE_REST_URL = "https://mbzflwqkvzocrkpmepxf.supabase.co/rest/v1/"
    const val SUPABASE_ANON_KEY = "sb_publishable_A4LIP6Tk1jzlfsWvT7BjzQ_G2NiYbd4"

    // Default High-Availability Cloud Bridge Channel
    private const val DEFAULT_OBJECT_ID = "ff808181a09d98f701a0a9a7b167194a"
    private const val BASE_CLOUD_URL = "https://api.restful-api.dev/objects/"

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val syncMutex = Mutex()

    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _syncStatusMessage = MutableStateFlow("Cloud Sync: Active")
    val syncStatusMessage: StateFlow<String> = _syncStatusMessage.asStateFlow()

    private val _cloudActiveRoundNumber = MutableStateFlow(0L)
    val cloudActiveRoundNumber: StateFlow<Long> = _cloudActiveRoundNumber.asStateFlow()

    private var autoSyncJob: Job? = null

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

    fun startAutoSync(context: Context, intervalMs: Long = 1200L) {
        if (autoSyncJob?.isActive == true) return
        autoSyncJob = scope.launch {
            pullFromCloud(context)
            while (isActive) {
                delay(intervalMs)
                try {
                    pullFromCloud(context)
                } catch (e: Exception) {
                    Log.w(TAG, "Auto sync iteration error: ${e.message}")
                }
            }
        }
    }

    fun stopAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
    }

    fun syncNow(context: Context) {
        scope.launch {
            pullFromCloud(context)
            delay(150)
            pushToCloud(context)
        }
    }

    private fun fetchExistingCloudData(endpoint: String): JSONObject? {
        for (attempt in 1..3) {
            try {
                val cacheBustUrl = if (endpoint.contains("?")) "$endpoint&_cb=${System.currentTimeMillis()}" else "$endpoint?_cb=${System.currentTimeMillis()}"
                val url = URL(cacheBustUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                    setRequestProperty("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate")
                    setRequestProperty("Pragma", "no-cache")
                    connectTimeout = 4000
                    readTimeout = 4000
                }
                if (conn.responseCode in 200..299) {
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use {
                        it.readText()
                    }
                    conn.disconnect()
                    val rootObj = JSONObject(responseText)
                    val d = rootObj.optJSONObject("data")
                    if (d != null) return d
                } else {
                    conn.disconnect()
                }
            } catch (_: Exception) {}
            try { Thread.sleep(100) } catch (_: Exception) {}
        }
        return null
    }

    fun pushPaymentNumbersToCloud(context: Context, paymentNumbersJson: String) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    val existingData = fetchExistingCloudData(endpoint) ?: JSONObject()
                    val roundNow = System.currentTimeMillis() / 25000L

                    existingData.put("version", 2)
                    existingData.put("snx_version", System.currentTimeMillis())
                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "admin")
                    existingData.put("active_round", roundNow)
                    existingData.put("payment_numbers", paymentNumbersJson)

                    // Also embed inside payload for backwards compatibility
                    val fullLocalJson = SharedDataStore.exportAllDataJson(context)
                    val localObj = try { JSONObject(fullLocalJson) } catch (_: Exception) { JSONObject() }
                    localObj.put("payment_numbers", paymentNumbersJson)
                    existingData.put("payload", localObj.toString())

                    val requestBody = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }.toString()

                    val url = URL(endpoint)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "PUT"
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        setRequestProperty("Accept", "application/json")
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        connectTimeout = 5000
                        readTimeout = 5000
                        doOutput = true
                    }

                    OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                        os.write(requestBody)
                        os.flush()
                    }

                    val responseCode = conn.responseCode
                    conn.disconnect()
                    if (responseCode in 200..299) {
                        _isCloudConnected.value = true
                        _lastSyncTimestamp.value = System.currentTimeMillis()
                        _syncStatusMessage.value = "Payment Numbers Published"
                        Log.d(TAG, "Successfully published payment numbers to Cloud Bridge ($responseCode)")
                    }

                    // Also sync to Supabase table
                    syncToSupabaseStore(existingData)
                } catch (e: Exception) {
                    Log.e(TAG, "pushPaymentNumbersToCloud error: ${e.message}")
                }
            }
        }
    }

    fun pushDepositStatusUpdate(
        context: Context,
        depositId: String,
        newStatus: String,
        reviewNote: String = ""
    ) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    val existingData = fetchExistingCloudData(endpoint) ?: JSONObject()
                    val rawDeposits = existingData.optString("deposit_requests", "[]")
                    val arr = try { JSONArray(rawDeposits) } catch (_: Exception) { JSONArray() }
                    var found = false

                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        if (item.optString("id") == depositId) {
                            item.put("status", newStatus)
                            if (reviewNote.isNotBlank()) item.put("reviewNote", reviewNote)
                            found = true
                            break
                        }
                    }

                    if (!found) {
                        val localDeposits = AdminManager.getInstance(context).depositRequests.value
                        val matched = localDeposits.firstOrNull { it.id == depositId }
                        if (matched != null) {
                            val newItem = JSONObject().apply {
                                put("id", matched.id)
                                put("username", matched.username)
                                put("userPhone", matched.userPhone)
                                put("method", matched.method.name)
                                put("amount", matched.amount)
                                put("agentNumberUsed", matched.agentNumberUsed)
                                put("userAccountNo", matched.userAccountNo)
                                put("trxId", matched.trxId)
                                put("status", newStatus)
                                put("createdAt", matched.createdAt)
                                put("reviewNote", reviewNote.ifBlank { matched.reviewNote })
                            }
                            arr.put(newItem)
                        }
                    }

                    existingData.put("deposit_requests", arr.toString())
                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "admin")

                    val fullLocal = SharedDataStore.exportAllDataJson(context)
                    existingData.put("payload", fullLocal)

                    val requestBody = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }.toString()

                    val url = URL(endpoint)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "PUT"
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        setRequestProperty("Accept", "application/json")
                        connectTimeout = 5000
                        readTimeout = 5000
                        doOutput = true
                    }
                    OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                        os.write(requestBody)
                        os.flush()
                    }
                    val code = conn.responseCode
                    conn.disconnect()
                    if (code in 200..299) {
                        _isCloudConnected.value = true
                        _lastSyncTimestamp.value = System.currentTimeMillis()
                        _syncStatusMessage.value = "Deposit $depositId $newStatus Published"
                    }
                    syncToSupabaseStore(existingData)
                } catch (e: Exception) {
                    Log.e(TAG, "pushDepositStatusUpdate error: ${e.message}")
                }
            }
        }
    }

    fun pushWithdrawStatusUpdate(
        context: Context,
        withdrawId: String,
        newStatus: String,
        reviewNote: String = ""
    ) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    val existingData = fetchExistingCloudData(endpoint) ?: JSONObject()
                    val rawWithdrawals = existingData.optString("withdraw_requests", "[]")
                    val arr = try { JSONArray(rawWithdrawals) } catch (_: Exception) { JSONArray() }
                    var found = false

                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        if (item.optString("id") == withdrawId) {
                            item.put("status", newStatus)
                            if (reviewNote.isNotBlank()) item.put("reviewNote", reviewNote)
                            found = true
                            break
                        }
                    }

                    if (!found) {
                        val localWithdrawals = AdminManager.getInstance(context).withdrawalRequests.value
                        val matched = localWithdrawals.firstOrNull { it.id == withdrawId }
                        if (matched != null) {
                            val newItem = JSONObject().apply {
                                put("id", matched.id)
                                put("username", matched.username)
                                put("userPhone", matched.userPhone)
                                put("method", matched.method.name)
                                put("amount", matched.amount)
                                put("targetAccountNo", matched.targetAccountNo)
                                put("trxId", matched.trxId)
                                put("status", newStatus)
                                put("createdAt", matched.createdAt)
                                put("reviewNote", reviewNote.ifBlank { matched.reviewNote })
                            }
                            arr.put(newItem)
                        }
                    }

                    existingData.put("withdraw_requests", arr.toString())
                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "admin")

                    val fullLocal = SharedDataStore.exportAllDataJson(context)
                    existingData.put("payload", fullLocal)

                    val requestBody = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }.toString()

                    val url = URL(endpoint)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "PUT"
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        setRequestProperty("Accept", "application/json")
                        connectTimeout = 5000
                        readTimeout = 5000
                        doOutput = true
                    }
                    OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                        os.write(requestBody)
                        os.flush()
                    }
                    val code = conn.responseCode
                    conn.disconnect()
                    if (code in 200..299) {
                        _isCloudConnected.value = true
                        _lastSyncTimestamp.value = System.currentTimeMillis()
                        _syncStatusMessage.value = "Withdrawal $withdrawId $newStatus Published"
                    }
                    syncToSupabaseStore(existingData)
                } catch (e: Exception) {
                    Log.e(TAG, "pushWithdrawStatusUpdate error: ${e.message}")
                }
            }
        }
    }

    fun pushToCloud(context: Context) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    val fullLocalJson = SharedDataStore.exportAllDataJson(context)
                    val localObj = JSONObject(fullLocalJson)
                    val roundNow = System.currentTimeMillis() / 25000L
                    val paymentNums = localObj.optString("payment_numbers", "")

                    val existingData = fetchExistingCloudData(endpoint) ?: JSONObject()

                    val cloudDep = existingData.optString("deposit_requests", "")
                    val localDep = localObj.optString("deposit_requests", "")
                    val mergedDeposits = SharedDataStore.mergeJsonListsById(cloudDep, localDep)

                    val cloudWd = existingData.optString("withdraw_requests", "")
                    val localWd = localObj.optString("withdraw_requests", "")
                    val mergedWithdrawals = SharedDataStore.mergeJsonListsById(cloudWd, localWd)

                    val cloudUsers = existingData.optString("registered_users", "")
                    val localUsers = localObj.optString("registered_users", "")
                    val mergedUsers = SharedDataStore.mergeUsersByPhone(cloudUsers, localUsers, true)

                    val dataWrapper = JSONObject().apply {
                        put("version", 2)
                        put("snx_version", System.currentTimeMillis())
                        put("last_updated_time", System.currentTimeMillis())
                        put("last_updated_by", "admin")
                        put("active_round", roundNow)
                        put("payment_numbers", if (paymentNums.isNotBlank() && paymentNums != "[]") paymentNums else existingData.optString("payment_numbers", ""))
                        put("deposit_requests", mergedDeposits)
                        put("withdraw_requests", mergedWithdrawals)
                        put("registered_users", mergedUsers)
                        put("site_config", localObj.optString("site_config", existingData.optString("site_config", "{}")))
                        put("games_list", localObj.optString("games_list", existingData.optString("games_list", "[]")))
                        put("payload", fullLocalJson)
                    }

                    val requestBody = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", dataWrapper)
                    }.toString()

                    val url = URL(endpoint)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "PUT"
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        setRequestProperty("Accept", "application/json")
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                        connectTimeout = 4000
                        readTimeout = 4000
                        doOutput = true
                    }

                    OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                        os.write(requestBody)
                        os.flush()
                    }

                    val responseCode = conn.responseCode
                    if (responseCode in 200..299) {
                        _isCloudConnected.value = true
                        _lastSyncTimestamp.value = System.currentTimeMillis()
                        _cloudActiveRoundNumber.value = roundNow
                        _syncStatusMessage.value = "All Admin Changes Synchronized"
                        Log.d(TAG, "Successfully pushed Admin updates to Cloud Bridge: $responseCode")
                    } else {
                        Log.w(TAG, "Failed to push to Cloud Bridge: $responseCode")
                    }
                    conn.disconnect()

                    syncToSupabaseStore(dataWrapper)
                } catch (e: Exception) {
                    _isCloudConnected.value = false
                    Log.e(TAG, "Push to Cloud Bridge network error: ${e.message}")
                }
            }
        }
    }

    private fun syncToSupabaseStore(dataObj: JSONObject) {
        try {
            val url = URL("${SUPABASE_REST_URL}snx_store")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("apikey", SUPABASE_ANON_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
                setRequestProperty("Prefer", "resolution=merge-duplicates")
                setRequestProperty("Cache-Control", "no-cache, no-store")
                connectTimeout = 4000
                readTimeout = 4000
                doOutput = true
            }
            val payload = JSONObject().apply {
                put("key", "global_state")
                put("value", dataObj.toString())
                put("updated_at", System.currentTimeMillis())
            }.toString()

            OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                os.write(payload)
                os.flush()
            }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {}
    }

    fun pullFromCloud(context: Context) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val endpoint = getCloudEndpoint(context)
                    val dataObj = fetchExistingCloudData(endpoint) ?: return@withLock
                    val remoteVersion = maxOf(
                        dataObj.optLong("snx_version", 0L),
                        dataObj.optLong("last_updated_time", 0L)
                    )
                    val activeRound = dataObj.optLong("active_round", 0L)
                    if (activeRound > 0L) {
                        _cloudActiveRoundNumber.value = activeRound
                    }

                    // Helper to extract fields
                    val payloadObj = if (dataObj.has("payload")) {
                        try {
                            val p = dataObj.opt("payload")
                            when (p) {
                                is JSONObject -> p
                                is String -> if (p.trim().startsWith("{")) JSONObject(p) else null
                                else -> null
                            }
                        } catch (_: Exception) { null }
                    } else null

                    fun extractField(key: String): String {
                        val direct = dataObj.opt(key)
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

                    val formattedPayload = JSONObject().apply {
                        put("payment_numbers", paymentNumbersVal)
                        put("deposit_requests", depositRequestsVal)
                        put("withdraw_requests", withdrawRequestsVal)
                        put("registered_users", registeredUsersVal)
                        put("site_config", siteConfigVal)
                        put("games_list", gamesListVal)
                        put("timestamp", remoteVersion)
                    }

                    SharedDataStore.importAllDataJson(context, formattedPayload.toString())
                    _lastSyncTimestamp.value = System.currentTimeMillis()
                    _isCloudConnected.value = true
                } catch (e: Exception) {
                    _isCloudConnected.value = false
                    Log.e(TAG, "Pull from Cloud Bridge network error: ${e.message}")
                }
            }
        }
    }
}
