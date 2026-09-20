package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Manages persistent user session, authentication cookies/tokens,
 * and profile state so users stay logged in indefinitely without
 * having to re-authenticate or re-register.
 */
class SessionManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        SharedDataStore.getSessionPrefs(appContext)

    companion object {
        private const val PREF_NAME = "snx_session_cookies_prefs"

        // Session & Cookie keys
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_SESSION_COOKIE_TOKEN = "key_session_cookie_token"
        private const val KEY_SESSION_CREATED_AT = "key_session_created_at"

        // User profile keys
        private const val KEY_USERNAME = "key_username"
        private const val KEY_PHONE = "key_phone"
        private const val KEY_EMAIL = "key_email"
        private const val KEY_BALANCE_BITS = "key_balance_bits"
        private const val KEY_VIP_LEVEL = "key_vip_level"
        private const val KEY_REFERRAL_CODE = "key_referral_code"
        private const val KEY_TOTAL_DEPOSITED_BITS = "key_total_deposited_bits"
        private const val KEY_TOTAL_WITHDRAWN_BITS = "key_total_withdrawn_bits"
        private const val KEY_TOTAL_INVITED = "key_total_invited"
        private const val KEY_FRIENDS_WON_BITS = "key_friends_won_bits"
        private const val KEY_EARNED_COMMISSION_BITS = "key_earned_commission_bits"
        private const val KEY_PENDING_COUPON = "key_pending_coupon"
        private const val KEY_LAST_DAILY_SPIN_DATE = "key_last_daily_spin_date"
        private const val KEY_LAST_DAILY_SPIN_TIMESTAMP = "key_last_daily_spin_timestamp"
        private const val KEY_LAST_WEEKLY_CASHBACK_DATE = "key_last_weekly_cashback_date"
        private const val KEY_USER_STATUS = "key_user_status"

        // Registered user registry key
        private const val KEY_REGISTERED_USERS_JSON = "key_registered_users_json"

        // App settings keys
        private const val KEY_APP_LANGUAGE = "key_app_language"
        private const val KEY_TRANSACTIONS_JSON = "key_transactions_json"
    }

    /**
     * Saves or updates the active user session and profile data.
     */
    fun saveUserSession(profile: UserProfile) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, profile.isLoggedIn)

            if (profile.isLoggedIn) {
                // Ensure a persistent cookie / session token exists
                if (!prefs.contains(KEY_SESSION_COOKIE_TOKEN) || prefs.getString(KEY_SESSION_COOKIE_TOKEN, "").isNullOrEmpty()) {
                    val token = "snx_cookie_session_" + UUID.randomUUID().toString().replace("-", "")
                    putString(KEY_SESSION_COOKIE_TOKEN, token)
                    putLong(KEY_SESSION_CREATED_AT, System.currentTimeMillis())
                }
            }

            putString(KEY_USERNAME, profile.username)
            putString(KEY_PHONE, profile.phone)
            putString(KEY_EMAIL, profile.email)
            putLong(KEY_BALANCE_BITS, java.lang.Double.doubleToRawLongBits(profile.balanceBDT))
            putString(KEY_VIP_LEVEL, profile.vipLevel)
            putString(KEY_REFERRAL_CODE, profile.referralCode)
            putLong(KEY_TOTAL_DEPOSITED_BITS, java.lang.Double.doubleToRawLongBits(profile.totalDeposited))
            putLong(KEY_TOTAL_WITHDRAWN_BITS, java.lang.Double.doubleToRawLongBits(profile.totalWithdrawn))
            putInt(KEY_TOTAL_INVITED, profile.totalInvitedCount)
            putLong(KEY_FRIENDS_WON_BITS, java.lang.Double.doubleToRawLongBits(profile.friendsTotalWonBDT))
            putLong(KEY_EARNED_COMMISSION_BITS, java.lang.Double.doubleToRawLongBits(profile.earnedCommissionBDT))
            // Only preserve pending coupon if user actually has real deposit history of ৳1,000+
            if (profile.totalDeposited >= 1000.0) {
                putString(KEY_PENDING_COUPON, profile.pendingCouponCode)
            } else {
                remove(KEY_PENDING_COUPON)
            }
            putString(KEY_LAST_DAILY_SPIN_DATE, profile.lastDailySpinDate)
            putLong(KEY_LAST_DAILY_SPIN_TIMESTAMP, profile.lastDailySpinTimestamp)
            putString(KEY_LAST_WEEKLY_CASHBACK_DATE, profile.lastWeeklyCashbackClaimDate)
            putString(KEY_USER_STATUS, profile.status)
            apply()
        }
        SharedDataStore.broadcastAndSync(appContext)
    }

    /**
     * Retrieves the persisted user session and profile.
     */
    fun getUserSession(): UserProfile {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val phone = prefs.getString(KEY_PHONE, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val balanceBDT = if (!isLoggedIn) 0.0 else java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_BALANCE_BITS, java.lang.Double.doubleToRawLongBits(0.0))
        )
        val vipLevel = prefs.getString(KEY_VIP_LEVEL, "VIP 1") ?: "VIP 1"
        val referralCode = prefs.getString(KEY_REFERRAL_CODE, "SNX777VIP") ?: "SNX777VIP"
        val totalDeposited = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_TOTAL_DEPOSITED_BITS, java.lang.Double.doubleToRawLongBits(0.0))
        )
        val totalWithdrawn = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_TOTAL_WITHDRAWN_BITS, java.lang.Double.doubleToRawLongBits(0.0))
        )
        val totalInvited = prefs.getInt(KEY_TOTAL_INVITED, 0)
        val friendsWon = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_FRIENDS_WON_BITS, java.lang.Double.doubleToRawLongBits(0.0))
        )
        val earnedCommission = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_EARNED_COMMISSION_BITS, java.lang.Double.doubleToRawLongBits(0.0))
        )
        // Vouchers require real deposit of ৳1,000+; otherwise never auto-saved
        val pendingCoupon = if (totalDeposited >= 1000.0) prefs.getString(KEY_PENDING_COUPON, null) else null
        val lastDailySpinDate = prefs.getString(KEY_LAST_DAILY_SPIN_DATE, null)
        val lastDailySpinTimestamp = prefs.getLong(KEY_LAST_DAILY_SPIN_TIMESTAMP, 0L)
        val lastWeeklyCashbackClaimDate = prefs.getString(KEY_LAST_WEEKLY_CASHBACK_DATE, null)
        val userStatus = prefs.getString(KEY_USER_STATUS, "ACTIVE") ?: "ACTIVE"

        return UserProfile(
            username = username,
            phone = phone,
            email = email,
            balanceBDT = balanceBDT,
            vipLevel = vipLevel,
            referralCode = referralCode,
            isLoggedIn = isLoggedIn,
            totalDeposited = totalDeposited,
            totalWithdrawn = totalWithdrawn,
            totalInvitedCount = totalInvited,
            friendsTotalWonBDT = friendsWon,
            earnedCommissionBDT = earnedCommission,
            pendingCouponCode = pendingCoupon,
            lastDailySpinDate = lastDailySpinDate,
            lastDailySpinTimestamp = lastDailySpinTimestamp,
            lastWeeklyCashbackClaimDate = lastWeeklyCashbackClaimDate,
            status = userStatus
        )
    }

    /**
     * Explicit user logout: Marks isLoggedIn as false and clears active session token,
     * while retaining username/phone for easy re-login if desired.
     */
    fun clearSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_SESSION_COOKIE_TOKEN)
            remove(KEY_SESSION_CREATED_AT)
            apply()
        }
    }

    /**
     * Checks if an active persistent session cookie exists.
     */
    fun hasActiveSession(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) &&
                !prefs.getString(KEY_SESSION_COOKIE_TOKEN, null).isNullOrEmpty()
    }

    /**
     * Gets the current session cookie token string.
     */
    fun getSessionCookieToken(): String? {
        return prefs.getString(KEY_SESSION_COOKIE_TOKEN, null)
    }

    /**
     * Saves user app language preference.
     */
    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language.name).apply()
    }

    /**
     * Retrieves saved app language preference.
     */
    fun getLanguage(): AppLanguage? {
        val raw = prefs.getString(KEY_APP_LANGUAGE, null) ?: return null
        return try {
            AppLanguage.valueOf(raw)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Saves transaction history to persistent storage.
     */
    fun saveTransactions(records: List<TransactionRecord>) {
        try {
            val jsonArray = JSONArray()
            records.take(50).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("type", item.type.name)
                    put("method", item.method.name)
                    put("amount", item.amount)
                    put("accountNo", item.accountNo)
                    put("trxId", item.trxId)
                    put("status", item.status.name)
                    put("timeFormatted", item.timeFormatted)
                    put("username", item.username)
                    put("userPhone", item.userPhone)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_TRANSACTIONS_JSON, jsonArray.toString()).apply()
            SharedDataStore.broadcastAndSync(appContext)
        } catch (_: Exception) {}
    }

    /**
     * Retrieves transaction history from persistent storage.
     */
    fun getTransactions(): List<TransactionRecord>? {
        val raw = prefs.getString(KEY_TRANSACTIONS_JSON, null) ?: return null
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<TransactionRecord>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TransactionRecord(
                        id = obj.getString("id"),
                        type = TransactionType.valueOf(obj.getString("type")),
                        method = PaymentMethod.valueOf(obj.getString("method")),
                        amount = obj.getDouble("amount"),
                        accountNo = obj.getString("accountNo"),
                        trxId = obj.getString("trxId"),
                        status = TransactionStatus.valueOf(obj.getString("status")),
                        timeFormatted = obj.getString("timeFormatted"),
                        username = obj.optString("username", ""),
                        userPhone = obj.optString("userPhone", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Checks if a phone number or email is already registered by any user.
     */
    fun isPhoneOrEmailRegistered(phone: String, email: String): Boolean {
        val cleanPhone = phone.trim().replace(Regex("[^0-9]"), "")
        val cleanEmail = email.trim().lowercase()

        val users = getRegisteredAccounts()
        return users.any { user ->
            val userPhone = user.phone.trim().replace(Regex("[^0-9]"), "")
            val userEmail = user.email.trim().lowercase()
            val phoneMatches = cleanPhone.isNotEmpty() && userPhone.isNotEmpty() &&
                    (cleanPhone == userPhone || cleanPhone.endsWith(userPhone) || userPhone.endsWith(cleanPhone))
            val emailMatches = cleanEmail.isNotEmpty() && userEmail.isNotEmpty() && cleanEmail == userEmail
            phoneMatches || emailMatches
        }
    }

    /**
     * Saves a new registered account to the persistent user registry.
     */
    fun saveRegisteredAccount(account: RegisteredAccount) {
        val current = getRegisteredAccounts().toMutableList()
        current.removeAll {
            (it.phone.isNotBlank() && it.phone == account.phone) ||
            (it.email.isNotBlank() && it.email.equals(account.email, ignoreCase = true))
        }
        current.add(account)
        saveRegisteredAccountsList(current)
    }

    /**
     * Updates user's registered phone number.
     */
    fun updateUserPhone(oldPhone: String, newPhone: String, pass: String): Pair<Boolean, String> {
        val cleanNewPhone = newPhone.trim()
        if (cleanNewPhone.length < 10) {
            return Pair(false, "সঠিক ১১ ডিজিটের মোবাইল নম্বর প্রদান করুন")
        }

        val users = getRegisteredAccounts().toMutableList()
        val index = users.indexOfFirst {
            it.phone == oldPhone || (it.phone.isNotBlank() && oldPhone.isNotBlank() && (it.phone.endsWith(oldPhone) || oldPhone.endsWith(it.phone)))
        }

        if (index != -1) {
            val user = users[index]
            if (pass.isNotBlank() && user.password != pass) {
                return Pair(false, "অ্যাকাউন্টের বর্তমান পাসওয়ার্ড সঠিক নয়")
            }
            if (isPhoneOrEmailRegistered(cleanNewPhone, "") && cleanNewPhone != user.phone) {
                return Pair(false, "এই নতুন মোবাইল নম্বরটি ইতিমধ্যে অন্য অ্যাকাউন্টে ব্যবহৃত হচ্ছে")
            }
            val updatedUser = user.copy(phone = cleanNewPhone)
            users[index] = updatedUser
            saveRegisteredAccountsList(users)
        }

        val session = getUserSession()
        if (session.isLoggedIn) {
            saveUserSession(session.copy(phone = cleanNewPhone))
        }
        return Pair(true, "মোবাইল নম্বর সফলভাবে পরিবর্তন করা হয়েছে!")
    }

    /**
     * Updates user's registered password.
     */
    fun updateUserPassword(phone: String, oldPass: String, newPass: String): Pair<Boolean, String> {
        if (newPass.length < 4) {
            return Pair(false, "নতুন পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে")
        }

        val users = getRegisteredAccounts().toMutableList()
        val index = users.indexOfFirst {
            it.phone == phone || (it.phone.isNotBlank() && phone.isNotBlank() && (it.phone.endsWith(phone) || phone.endsWith(it.phone)))
        }

        if (index != -1) {
            val user = users[index]
            if (oldPass.isNotBlank() && user.password != oldPass) {
                return Pair(false, "বর্তমান পাসওয়ার্ড সঠিক নয়")
            }
            val updatedUser = user.copy(password = newPass)
            users[index] = updatedUser
            saveRegisteredAccountsList(users)
        }

        return Pair(true, "পাসওয়ার্ড সফলভাবে পরিবর্তন করা হয়েছে!")
    }

    /**
     * Authenticates a registered account with phone/email and password.
     */
    fun findRegisteredAccount(identifier: String, pass: String): RegisteredAccount? {
        val cleanId = identifier.trim()
        val cleanPhone = cleanId.replace(Regex("[^0-9]"), "")
        val cleanEmail = cleanId.lowercase()

        val users = getRegisteredAccounts()
        return users.firstOrNull { user ->
            val userPhone = user.phone.trim().replace(Regex("[^0-9]"), "")
            val userEmail = user.email.trim().lowercase()
            val idMatch = (cleanPhone.isNotEmpty() && userPhone.isNotEmpty() && (cleanPhone == userPhone || cleanPhone.endsWith(userPhone) || userPhone.endsWith(cleanPhone))) ||
                    (cleanEmail.isNotEmpty() && userEmail.isNotEmpty() && cleanEmail == userEmail)
            idMatch && user.password == pass
        }
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Retrieves all saved registered accounts.
     */
    fun getRegisteredAccounts(): List<RegisteredAccount> {
        val raw = prefs.getString(KEY_REGISTERED_USERS_JSON, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<RegisteredAccount>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    RegisteredAccount(
                        username = obj.getString("username"),
                        phone = obj.getString("phone"),
                        email = obj.optString("email", ""),
                        password = obj.getString("password"),
                        balanceBDT = obj.optDouble("balanceBDT", 0.0),
                        totalDeposited = obj.optDouble("totalDeposited", 0.0),
                        totalWithdrawn = obj.optDouble("totalWithdrawn", 0.0),
                        vipLevel = obj.optString("vipLevel", "VIP 1"),
                        registeredDate = obj.optString("registeredDate", "Live Registered"),
                        status = obj.optString("status", "ACTIVE")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveRegisteredAccounts(accounts: List<RegisteredAccount>) {
        saveRegisteredAccountsList(accounts)
    }

    private fun saveRegisteredAccountsList(accounts: List<RegisteredAccount>) {
        try {
            val jsonArray = JSONArray()
            accounts.forEach { acc ->
                val obj = JSONObject().apply {
                    put("username", acc.username)
                    put("phone", acc.phone)
                    put("email", acc.email)
                    put("password", acc.password)
                    put("balanceBDT", acc.balanceBDT)
                    put("totalDeposited", acc.totalDeposited)
                    put("totalWithdrawn", acc.totalWithdrawn)
                    put("vipLevel", acc.vipLevel)
                    put("registeredDate", acc.registeredDate)
                    put("status", acc.status)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_REGISTERED_USERS_JSON, jsonArray.toString()).apply()
            SharedDataStore.broadcastAndSync(appContext)
        } catch (_: Exception) {}
    }
}

data class RegisteredAccount(
    val username: String,
    val phone: String,
    val email: String,
    val password: String,
    val balanceBDT: Double = 0.0,
    val totalDeposited: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val vipLevel: String = "VIP 1",
    val registeredDate: String = "Live Registered",
    val status: String = "ACTIVE"
)
