package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.model.PaymentMethod
import com.example.model.TransactionStatus
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
