package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.example.model.PaymentMethod
import com.example.model.TransactionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Universal High-Reliability Local Data Bridge for real-time synchronization between:
 * 1) Main Game Website App (com.aistudio.snx777.game)
 * 2) Standalone Admin Panel App (com.aistudio.snx777.admin)
 *
 * Uses a quad-redundant architecture:
 * 1. Synchronous ContentProvider IPC (< 1ms direct Binder RPC with auto-process launch)
 * 2. Targeted Explicit Broadcasts (delivered through Android 8.0+ background execution limits)
 * 3. Bidirectional smart-merging on app resume / polling every 2s
 * 4. In-memory state-flow pulse for instant reactive UI updates
 */
object SharedDataStore {

    private const val TAG = "SharedDataStore"

    const val PREF_ADMIN = "snx_admin_panel_prefs"
    const val PREF_SESSION = "snx_session_cookies_prefs"
    const val ACTION_DATA_SYNC = "com.aistudio.snx777.ACTION_DATA_SYNC"

    const val GAME_PACKAGE = "com.aistudio.snx777.game"
    const val ADMIN_PACKAGE = "com.aistudio.snx777.admin"

    val GAME_PROVIDER_URI: Uri = Uri.parse("content://com.aistudio.snx777.game.provider")
    val ADMIN_PROVIDER_URI: Uri = Uri.parse("content://com.aistudio.snx777.admin.provider")

    // In-memory sync pulse flow for ViewModels and Composables
    private val _syncFlow = MutableSharedFlow<Long>(extraBufferCapacity = 64)
    val syncFlow = _syncFlow.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    fun isGameAppInstalled(context: Context): Boolean {
        if (context.packageName == GAME_PACKAGE) return false
        return try {
            context.packageManager.getPackageInfo(GAME_PACKAGE, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isAdminAppInstalled(context: Context): Boolean {
        if (context.packageName == ADMIN_PACKAGE) return false
        return try {
            context.packageManager.getPackageInfo(ADMIN_PACKAGE, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Safely executes ContentProvider call only if provider authority is declared and available.
     * Prevents Android ActivityThread from logging "Failed to find provider info" when a package is not installed.
     */
    fun safeCall(
        context: Context,
        uri: Uri,
        method: String,
        arg: String? = null,
        extras: Bundle? = null
    ): Bundle? {
        val auth = uri.authority ?: return null
        if (auth == "com.aistudio.snx777.game.provider" && context.packageName != GAME_PACKAGE && !isGameAppInstalled(context)) {
            return null
        }
        if (auth == "com.aistudio.snx777.admin.provider" && context.packageName != ADMIN_PACKAGE && !isAdminAppInstalled(context)) {
            return null
        }
        return try {
            val info = context.packageManager.resolveContentProvider(auth, 0)
            if (info == null) {
                return null
            }
            context.contentResolver.call(uri, method, arg, extras)
        } catch (e: Exception) {
            Log.w(TAG, "safeCall to $uri failed: ${e.message}")
            null
        }
    }

    fun getAdminPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREF_ADMIN, Context.MODE_PRIVATE)
    }

    fun getSessionPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE)
    }

    /**
     * Submit a new deposit from Website directly to Admin Panel via ContentProvider IPC & Explicit Broadcast.
     */
    fun submitDepositToAdmin(context: Context, req: DepositRequest) {
        scope.launch {
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

            val extras = Bundle().apply {
                putString("deposit_json", reqObj.toString())
            }

            // 1. Direct ContentProvider IPC to Admin App
            safeCall(context, ADMIN_PROVIDER_URI, "SUBMIT_DEPOSIT", null, extras)

            // 2. Explicit Broadcast to Admin App
            sendExplicitBroadcast(context, ADMIN_PACKAGE, "NEW_DEPOSIT", reqObj.toString())

            // 3. Fallback sync full state
            broadcastAndSync(context)
        }
    }

    /**
     * Submit a new withdrawal from Website directly to Admin Panel via ContentProvider IPC & Explicit Broadcast.
     */
    fun submitWithdrawToAdmin(context: Context, req: WithdrawalRequest) {
        scope.launch {
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

            val extras = Bundle().apply {
                putString("withdraw_json", reqObj.toString())
            }

            safeCall(context, ADMIN_PROVIDER_URI, "SUBMIT_WITHDRAW", null, extras)

            sendExplicitBroadcast(context, ADMIN_PACKAGE, "NEW_WITHDRAW", reqObj.toString())
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Website that a deposit request status has been updated (e.g. APPROVED or REJECTED) by Admin.
     */
    fun notifyDepositStatusChanged(
        context: Context,
        requestId: String,
        newStatus: String,
        amount: Double,
        userPhone: String,
        username: String
    ) {
        scope.launch {
            val extras = Bundle().apply {
                putString("request_id", requestId)
                putString("status", newStatus)
                putDouble("amount", amount)
                putString("user_phone", userPhone)
                putString("username", username)
            }

            safeCall(context, GAME_PROVIDER_URI, "UPDATE_DEPOSIT_STATUS", null, extras)

            sendExplicitBroadcast(context, GAME_PACKAGE, "UPDATE_DEPOSIT_STATUS", requestId)
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Website that a withdrawal request status has been updated by Admin.
     */
    fun notifyWithdrawStatusChanged(
        context: Context,
        requestId: String,
        newStatus: String,
        amount: Double,
        userPhone: String,
        username: String
    ) {
        scope.launch {
            val extras = Bundle().apply {
                putString("request_id", requestId)
                putString("status", newStatus)
                putDouble("amount", amount)
                putString("user_phone", userPhone)
                putString("username", username)
            }

            safeCall(context, GAME_PROVIDER_URI, "UPDATE_WITHDRAW_STATUS", null, extras)

            sendExplicitBroadcast(context, GAME_PACKAGE, "UPDATE_WITHDRAW_STATUS", requestId)
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Website when Admin changes payment numbers or agent details.
     */
    fun notifyPaymentNumbersChanged(context: Context, paymentNumbersJson: String) {
        scope.launch {
            val extras = Bundle().apply {
                putString("payment_numbers_json", paymentNumbersJson)
            }

            safeCall(context, GAME_PROVIDER_URI, "UPDATE_PAYMENT_NUMBERS", null, extras)

            sendExplicitBroadcast(context, GAME_PACKAGE, "UPDATE_PAYMENT_NUMBERS", paymentNumbersJson)
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Admin when a new user registers on the website.
     */
    fun notifyUserRegistered(context: Context, account: RegisteredAccount) {
        scope.launch {
            val obj = JSONObject().apply {
                put("username", account.username)
                put("phone", account.phone)
                put("email", account.email)
                put("password", account.password)
                put("balanceBDT", account.balanceBDT)
                put("totalDeposited", account.totalDeposited)
                put("totalWithdrawn", account.totalWithdrawn)
                put("vipLevel", account.vipLevel)
                put("registeredDate", account.registeredDate)
                put("status", account.status)
            }

            val extras = Bundle().apply {
                putString("user_json", obj.toString())
            }

            safeCall(context, ADMIN_PROVIDER_URI, "REGISTER_USER", null, extras)

            sendExplicitBroadcast(context, ADMIN_PACKAGE, "NEW_USER_REGISTERED", obj.toString())
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Website when Admin adds, edits, or deletes games.
     */
    fun notifyGamesChanged(context: Context, gamesJson: String) {
        scope.launch {
            val extras = Bundle().apply {
                putString("games_json", gamesJson)
            }

            safeCall(context, GAME_PROVIDER_URI, "UPDATE_GAMES", null, extras)

            sendExplicitBroadcast(context, GAME_PACKAGE, "UPDATE_GAMES", gamesJson)
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Website when Admin updates site notice / ticker / banner.
     */
    fun notifySiteConfigChanged(context: Context, configJson: String) {
        scope.launch {
            val extras = Bundle().apply {
                putString("site_config_json", configJson)
            }

            safeCall(context, GAME_PROVIDER_URI, "UPDATE_SITE_CONFIG", null, extras)

            sendExplicitBroadcast(context, GAME_PACKAGE, "UPDATE_SITE_CONFIG", configJson)
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Website when Admin updates a user's status (ACTIVE, SUSPENDED, BANNED).
     */
    fun notifyUserStatusChanged(context: Context, phone: String, status: String) {
        scope.launch {
            val extras = Bundle().apply {
                putString("user_phone", phone)
                putString("user_status", status)
            }

            safeCall(context, GAME_PROVIDER_URI, "UPDATE_USER_STATUS", null, extras)

            val payload = JSONObject().apply {
                put("phone", phone)
                put("status", status)
            }.toString()

            sendExplicitBroadcast(context, GAME_PACKAGE, "UPDATE_USER_STATUS", payload)
            broadcastAndSync(context)
        }
    }

    /**
     * Notify Website when Admin manually adjusts a user's balance.
     */
    fun notifyBalanceAdjusted(context: Context, phone: String, newBalance: Double) {
        scope.launch {
            val extras = Bundle().apply {
                putString("user_phone", phone)
                putDouble("new_balance", newBalance)
            }

            safeCall(context, GAME_PROVIDER_URI, "ADJUST_USER_BALANCE", null, extras)

            sendExplicitBroadcast(context, GAME_PACKAGE, "ADJUST_USER_BALANCE", phone)
            broadcastAndSync(context)
        }
    }

    /**
     * Send targeted explicit broadcast to bypass Android background limits.
     */
    private fun sendExplicitBroadcast(context: Context, targetPackage: String, actionType: String, data: String) {
        val shouldSendExplicit = when (targetPackage) {
            GAME_PACKAGE -> isGameAppInstalled(context)
            ADMIN_PACKAGE -> isAdminAppInstalled(context)
            else -> false
        }
        if (shouldSendExplicit) {
            try {
                val intent = Intent(ACTION_DATA_SYNC).apply {
                    setPackage(targetPackage)
                    putExtra("action_type", actionType)
                    putExtra("data_payload", data)
                    putExtra("timestamp", System.currentTimeMillis())
                }
                context.sendBroadcast(intent)
            } catch (_: Exception) {}
        }

        // Also fire general broadcast for in-process receivers
        try {
            val intent = Intent(ACTION_DATA_SYNC).apply {
                putExtra("action_type", actionType)
                putExtra("data_payload", data)
                putExtra("timestamp", System.currentTimeMillis())
            }
            context.sendBroadcast(intent)
        } catch (_: Exception) {}
    }

    /**
     * Exports all crucial sync keys into a single portable JSON payload.
     */
    fun exportAllDataJson(context: Context): String {
        val root = JSONObject()
        try {
            val adminPrefs = getAdminPrefs(context)
            val sessionPrefs = getSessionPrefs(context)

            // Admin data
            root.put("payment_numbers", adminPrefs.getString("key_payment_numbers_json", "") ?: "")
            root.put("deposit_requests", adminPrefs.getString("key_deposit_requests_json", "") ?: "")
            root.put("withdraw_requests", adminPrefs.getString("key_withdraw_requests_json", "") ?: "")
            root.put("site_config", adminPrefs.getString("key_site_config_json", "") ?: "")
            root.put("games_list", adminPrefs.getString("key_games_list_json", "") ?: "")

            // Session & User data
            root.put("registered_users", sessionPrefs.getString("key_registered_users_json", "") ?: "")
            root.put("transactions", sessionPrefs.getString("key_transactions_json", "") ?: "")
            root.put("balance_bits", sessionPrefs.getLong("key_balance_bits", 0L))
            root.put("user_phone", sessionPrefs.getString("key_phone", "") ?: "")
            root.put("user_name", sessionPrefs.getString("key_username", "") ?: "")
            root.put("is_logged_in", sessionPrefs.getBoolean("key_is_logged_in", false))
            root.put("sender_package", context.packageName)
            root.put("timestamp", System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data: ${e.message}")
        }
        return root.toString()
    }

    /**
     * Intelligent bidirectional merge that NEVER overwrites or loses records.
     */
    fun importAllDataJson(context: Context, jsonStr: String): Boolean {
        if (jsonStr.isEmpty()) return false
        return try {
            val root = JSONObject(jsonStr)
            val adminPrefs = getAdminPrefs(context)
            val sessionPrefs = getSessionPrefs(context)
            val isCurrentAdminApp = context.packageName.contains("admin")

            // 1. Merge Deposit Requests
            if (root.has("deposit_requests")) {
                val incomingDep = root.getString("deposit_requests")
                if (incomingDep.isNotBlank()) {
                    val currentDep = adminPrefs.getString("key_deposit_requests_json", "") ?: ""
                    val mergedDep = mergeJsonListsById(currentDep, incomingDep)
                    adminPrefs.edit().putString("key_deposit_requests_json", mergedDep).apply()
                }
            }

            // 2. Merge Withdraw Requests
            if (root.has("withdraw_requests")) {
                val incomingWd = root.getString("withdraw_requests")
                if (incomingWd.isNotBlank()) {
                    val currentWd = adminPrefs.getString("key_withdraw_requests_json", "") ?: ""
                    val mergedWd = mergeJsonListsById(currentWd, incomingWd)
                    adminPrefs.edit().putString("key_withdraw_requests_json", mergedWd).apply()
                }
            }

            // 3. Payment Numbers: Update and refresh state if valid
            if (root.has("payment_numbers")) {
                val incomingNumbers = root.getString("payment_numbers")
                if (incomingNumbers.isNotBlank() && incomingNumbers != "[]") {
                    val currentNumbers = adminPrefs.getString("key_payment_numbers_json", "") ?: ""
                    if (!isCurrentAdminApp || currentNumbers.isBlank() || currentNumbers == "[]") {
                        if (currentNumbers != incomingNumbers) {
                            adminPrefs.edit().putString("key_payment_numbers_json", incomingNumbers).apply()
                            AdminManager.getInstance(context).applyPaymentNumbersFromCloud(incomingNumbers)
                        }
                    }
                }
            }

            // 4. Site Config: Admin app is authority
            if (root.has("site_config")) {
                val incomingConfig = root.getString("site_config")
                if (incomingConfig.isNotBlank() && !isCurrentAdminApp) {
                    adminPrefs.edit().putString("key_site_config_json", incomingConfig).apply()
                }
            }

            // 4b. Games List: Admin app is authority
            if (root.has("games_list")) {
                val incomingGames = root.getString("games_list")
                if (incomingGames.isNotBlank() && !isCurrentAdminApp) {
                    adminPrefs.edit().putString("key_games_list_json", incomingGames).apply()
                }
            }

            // 5. Merge Registered Users
            if (root.has("registered_users")) {
                val incomingUsers = root.getString("registered_users")
                if (incomingUsers.isNotBlank()) {
                    val currentUsers = sessionPrefs.getString("key_registered_users_json", "") ?: ""
                    val mergedUsers = mergeUsersByPhone(currentUsers, incomingUsers)
                    sessionPrefs.edit().putString("key_registered_users_json", mergedUsers).apply()
                }
            }

            // 6. Merge Transactions
            if (root.has("transactions")) {
                val incomingTxs = root.getString("transactions")
                if (incomingTxs.isNotBlank()) {
                    val currentTxs = sessionPrefs.getString("key_transactions_json", "") ?: ""
                    val mergedTxs = mergeJsonListsById(currentTxs, incomingTxs)
                    sessionPrefs.edit().putString("key_transactions_json", mergedTxs).apply()
                }
            }

            _syncFlow.tryEmit(System.currentTimeMillis())
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing data: ${e.message}")
            false
        }
    }

    /**
     * Broadcasts sync event to the other app and pushes data via ContentProvider.
     */
    fun broadcastAndSync(context: Context) {
        val payload = exportAllDataJson(context)

        // 1. Push payload to other app's ContentProvider only if installed
        if (isGameAppInstalled(context)) {
            val extras = Bundle().apply {
                putString("json_payload", payload)
            }
            safeCall(context, GAME_PROVIDER_URI, "SAVE_ALL_DATA", null, extras)
            sendExplicitBroadcast(context, GAME_PACKAGE, "FULL_SYNC", payload)
        }
        if (isAdminAppInstalled(context)) {
            val extras = Bundle().apply {
                putString("json_payload", payload)
            }
            safeCall(context, ADMIN_PROVIDER_URI, "SAVE_ALL_DATA", null, extras)
            sendExplicitBroadcast(context, ADMIN_PACKAGE, "FULL_SYNC", payload)
        }

        // 2. Real-Time Internet Cloud Synchronization
        SnxCloudSyncService.pushToCloud(context)

        _syncFlow.tryEmit(System.currentTimeMillis())
    }

    /**
     * Pulls the latest data from the other app if available.
     */
    fun pullFromOtherApp(context: Context) {
        scope.launch {
            val targetUri = if (context.packageName.contains("admin")) {
                if (isGameAppInstalled(context)) GAME_PROVIDER_URI else null
            } else {
                if (isAdminAppInstalled(context)) ADMIN_PROVIDER_URI else null
            }

            if (targetUri != null) {
                try {
                    val response = safeCall(context, targetUri, "GET_ALL_DATA", null, null)
                    val payload = response?.getString("json_payload")
                    if (!payload.isNullOrEmpty()) {
                        importAllDataJson(context, payload)
                    }
                } catch (_: Exception) {}
            }

            // Real-Time Internet Cloud Sync pull
            SnxCloudSyncService.pullFromCloud(context)
        }
    }

    /**
     * Merges two JSON arrays of items having an "id" field without losing items.
     * If an item exists in both, the non-PENDING status item or newer takes precedence.
     */
    private fun mergeJsonListsById(currentJson: String, incomingJson: String): String {
        val map = LinkedHashMap<String, JSONObject>()
        try {
            if (currentJson.isNotBlank()) {
                val curArray = JSONArray(currentJson)
                for (i in 0 until curArray.length()) {
                    val obj = curArray.getJSONObject(i)
                    val id = obj.optString("id", i.toString())
                    map[id] = obj
                }
            }
            if (incomingJson.isNotBlank()) {
                val incArray = JSONArray(incomingJson)
                for (i in 0 until incArray.length()) {
                    val obj = incArray.getJSONObject(i)
                    val id = obj.optString("id", i.toString())
                    if (!map.containsKey(id)) {
                        map[id] = obj
                    } else {
                        // If current is PENDING and incoming is APPROVED/REJECTED, incoming wins!
                        val existing = map[id]!!
                        val existingStatus = existing.optString("status", "")
                        val incomingStatus = obj.optString("status", "")
                        if (existingStatus == "PENDING" && incomingStatus != "PENDING" && incomingStatus.isNotEmpty()) {
                            map[id] = obj
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        val result = JSONArray()
        for (item in map.values) {
            result.put(item)
        }
        return result.toString()
    }

    /**
     * Merges registered users by phone number without losing users.
     */
    private fun mergeUsersByPhone(currentJson: String, incomingJson: String): String {
        val map = LinkedHashMap<String, JSONObject>()
        try {
            if (currentJson.isNotBlank()) {
                val curArray = JSONArray(currentJson)
                for (i in 0 until curArray.length()) {
                    val obj = curArray.getJSONObject(i)
                    val phone = obj.optString("phone", obj.optString("username", i.toString()))
                    map[phone] = obj
                }
            }
            if (incomingJson.isNotBlank()) {
                val incArray = JSONArray(incomingJson)
                for (i in 0 until incArray.length()) {
                    val obj = incArray.getJSONObject(i)
                    val phone = obj.optString("phone", obj.optString("username", i.toString()))
                    if (!map.containsKey(phone)) {
                        map[phone] = obj
                    } else {
                        // Keep highest balance and total deposited
                        val existing = map[phone]!!
                        val existBal = existing.optDouble("balanceBDT", 0.0)
                        val incBal = obj.optDouble("balanceBDT", 0.0)
                        if (incBal > existBal) {
                            existing.put("balanceBDT", incBal)
                        }
                        val existDep = existing.optDouble("totalDeposited", 0.0)
                        val incDep = obj.optDouble("totalDeposited", 0.0)
                        if (incDep > existDep) {
                            existing.put("totalDeposited", incDep)
                        }
                        if (obj.has("status")) {
                            existing.put("status", obj.getString("status"))
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        val result = JSONArray()
        for (item in map.values) {
            result.put(item)
        }
        return result.toString()
    }
}
