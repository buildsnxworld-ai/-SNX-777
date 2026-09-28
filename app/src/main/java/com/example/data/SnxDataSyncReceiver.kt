package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.model.PaymentMethod
import com.example.model.TransactionRecord
import com.example.model.TransactionStatus
import com.example.model.TransactionType
import org.json.JSONObject

/**
 * BroadcastReceiver triggered when either the Game app or Admin app modifies data,
 * instantly applying updates in real time with high reliability.
 */
class SnxDataSyncReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SnxDataSyncReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SharedDataStore.ACTION_DATA_SYNC) {
            val actionType = intent.getStringExtra("action_type") ?: ""
            val dataPayload = intent.getStringExtra("data_payload") ?: ""
            val fullPayload = intent.getStringExtra("json_payload") ?: ""

            try {
                when (actionType) {
                    "UPDATE_DEPOSIT_STATUS" -> {
                        if (dataPayload.isNotEmpty()) {
                            try {
                                val obj = if (dataPayload.startsWith("{")) JSONObject(dataPayload) else JSONObject().apply { put("request_id", dataPayload); put("status", "APPROVED") }
                                val reqId = obj.optString("request_id")
                                val newStatus = obj.optString("status", "APPROVED")
                                val amount = obj.optDouble("amount", 0.0)
                                val phone = obj.optString("user_phone", "")
                                val username = obj.optString("username", "")
                                val reason = obj.optString("reason", "")
                                val trxId = obj.optString("trxId", "")

                                val statusEnum = try { TransactionStatus.valueOf(newStatus) } catch (_: Exception) { TransactionStatus.APPROVED }
                                AdminManager.getInstance(context).updateDepositStatusLocally(reqId, statusEnum, reason)

                                if (newStatus == "APPROVED" && amount > 0.0) {
                                    val sessionMgr = SessionManager(context)
                                    val currentSession = sessionMgr.getUserSession()
                                    val pMatch = phone.isNotBlank() && (currentSession.phone == phone || currentSession.phone.endsWith(phone) || phone.endsWith(currentSession.phone))
                                    val uMatch = username.isNotBlank() && currentSession.username.equals(username, ignoreCase = true)
                                    if (currentSession.isLoggedIn && (pMatch || uMatch || phone.isBlank())) {
                                        sessionMgr.saveUserSession(
                                            currentSession.copy(
                                                balanceBDT = currentSession.balanceBDT + amount,
                                                totalDeposited = currentSession.totalDeposited + amount
                                            )
                                        )
                                    }

                                    // Update registered accounts
                                    val currentAccounts = sessionMgr.getRegisteredAccounts()
                                    val updatedAccounts = currentAccounts.map { acc ->
                                        if (acc.phone == phone || acc.username.equals(username, ignoreCase = true)) {
                                            acc.copy(
                                                balanceBDT = acc.balanceBDT + amount,
                                                totalDeposited = acc.totalDeposited + amount
                                            )
                                        } else acc
                                    }
                                    sessionMgr.saveRegisteredAccounts(updatedAccounts)

                                    // Update local transactions
                                    val txs = sessionMgr.getTransactions() ?: emptyList()
                                    val updatedTxs = txs.map { tx ->
                                        val matchTrx = trxId.isNotBlank() && tx.trxId.equals(trxId, ignoreCase = true)
                                        if (tx.type == TransactionType.DEPOSIT && (tx.id == reqId || matchTrx || (tx.status == TransactionStatus.PENDING && (phone.isBlank() || tx.userPhone == phone)))) {
                                            tx.copy(status = TransactionStatus.APPROVED)
                                        } else tx
                                    }
                                    sessionMgr.saveTransactions(updatedTxs)
                                } else if (newStatus == "REJECTED") {
                                    val sessionMgr = SessionManager(context)
                                    val txs = sessionMgr.getTransactions() ?: emptyList()
                                    val updatedTxs = txs.map { tx ->
                                        val matchTrx = trxId.isNotBlank() && tx.trxId.equals(trxId, ignoreCase = true)
                                        if (tx.type == TransactionType.DEPOSIT && (tx.id == reqId || matchTrx || (tx.status == TransactionStatus.PENDING && (phone.isBlank() || tx.userPhone == phone)))) {
                                            tx.copy(status = TransactionStatus.REJECTED, rejectReason = reason)
                                        } else tx
                                    }
                                    sessionMgr.saveTransactions(updatedTxs)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                    "NEW_DEPOSIT" -> {
                        if (dataPayload.isNotEmpty()) {
                            val obj = JSONObject(dataPayload)
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
                            AdminManager.getInstance(context).insertIncomingDeposit(req)
                        }
                    }
                    "NEW_WITHDRAW" -> {
                        if (dataPayload.isNotEmpty()) {
                            val obj = JSONObject(dataPayload)
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
                            AdminManager.getInstance(context).insertIncomingWithdrawal(req)
                        }
                    }
                    "UPDATE_PAYMENT_NUMBERS" -> {
                        if (dataPayload.isNotEmpty()) {
                            val prefs = SharedDataStore.getAdminPrefs(context)
                            prefs.edit().putString("key_payment_numbers_json", dataPayload).commit()
                            AdminManager.getInstance(context).reloadPaymentNumbers()
                        }
                    }
                    "UPDATE_GAMES" -> {
                        if (dataPayload.isNotEmpty()) {
                            val prefs = SharedDataStore.getAdminPrefs(context)
                            prefs.edit().putString("key_games_list_json", dataPayload).apply()
                        }
                    }
                    "UPDATE_SITE_CONFIG" -> {
                        if (dataPayload.isNotEmpty()) {
                            val prefs = SharedDataStore.getAdminPrefs(context)
                            prefs.edit().putString("key_site_config_json", dataPayload).apply()
                        }
                    }
                    "UPDATE_USER_STATUS" -> {
                        if (dataPayload.isNotEmpty()) {
                            try {
                                val obj = JSONObject(dataPayload)
                                val phone = obj.getString("phone")
                                val status = obj.getString("status")
                                val sessionMgr = SessionManager(context)
                                val currentSession = sessionMgr.getUserSession()
                                if (currentSession.phone == phone) {
                                    sessionMgr.saveUserSession(currentSession.copy(status = status))
                                }
                                val accounts = sessionMgr.getRegisteredAccounts().map {
                                    if (it.phone == phone) it.copy(status = status) else it
                                }
                                sessionMgr.saveRegisteredAccounts(accounts)
                            } catch (_: Exception) {}
                        }
                    }
                    "NEW_USER_REGISTERED" -> {
                        if (dataPayload.isNotEmpty()) {
                            val obj = JSONObject(dataPayload)
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
                            SessionManager(context).saveRegisteredAccount(acc)
                        }
                    }
                }

                if (fullPayload.isNotEmpty()) {
                    SharedDataStore.importAllDataJson(context, fullPayload)
                } else {
                    SharedDataStore.pullFromOtherApp(context)
                }

                AdminManager.getInstance(context).reloadFromStorage()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling broadcast: ${e.message}")
            }
        }
    }
}
