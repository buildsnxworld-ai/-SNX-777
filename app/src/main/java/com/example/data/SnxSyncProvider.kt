package com.example.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.example.model.PaymentMethod
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject

/**
 * ContentProvider for high-speed inter-application communication and real-time data sync
 * between Game Website app (com.aistudio.snx777.game) and Standalone Admin Panel app (com.aistudio.snx777.admin).
 */
class SnxSyncProvider : ContentProvider() {

    companion object {
        private const val TAG = "SnxSyncProvider"
    }

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val ctx = context ?: return null
        val response = Bundle()

        try {
            when (method) {
                "PING" -> {
                    response.putBoolean("pong", true)
                }

                // Direct deposit submission from Game App into Admin App
                "SUBMIT_DEPOSIT" -> {
                    val depositJson = extras?.getString("deposit_json")
                    if (!depositJson.isNullOrEmpty()) {
                        val obj = JSONObject(depositJson)
                        val req = DepositRequest(
                            id = obj.getString("id"),
                            username = obj.getString("username"),
                            userPhone = obj.getString("userPhone"),
                            method = PaymentMethod.valueOf(obj.getString("method")),
                            amount = obj.getDouble("amount"),
                            agentNumberUsed = obj.getString("agentNumberUsed"),
                            userAccountNo = obj.getString("userAccountNo"),
                            trxId = obj.getString("trxId"),
                            status = TransactionStatus.valueOf(obj.optString("status", "PENDING")),
                            createdAt = obj.optString("createdAt", ""),
                            reviewNote = obj.optString("reviewNote", "")
                        )
                        AdminManager.getInstance(ctx).insertIncomingDeposit(req)
                        response.putBoolean("success", true)
                    }
                }

                // Direct withdrawal submission from Game App into Admin App
                "SUBMIT_WITHDRAW" -> {
                    val withdrawJson = extras?.getString("withdraw_json")
                    if (!withdrawJson.isNullOrEmpty()) {
                        val obj = JSONObject(withdrawJson)
                        val req = WithdrawalRequest(
                            id = obj.getString("id"),
                            username = obj.getString("username"),
                            userPhone = obj.getString("userPhone"),
                            method = PaymentMethod.valueOf(obj.getString("method")),
                            amount = obj.getDouble("amount"),
                            targetAccountNo = obj.getString("targetAccountNo"),
                            trxId = obj.optString("trxId", "WD" + (100000..999999).random()),
                            status = TransactionStatus.valueOf(obj.optString("status", "PENDING")),
                            createdAt = obj.optString("createdAt", ""),
                            reviewNote = obj.optString("reviewNote", "")
                        )
                        AdminManager.getInstance(ctx).insertIncomingWithdrawal(req)
                        response.putBoolean("success", true)
                    }
                }

                // Admin notifies Game App that a deposit has been APPROVED or REJECTED
                "UPDATE_DEPOSIT_STATUS" -> {
                    val reqId = extras?.getString("request_id") ?: ""
                    val newStatusStr = extras?.getString("status") ?: "APPROVED"
                    val amount = extras?.getDouble("amount") ?: 0.0
                    val phone = extras?.getString("user_phone") ?: ""
                    val username = extras?.getString("username") ?: ""

                    val newStatus = try {
                        TransactionStatus.valueOf(newStatusStr)
                    } catch (_: Exception) {
                        TransactionStatus.APPROVED
                    }

                    // 1. Update deposit requests in AdminManager / PREF_ADMIN
                    AdminManager.getInstance(ctx).updateDepositStatusLocally(reqId, newStatus)

                    // 2. If APPROVED, credit user's balance and update session transaction in PREF_SESSION
                    if (newStatus == TransactionStatus.APPROVED && amount > 0.0) {
                        val sessionMgr = SessionManager(ctx)
                        val currentSession = sessionMgr.getUserSession()
                        if (currentSession.isLoggedIn) {
                            sessionMgr.saveUserSession(
                                currentSession.copy(
                                    balanceBDT = currentSession.balanceBDT + amount,
                                    totalDeposited = currentSession.totalDeposited + amount
                                )
                            )
                        }

                        // Update registered accounts list
                        val accounts = sessionMgr.getRegisteredAccounts().map { acc ->
                            if (acc.phone == phone || acc.username.equals(username, ignoreCase = true)) {
                                acc.copy(
                                    balanceBDT = acc.balanceBDT + amount,
                                    totalDeposited = acc.totalDeposited + amount
                                )
                            } else acc
                        }
                        sessionMgr.saveRegisteredAccounts(accounts)

                        // Update transaction records
                        val txs = sessionMgr.getTransactions() ?: emptyList()
                        val updatedTxs = txs.map { tx ->
                            if (tx.type == TransactionType.DEPOSIT && (tx.id == reqId || tx.status == TransactionStatus.PENDING)) {
                                tx.copy(status = TransactionStatus.APPROVED)
                            } else tx
                        }
                        sessionMgr.saveTransactions(updatedTxs)
                    } else if (newStatus == TransactionStatus.REJECTED) {
                        val sessionMgr = SessionManager(ctx)
                        val txs = sessionMgr.getTransactions() ?: emptyList()
                        val updatedTxs = txs.map { tx ->
                            if (tx.type == TransactionType.DEPOSIT && (tx.id == reqId || tx.status == TransactionStatus.PENDING)) {
                                tx.copy(status = TransactionStatus.REJECTED)
                            } else tx
                        }
                        sessionMgr.saveTransactions(updatedTxs)
                    }

                    response.putBoolean("success", true)
                }

                // Admin notifies Game App that a withdrawal has been APPROVED or REJECTED
                "UPDATE_WITHDRAW_STATUS" -> {
                    val reqId = extras?.getString("request_id") ?: ""
                    val newStatusStr = extras?.getString("status") ?: "APPROVED"
                    val amount = extras?.getDouble("amount") ?: 0.0
                    val phone = extras?.getString("user_phone") ?: ""

                    val newStatus = try {
                        TransactionStatus.valueOf(newStatusStr)
                    } catch (_: Exception) {
                        TransactionStatus.APPROVED
                    }

                    AdminManager.getInstance(ctx).updateWithdrawStatusLocally(reqId, newStatus)

                    val sessionMgr = SessionManager(ctx)
                    if (newStatus == TransactionStatus.REJECTED && amount > 0.0) {
                        // Refund balance on rejection
                        val currentSession = sessionMgr.getUserSession()
                        if (currentSession.isLoggedIn) {
                            sessionMgr.saveUserSession(
                                currentSession.copy(
                                    balanceBDT = currentSession.balanceBDT + amount,
                                    totalWithdrawn = maxOf(0.0, currentSession.totalWithdrawn - amount)
                                )
                            )
                        }
                    }

                    val txs = sessionMgr.getTransactions() ?: emptyList()
                    val updatedTxs = txs.map { tx ->
                        if (tx.type == TransactionType.WITHDRAW && (tx.id == reqId || tx.status == TransactionStatus.PENDING)) {
                            tx.copy(status = newStatus)
                        } else tx
                    }
                    sessionMgr.saveTransactions(updatedTxs)

                    response.putBoolean("success", true)
                }

                // Admin pushes updated payment numbers to Game App
                "UPDATE_PAYMENT_NUMBERS" -> {
                    val numbersJson = extras?.getString("payment_numbers_json")
                    if (!numbersJson.isNullOrEmpty()) {
                        val prefs = SharedDataStore.getAdminPrefs(ctx)
                        prefs.edit().putString("key_payment_numbers_json", numbersJson).apply()
                        AdminManager.getInstance(ctx).reloadFromStorage()
                        response.putBoolean("success", true)
                    }
                }

                // Admin pushes updated games list to Game App
                "UPDATE_GAMES" -> {
                    val gamesJson = extras?.getString("games_json")
                    if (!gamesJson.isNullOrEmpty()) {
                        val prefs = SharedDataStore.getAdminPrefs(ctx)
                        prefs.edit().putString("key_games_list_json", gamesJson).apply()
                        AdminManager.getInstance(ctx).reloadFromStorage()
                        response.putBoolean("success", true)
                    }
                }

                // Admin pushes updated site configuration to Game App
                "UPDATE_SITE_CONFIG" -> {
                    val configJson = extras?.getString("site_config_json")
                    if (!configJson.isNullOrEmpty()) {
                        val prefs = SharedDataStore.getAdminPrefs(ctx)
                        prefs.edit().putString("key_site_config_json", configJson).apply()
                        AdminManager.getInstance(ctx).reloadFromStorage()
                        response.putBoolean("success", true)
                    }
                }

                // Admin updates user status (ACTIVE, SUSPENDED, BANNED)
                "UPDATE_USER_STATUS" -> {
                    val phone = extras?.getString("user_phone") ?: ""
                    val newStatus = extras?.getString("user_status") ?: "ACTIVE"
                    val sessionMgr = SessionManager(ctx)
                    val currentSession = sessionMgr.getUserSession()
                    if (currentSession.phone == phone) {
                        sessionMgr.saveUserSession(currentSession.copy(status = newStatus))
                    }
                    val accounts = sessionMgr.getRegisteredAccounts().map {
                        if (it.phone == phone) it.copy(status = newStatus) else it
                    }
                    sessionMgr.saveRegisteredAccounts(accounts)
                    AdminManager.getInstance(ctx).reloadFromStorage()
                    response.putBoolean("success", true)
                }

                // Game app registers a user -> add to Admin Panel's registered users
                "REGISTER_USER" -> {
                    val userJson = extras?.getString("user_json")
                    if (!userJson.isNullOrEmpty()) {
                        val obj = JSONObject(userJson)
                        val acc = RegisteredAccount(
                            username = obj.getString("username"),
                            phone = obj.getString("phone"),
                            email = obj.optString("email", ""),
                            password = obj.optString("password", "••••"),
                            balanceBDT = obj.optDouble("balanceBDT", 0.0),
                            totalDeposited = obj.optDouble("totalDeposited", 0.0),
                            totalWithdrawn = obj.optDouble("totalWithdrawn", 0.0),
                            vipLevel = obj.optString("vipLevel", "VIP 1"),
                            registeredDate = obj.optString("registeredDate", ""),
                            status = obj.optString("status", "ACTIVE")
                        )
                        SessionManager(ctx).saveRegisteredAccount(acc)
                        AdminManager.getInstance(ctx).reloadFromStorage()
                        response.putBoolean("success", true)
                    }
                }

                // Admin adjusts user's balance -> apply to Game App
                "ADJUST_USER_BALANCE" -> {
                    val phone = extras?.getString("user_phone") ?: ""
                    val newBalance = extras?.getDouble("new_balance") ?: 0.0
                    val sessionMgr = SessionManager(ctx)
                    val currentSession = sessionMgr.getUserSession()
                    if (currentSession.phone == phone) {
                        sessionMgr.saveUserSession(currentSession.copy(balanceBDT = newBalance))
                    }
                    val accounts = sessionMgr.getRegisteredAccounts().map {
                        if (it.phone == phone) it.copy(balanceBDT = newBalance) else it
                    }
                    sessionMgr.saveRegisteredAccounts(accounts)
                    response.putBoolean("success", true)
                }

                "GET_ALL_DATA" -> {
                    val data = SharedDataStore.exportAllDataJson(ctx)
                    response.putString("json_payload", data)
                }

                "SAVE_ALL_DATA" -> {
                    val json = extras?.getString("json_payload")
                    if (!json.isNullOrEmpty()) {
                        val success = SharedDataStore.importAllDataJson(ctx, json)
                        AdminManager.getInstance(ctx).reloadFromStorage()
                        response.putBoolean("success", success)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling call '$method': ${e.message}")
        }

        return response
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
