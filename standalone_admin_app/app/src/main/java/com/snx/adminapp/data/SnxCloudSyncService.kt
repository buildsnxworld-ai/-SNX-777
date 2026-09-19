package com.snx.adminapp.data

import android.content.Context
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
 * via real-time internet cloud synchronization.
 */
object SnxCloudSyncService {

    private const val TAG = "SnxCloudSyncService"

    private const val CLOUD_OBJECT_ID = "ff808181a09d98f701a0a9a7b167194a"
    private const val BASE_URL = "https://api.restful-api.dev/objects"

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val syncMutex = Mutex()

    private val _isCloudConnected = MutableStateFlow(true)
    val isCloudConnected: StateFlow<Boolean> = _isCloudConnected.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _cloudActiveRoundNumber = MutableStateFlow(0L)
    val cloudActiveRoundNumber: StateFlow<Long> = _cloudActiveRoundNumber.asStateFlow()

    private var autoSyncJob: Job? = null
    private var lastKnownRemoteVersion = 0L

    fun startAutoSync(context: Context, intervalMs: Long = 2000L) {
        if (autoSyncJob?.isActive == true) return
        autoSyncJob = scope.launch {
            while (isActive) {
                try {
                    pullFromCloud(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Auto sync iteration error: ${e.message}")
                }
                delay(intervalMs)
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
            delay(200)
            pushToCloud(context)
        }
    }

    fun pushPaymentNumbersToCloud(context: Context, paymentNumbersJson: String) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val existingData = fetchExistingCloudData() ?: JSONObject()
                    val roundNow = System.currentTimeMillis() / 25000L

                    existingData.put("version", 2)
                    existingData.put("snx_version", System.currentTimeMillis())
                    existingData.put("last_updated_time", System.currentTimeMillis())
                    existingData.put("last_updated_by", "admin")
                    existingData.put("active_round", roundNow)
                    existingData.put("payment_numbers", paymentNumbersJson)

                    // Also embed inside payload for full backwards compatibility
                    val fullLocalJson = SharedDataStore.exportAllDataJson(context)
                    val localObj = try { JSONObject(fullLocalJson) } catch (_: Exception) { JSONObject() }
                    localObj.put("payment_numbers", paymentNumbersJson)
                    existingData.put("payload", localObj.toString())

                    val requestBody = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", existingData)
                    }.toString()

                    val url = URL("$BASE_URL/$CLOUD_OBJECT_ID")
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

                    val responseCode = conn.responseCode
                    conn.disconnect()
                    if (responseCode in 200..299) {
                        _isCloudConnected.value = true
                        _lastSyncTimestamp.value = System.currentTimeMillis()
                        Log.d(TAG, "Successfully published payment numbers to Cloud Bridge ($responseCode)")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "pushPaymentNumbersToCloud error: ${e.message}")
                }
            }
        }
    }

    fun pushToCloud(context: Context) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val fullLocalJson = SharedDataStore.exportAllDataJson(context)
                    val localObj = JSONObject(fullLocalJson)
                    val roundNow = System.currentTimeMillis() / 25000L
                    val paymentNums = localObj.optString("payment_numbers", "")

                    val existingData = fetchExistingCloudData() ?: JSONObject()

                    val dataWrapper = JSONObject().apply {
                        put("version", 2)
                        put("snx_version", System.currentTimeMillis())
                        put("last_updated_time", System.currentTimeMillis())
                        put("last_updated_by", "admin")
                        put("active_round", roundNow)
                        put("payment_numbers", if (paymentNums.isNotBlank() && paymentNums != "[]") paymentNums else existingData.optString("payment_numbers", ""))
                        put("deposit_requests", localObj.optString("deposit_requests", existingData.optString("deposit_requests", "[]")))
                        put("withdraw_requests", localObj.optString("withdraw_requests", existingData.optString("withdraw_requests", "[]")))
                        put("registered_users", localObj.optString("registered_users", existingData.optString("registered_users", "[]")))
                        put("site_config", localObj.optString("site_config", existingData.optString("site_config", "{}")))
                        put("games_list", localObj.optString("games_list", existingData.optString("games_list", "[]")))
                        put("payload", fullLocalJson)
                    }

                    val requestBody = JSONObject().apply {
                        put("name", "snx_cloud_bridge")
                        put("data", dataWrapper)
                    }.toString()

                    val url = URL("$BASE_URL/$CLOUD_OBJECT_ID")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "PUT"
                        setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        setRequestProperty("Accept", "application/json")
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
                        Log.d(TAG, "Successfully pushed Admin updates to Cloud Bridge: $responseCode")
                    } else {
                        Log.w(TAG, "Failed to push to Cloud Bridge: $responseCode")
                    }
                    conn.disconnect()
                } catch (e: Exception) {
                    _isCloudConnected.value = false
                    Log.e(TAG, "Push to Cloud Bridge network error: ${e.message}")
                }
            }
        }
    }

    private fun fetchExistingCloudData(): JSONObject? {
        return try {
            val url = URL("$BASE_URL/$CLOUD_OBJECT_ID")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                connectTimeout = 4000
                readTimeout = 4000
            }
            if (conn.responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use {
                    it.readText()
                }
                conn.disconnect()
                val rootObj = JSONObject(responseText)
                rootObj.optJSONObject("data")
            } else {
                conn.disconnect()
                null
            }
        } catch (_: Exception) { null }
    }

    fun pullFromCloud(context: Context) {
        scope.launch {
            syncMutex.withLock {
                try {
                    val dataObj = fetchExistingCloudData() ?: return@withLock
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
