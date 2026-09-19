package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Data class representing a dynamic bKash / Nagad deposit number in the Admin Panel.
 */
data class AdminPaymentNumber(
    val id: String = UUID.randomUUID().toString(),
    val method: PaymentMethod,
    val number: String,
    val agentLabel: String,
    val isActive: Boolean = true,
    val createdAt: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
)

/**
 * Data class representing a Deposit request awaiting admin review.
 */
data class DepositRequest(
    val id: String,
    val username: String,
    val userPhone: String,
    val method: PaymentMethod,
    val amount: Double,
    val agentNumberUsed: String,
    val userAccountNo: String,
    val trxId: String,
    var status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
    val reviewNote: String = ""
)

/**
 * Data class representing a Withdrawal request awaiting admin review.
 */
data class WithdrawalRequest(
    val id: String,
    val username: String,
    val userPhone: String,
    val method: PaymentMethod,
    val amount: Double,
    val targetAccountNo: String,
    val trxId: String = "WD" + (100000..999999).random(),
    var status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
    val reviewNote: String = ""
)

/**
 * Data class representing site and game customization options.
 */
data class SiteCustomization(
    val siteName: String = "SNX WORLD",
    val marqueeTicker: String = "📢 প্রিয় গ্রাহক, ডিপোজিট করার পূর্বে প্রতিবার এডমিন প্যানেল কর্তৃক নির্ধারিত আপডেট নম্বর চেক করে টাকা পাঠান।",
    val bannerNotice: String = "🔥 শুভ স্বাগতম! বিকাশ ও নগদে ডিপোজিটে ৫% ইনস্ট্যান্ট বোনাস! নতুন গেম Spribe Aviator লাইভ! 🔥",
    val aviatorGameTitle: String = "SPRIBE AVIATOR",
    val isMaintenanceMode: Boolean = false
)

/**
 * Central singleton manager for SNX ADMIN PANEL:
 * - Secure login check (Password: "ANX 20")
 * - 10 bKash & 10 Nagad dynamic numbers management
 * - Deposit & Withdraw approval pipeline
 * - User management & balance adjustments
 * - Site customization
 */
class AdminManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        SharedDataStore.getAdminPrefs(appContext)
    private val sessionManager = SessionManager(appContext)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val PREF_ADMIN = "snx_admin_panel_prefs"
        private const val KEY_ADMIN_LOGGED_IN = "key_admin_logged_in"
        private const val KEY_PAYMENT_NUMBERS_JSON = "key_payment_numbers_json"
        private const val KEY_DEPOSIT_REQUESTS_JSON = "key_deposit_requests_json"
        private const val KEY_WITHDRAW_REQUESTS_JSON = "key_withdraw_requests_json"
        private const val KEY_SITE_CONFIG_JSON = "key_site_config_json"
        private const val KEY_GAMES_LIST_JSON = "key_games_list_json"
        private const val KEY_BKASH_INDEX = "key_bkash_rr_index"
        private const val KEY_NAGAD_INDEX = "key_nagad_rr_index"

        const val ADMIN_DEFAULT_USER = "SNX"
        const val ADMIN_SECRET_PASS = "ANX 20"

        @Volatile
        private var instance: AdminManager? = null

        fun getInstance(context: Context): AdminManager {
            return instance ?: synchronized(this) {
                instance ?: AdminManager(context).also { instance = it }
            }
        }
    }

    // State Flows
    private val _isAdminLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_ADMIN_LOGGED_IN, false))
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _paymentNumbers = MutableStateFlow<List<AdminPaymentNumber>>(emptyList())
    val paymentNumbers: StateFlow<List<AdminPaymentNumber>> = _paymentNumbers.asStateFlow()

    private val _depositRequests = MutableStateFlow<List<DepositRequest>>(emptyList())
    val depositRequests: StateFlow<List<DepositRequest>> = _depositRequests.asStateFlow()

    private val _withdrawalRequests = MutableStateFlow<List<WithdrawalRequest>>(emptyList())
    val withdrawalRequests: StateFlow<List<WithdrawalRequest>> = _withdrawalRequests.asStateFlow()

    private val _siteConfig = MutableStateFlow(SiteCustomization())
    val siteConfig: StateFlow<SiteCustomization> = _siteConfig.asStateFlow()

    private val _gamesList = MutableStateFlow<List<GameItem>>(emptyList())
    val gamesList: StateFlow<List<GameItem>> = _gamesList.asStateFlow()

    private val _registeredUsers = MutableStateFlow<List<RegisteredAccount>>(emptyList())
    val registeredUsers: StateFlow<List<RegisteredAccount>> = _registeredUsers.asStateFlow()

    init {
        loadPaymentNumbers()
        loadDepositRequests()
        loadWithdrawRequests()
        loadSiteConfig()
        loadGames()
        loadRegisteredUsers()

        coroutineScope.launch {
            SharedDataStore.syncFlow.collect {
                reloadFromStorage()
            }
        }
    }

    fun reloadFromStorage() {
        loadPaymentNumbers()
        loadDepositRequests()
        loadWithdrawRequests()
        loadSiteConfig()
        loadGames()
        loadRegisteredUsers()
    }

    // ==========================================
    // 1. ADMIN AUTHENTICATION
    // ==========================================
    fun loginAdmin(username: String, password: String): Boolean {
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()
        val userMatches = trimmedUser.equals("SNX", ignoreCase = true) ||
                trimmedUser.equals("SNX ADMIN PANEL", ignoreCase = true)
        val passMatches = trimmedPass.equals(ADMIN_SECRET_PASS, ignoreCase = true) ||
                trimmedPass == "ANX 20"
        if (userMatches && passMatches) {
            _isAdminLoggedIn.value = true
            prefs.edit().putBoolean(KEY_ADMIN_LOGGED_IN, true).apply()
            return true
        }
        return false
    }

    fun loginAdmin(password: String): Boolean {
        return loginAdmin("SNX", password)
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        prefs.edit().putBoolean(KEY_ADMIN_LOGGED_IN, false).apply()
    }

    // ==========================================
    // 2. DYNAMIC PAYMENT NUMBERS (10 bKash + 10 Nagad)
    // ==========================================
    private fun loadPaymentNumbers() {
        val raw = prefs.getString(KEY_PAYMENT_NUMBERS_JSON, null)
        if (raw.isNullOrEmpty() || raw == "[]") {
            val defaultList = createDefaultPaymentNumbers()
            _paymentNumbers.value = defaultList
            savePaymentNumbers(defaultList)
        } else {
            try {
                val array = JSONArray(raw)
                val list = mutableListOf<AdminPaymentNumber>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val num = obj.optString("number", obj.optString("phone", "")).trim()
                    if (num.isBlank()) continue
                    val methodStr = obj.optString("method", "BKASH").uppercase()
                    val method = if (methodStr.contains("NAGAD")) PaymentMethod.NAGAD else PaymentMethod.BKASH
                    val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                    val label = obj.optString("agentLabel", obj.optString("agent_label", "Agent ${num.takeLast(4)}"))
                    val active = obj.optBoolean("isActive", obj.optBoolean("is_active", true))
                    val created = obj.optString("createdAt", obj.optString("created_at", ""))
                    list.add(
                        AdminPaymentNumber(
                            id = id,
                            method = method,
                            number = num,
                            agentLabel = label,
                            isActive = active,
                            createdAt = created
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    _paymentNumbers.value = list
                } else {
                    val defaultList = createDefaultPaymentNumbers()
                    _paymentNumbers.value = defaultList
                }
            } catch (_: Exception) {
                if (_paymentNumbers.value.isEmpty()) {
                    _paymentNumbers.value = createDefaultPaymentNumbers()
                }
            }
        }
    }

    /**
     * Instantly updates payment numbers from cloud payload and notifies state flows.
     */
    fun applyPaymentNumbersFromCloud(jsonStr: String): Boolean {
        if (jsonStr.isBlank() || jsonStr == "[]") return false
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<AdminPaymentNumber>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val num = obj.optString("number", obj.optString("phone", "")).trim()
                if (num.isBlank()) continue
                val methodStr = obj.optString("method", "BKASH").uppercase()
                val method = if (methodStr.contains("NAGAD")) PaymentMethod.NAGAD else PaymentMethod.BKASH
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val label = obj.optString("agentLabel", obj.optString("agent_label", "Agent ${num.takeLast(4)}"))
                val active = obj.optBoolean("isActive", obj.optBoolean("is_active", true))
                val created = obj.optString("createdAt", obj.optString("created_at", ""))
                list.add(
                    AdminPaymentNumber(
                        id = id,
                        method = method,
                        number = num,
                        agentLabel = label,
                        isActive = active,
                        createdAt = created
                    )
                )
            }
            if (list.isNotEmpty()) {
                _paymentNumbers.value = list
                prefs.edit().putString(KEY_PAYMENT_NUMBERS_JSON, jsonStr).apply()
                return true
            }
        } catch (_: Exception) {}
        return false
    }

    private fun savePaymentNumbers(list: List<AdminPaymentNumber>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("method", item.method.name)
                    put("number", item.number)
                    put("agentLabel", item.agentLabel)
                    put("isActive", item.isActive)
                    put("createdAt", item.createdAt)
                }
                array.put(obj)
            }
            val jsonStr = array.toString()
            prefs.edit().putString(KEY_PAYMENT_NUMBERS_JSON, jsonStr).apply()
            SharedDataStore.notifyPaymentNumbersChanged(appContext, jsonStr)
            SharedDataStore.broadcastAndSync(appContext)
            SnxCloudSyncService.pushPaymentNumbersToCloud(appContext, jsonStr)
        } catch (_: Exception) {}
    }

    fun addPaymentNumber(method: PaymentMethod, number: String, label: String) {
        val cleanNumber = number.trim()
        val item = AdminPaymentNumber(
            method = method,
            number = cleanNumber,
            agentLabel = if (label.isNotBlank()) label.trim() else "Agent ${cleanNumber.takeLast(4)}"
        )
        val updated = listOf(item) + _paymentNumbers.value
        _paymentNumbers.value = updated
        savePaymentNumbers(updated)
    }

    fun updatePaymentNumber(id: String, newNumber: String, newLabel: String, isActive: Boolean) {
        val updated = _paymentNumbers.value.map {
            if (it.id == id) {
                it.copy(
                    number = newNumber.trim(),
                    agentLabel = newLabel.trim(),
                    isActive = isActive
                )
            } else it
        }
        _paymentNumbers.value = updated
        savePaymentNumbers(updated)
    }

    fun togglePaymentNumberStatus(id: String) {
        val updated = _paymentNumbers.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        _paymentNumbers.value = updated
        savePaymentNumbers(updated)
    }

    fun deletePaymentNumber(id: String) {
        val updated = _paymentNumbers.value.filterNot { it.id == id }
        _paymentNumbers.value = updated
        savePaymentNumbers(updated)
    }

    /**
     * Gets a dynamic deposit number for user deposit screen using round-robin / random distribution.
     */
    fun getActiveDepositNumber(method: PaymentMethod): String {
        val activeList = _paymentNumbers.value.filter { it.method == method && it.isActive && it.number.isNotBlank() }
        if (activeList.isEmpty()) {
            return if (method == PaymentMethod.BKASH) "01712-348901" else "01822-334455"
        }
        // Round robin distribution
        val key = if (method == PaymentMethod.BKASH) KEY_BKASH_INDEX else KEY_NAGAD_INDEX
        val currentIndex = prefs.getInt(key, 0)
        val selected = activeList[currentIndex % activeList.size]
        prefs.edit().putInt(key, (currentIndex + 1) % activeList.size).apply()
        return selected.number
    }

    private fun createDefaultPaymentNumbers(): List<AdminPaymentNumber> {
        val list = mutableListOf<AdminPaymentNumber>()
        // 10 bKash numbers
        val bkashNumbers = listOf(
            "01712-348901" to "bKash Agent 01 (Dhaka)",
            "01823-456712" to "bKash Agent 02 (Banani)",
            "01934-567823" to "bKash Agent 03 (Gulshan)",
            "01345-678934" to "bKash Agent 04 (Chittagong)",
            "01756-789045" to "bKash Agent 05 (Sylhet)",
            "01867-890156" to "bKash Agent 06 (Rajshahi)",
            "01978-901267" to "bKash Agent 07 (Khulna)",
            "01389-012378" to "bKash Agent 08 (Barisal)",
            "01790-123489" to "bKash Agent 09 (Rangpur)",
            "01801-234590" to "bKash Agent 10 (Comilla)"
        )
        bkashNumbers.forEach { (num, lbl) ->
            list.add(AdminPaymentNumber(method = PaymentMethod.BKASH, number = num, agentLabel = lbl))
        }

        // 10 Nagad numbers
        val nagadNumbers = listOf(
            "01711-223344" to "Nagad Merchant 01 (Motijheel)",
            "01822-334455" to "Nagad Merchant 02 (Dhanmondi)",
            "01933-445566" to "Nagad Merchant 03 (Uttara)",
            "01344-556677" to "Nagad Merchant 04 (Mirpur)",
            "01755-667788" to "Nagad Merchant 05 (Gazipur)",
            "01866-778899" to "Nagad Merchant 06 (Narayanganj)",
            "01977-889900" to "Nagad Merchant 07 (Bogra)",
            "01388-990011" to "Nagad Merchant 08 (Jessore)",
            "01799-001122" to "Nagad Merchant 09 (Mymensingh)",
            "01810-112233" to "Nagad Merchant 10 (Cox's Bazar)"
        )
        nagadNumbers.forEach { (num, lbl) ->
            list.add(AdminPaymentNumber(method = PaymentMethod.NAGAD, number = num, agentLabel = lbl))
        }

        return list
    }

    // ==========================================
    // 3. DEPOSIT APPROVAL PIPELINE
    // ==========================================
    private fun loadDepositRequests() {
        val raw = prefs.getString(KEY_DEPOSIT_REQUESTS_JSON, null)
        if (raw.isNullOrEmpty()) {
            val defaults = listOf(
                DepositRequest(
                    id = "DEP8841",
                    username = "Sakib77",
                    userPhone = "01712-348901",
                    method = PaymentMethod.BKASH,
                    amount = 1000.0,
                    agentNumberUsed = "01712-345678",
                    userAccountNo = "01712-348901",
                    trxId = "BK7823901X",
                    status = TransactionStatus.PENDING,
                    createdAt = "13 Sep, 10:45 AM"
                ),
                DepositRequest(
                    id = "DEP7732",
                    username = "RahimPro",
                    userPhone = "01823-456789",
                    method = PaymentMethod.NAGAD,
                    amount = 500.0,
                    agentNumberUsed = "01711-223344",
                    userAccountNo = "01823-456789",
                    trxId = "NG9012348A",
                    status = TransactionStatus.PENDING,
                    createdAt = "13 Sep, 11:15 AM"
                ),
                DepositRequest(
                    id = "DEP6621",
                    username = "TanvirGamer",
                    userPhone = "01934-567890",
                    method = PaymentMethod.BKASH,
                    amount = 2500.0,
                    agentNumberUsed = "01823-456789",
                    userAccountNo = "01934-567890",
                    trxId = "BK1122334Y",
                    status = TransactionStatus.APPROVED,
                    createdAt = "12 Sep, 04:30 PM",
                    reviewNote = "Approved by Admin"
                )
            )
            _depositRequests.value = defaults
            saveDepositRequests(defaults)
            return
        }
        try {
            val array = JSONArray(raw)
            val list = mutableListOf<DepositRequest>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    DepositRequest(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        userPhone = obj.getString("userPhone"),
                        method = PaymentMethod.valueOf(obj.getString("method")),
                        amount = obj.getDouble("amount"),
                        agentNumberUsed = obj.getString("agentNumberUsed"),
                        userAccountNo = obj.getString("userAccountNo"),
                        trxId = obj.getString("trxId"),
                        status = TransactionStatus.valueOf(obj.getString("status")),
                        createdAt = obj.getString("createdAt"),
                        reviewNote = obj.optString("reviewNote", "")
                    )
                )
            }
            _depositRequests.value = list
        } catch (_: Exception) {}
    }

    fun insertIncomingDeposit(req: DepositRequest) {
        val current = _depositRequests.value
        if (current.none { it.id == req.id || (it.trxId.isNotBlank() && it.trxId.equals(req.trxId, ignoreCase = true)) }) {
            val updated = listOf(req) + current
            _depositRequests.value = updated
            saveDepositRequestsDirect(updated)
        }
    }

    fun insertIncomingWithdrawal(req: WithdrawalRequest) {
        val current = _withdrawalRequests.value
        if (current.none { it.id == req.id || (it.trxId.isNotBlank() && it.trxId.equals(req.trxId, ignoreCase = true)) }) {
            val updated = listOf(req) + current
            _withdrawalRequests.value = updated
            saveWithdrawRequestsDirect(updated)
        }
    }

    fun updateDepositStatusLocally(id: String, status: TransactionStatus) {
        val updated = _depositRequests.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
        _depositRequests.value = updated
        saveDepositRequestsDirect(updated)
    }

    fun updateWithdrawStatusLocally(id: String, status: TransactionStatus) {
        val updated = _withdrawalRequests.value.map {
            if (it.id == id) it.copy(status = status) else it
        }
        _withdrawalRequests.value = updated
        saveWithdrawRequestsDirect(updated)
    }

    private fun saveDepositRequestsDirect(list: List<DepositRequest>) {
        try {
            val array = JSONArray()
            list.take(50).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("username", item.username)
                    put("userPhone", item.userPhone)
                    put("method", item.method.name)
                    put("amount", item.amount)
                    put("agentNumberUsed", item.agentNumberUsed)
                    put("userAccountNo", item.userAccountNo)
                    put("trxId", item.trxId)
                    put("status", item.status.name)
                    put("createdAt", item.createdAt)
                    put("reviewNote", item.reviewNote)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_DEPOSIT_REQUESTS_JSON, array.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun saveDepositRequests(list: List<DepositRequest>) {
        saveDepositRequestsDirect(list)
        SharedDataStore.broadcastAndSync(appContext)
        SnxCloudSyncService.pushToCloud(appContext)
    }

    fun submitUserDepositRequest(
        username: String,
        userPhone: String,
        method: PaymentMethod,
        amount: Double,
        agentNumberUsed: String,
        userAccountNo: String,
        trxId: String
    ): DepositRequest {
        val req = DepositRequest(
            id = "DEP" + (1000..9999).random(),
            username = username,
            userPhone = userPhone,
            method = method,
            amount = amount,
            agentNumberUsed = agentNumberUsed,
            userAccountNo = userAccountNo,
            trxId = trxId.uppercase(),
            status = TransactionStatus.PENDING
        )
        val updated = listOf(req) + _depositRequests.value
        _depositRequests.value = updated
        saveDepositRequests(updated)

        // Push directly to Admin app via ContentProvider IPC and Targeted Broadcast
        SharedDataStore.submitDepositToAdmin(appContext, req)
        return req
    }

    fun approveDeposit(requestId: String): Boolean {
        var approvedReq: DepositRequest? = null
        val updated = _depositRequests.value.map {
            if (it.id == requestId && it.status == TransactionStatus.PENDING) {
                approvedReq = it
                it.copy(status = TransactionStatus.APPROVED, reviewNote = "Approved by Admin")
            } else it
        }
        if (approvedReq != null) {
            _depositRequests.value = updated
            saveDepositRequests(updated)

            // Credit the user balance (+ 5% bonus)
            val req = approvedReq!!
            val bonus = if (req.amount >= 300.0) req.amount * 0.05 else 0.0
            val totalCredit = req.amount + bonus

            creditUserBalance(req.userPhone, req.username, totalCredit, req.amount)
            updateTransactionStatus(req.userPhone, req.username, TransactionType.DEPOSIT, TransactionStatus.APPROVED)

            // Push status update to Game Website app and Cloud
            SharedDataStore.notifyDepositStatusChanged(
                appContext,
                req.id,
                "APPROVED",
                totalCredit,
                req.userPhone,
                req.username
            )
            SnxCloudSyncService.pushDepositStatusUpdate(appContext, req.id, "APPROVED", "Approved by Admin")
            SnxCloudSyncService.pushToCloud(appContext)
            return true
        }
        return false
    }

    fun rejectDeposit(requestId: String, reason: String = "Invalid TrxID or unpaid"): Boolean {
        var rejectedReq: DepositRequest? = null
        val updated = _depositRequests.value.map {
            if (it.id == requestId && it.status == TransactionStatus.PENDING) {
                rejectedReq = it
                it.copy(status = TransactionStatus.REJECTED, reviewNote = reason)
            } else it
        }
        if (rejectedReq != null) {
            _depositRequests.value = updated
            saveDepositRequests(updated)
            updateTransactionStatus(rejectedReq!!.userPhone, rejectedReq!!.username, TransactionType.DEPOSIT, TransactionStatus.REJECTED)

            SharedDataStore.notifyDepositStatusChanged(
                appContext,
                rejectedReq!!.id,
                "REJECTED",
                0.0,
                rejectedReq!!.userPhone,
                rejectedReq!!.username
            )
            SnxCloudSyncService.pushDepositStatusUpdate(appContext, rejectedReq!!.id, "REJECTED", reason)
            SnxCloudSyncService.pushToCloud(appContext)
            return true
        }
        return false
    }

    // ==========================================
    // 4. WITHDRAW APPROVAL PIPELINE
    // ==========================================
    private fun loadWithdrawRequests() {
        val raw = prefs.getString(KEY_WITHDRAW_REQUESTS_JSON, null)
        if (raw.isNullOrEmpty()) {
            val defaults = listOf(
                WithdrawalRequest(
                    id = "WTH9901",
                    username = "Sakib77",
                    userPhone = "01712-348901",
                    method = PaymentMethod.BKASH,
                    amount = 500.0,
                    targetAccountNo = "01712-348901",
                    trxId = "WTH7891230A",
                    status = TransactionStatus.PENDING,
                    createdAt = "13 Sep, 11:30 AM"
                ),
                WithdrawalRequest(
                    id = "WTH8812",
                    username = "TanvirGamer",
                    userPhone = "01934-567890",
                    method = PaymentMethod.NAGAD,
                    amount = 1200.0,
                    targetAccountNo = "01934-567890",
                    trxId = "WTH6789012B",
                    status = TransactionStatus.APPROVED,
                    createdAt = "12 Sep, 07:20 PM",
                    reviewNote = "Paid via Nagad Merchant"
                )
            )
            _withdrawalRequests.value = defaults
            saveWithdrawRequests(defaults)
            return
        }
        try {
            val array = JSONArray(raw)
            val list = mutableListOf<WithdrawalRequest>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WithdrawalRequest(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        userPhone = obj.getString("userPhone"),
                        method = PaymentMethod.valueOf(obj.getString("method")),
                        amount = obj.getDouble("amount"),
                        targetAccountNo = obj.getString("targetAccountNo"),
                        trxId = obj.getString("trxId"),
                        status = TransactionStatus.valueOf(obj.getString("status")),
                        createdAt = obj.getString("createdAt"),
                        reviewNote = obj.optString("reviewNote", "")
                    )
                )
            }
            _withdrawalRequests.value = list
        } catch (_: Exception) {}
    }

    private fun saveWithdrawRequestsDirect(list: List<WithdrawalRequest>) {
        try {
            val array = JSONArray()
            list.take(50).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("username", item.username)
                    put("userPhone", item.userPhone)
                    put("method", item.method.name)
                    put("amount", item.amount)
                    put("targetAccountNo", item.targetAccountNo)
                    put("trxId", item.trxId)
                    put("status", item.status.name)
                    put("createdAt", item.createdAt)
                    put("reviewNote", item.reviewNote)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_WITHDRAW_REQUESTS_JSON, array.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun saveWithdrawRequests(list: List<WithdrawalRequest>) {
        saveWithdrawRequestsDirect(list)
        SharedDataStore.broadcastAndSync(appContext)
        SnxCloudSyncService.pushToCloud(appContext)
    }

    fun submitUserWithdrawRequest(
        username: String,
        userPhone: String,
        method: PaymentMethod,
        amount: Double,
        targetAccountNo: String
    ): WithdrawalRequest {
        val req = WithdrawalRequest(
            id = "WD" + (1000..9999).random(),
            username = username,
            userPhone = userPhone,
            method = method,
            amount = amount,
            targetAccountNo = targetAccountNo,
            status = TransactionStatus.PENDING
        )
        val updated = listOf(req) + _withdrawalRequests.value
        _withdrawalRequests.value = updated
        saveWithdrawRequests(updated)

        // Push directly to Admin app via IPC
        SharedDataStore.submitWithdrawToAdmin(appContext, req)
        return req
    }

    fun approveWithdrawal(requestId: String): Boolean {
        var approvedReq: WithdrawalRequest? = null
        val updated = _withdrawalRequests.value.map {
            if (it.id == requestId && it.status == TransactionStatus.PENDING) {
                approvedReq = it
                it.copy(status = TransactionStatus.APPROVED, reviewNote = "Dispatched & Confirmed by Admin")
            } else it
        }
        if (approvedReq != null) {
            _withdrawalRequests.value = updated
            saveWithdrawRequests(updated)
            updateTransactionStatus(approvedReq!!.userPhone, approvedReq!!.username, TransactionType.WITHDRAW, TransactionStatus.APPROVED)

            SharedDataStore.notifyWithdrawStatusChanged(
                appContext,
                approvedReq!!.id,
                "APPROVED",
                approvedReq!!.amount,
                approvedReq!!.userPhone,
                approvedReq!!.username
            )
            SnxCloudSyncService.pushWithdrawStatusUpdate(appContext, approvedReq!!.id, "APPROVED", "Dispatched & Confirmed by Admin")
            SnxCloudSyncService.pushToCloud(appContext)
            return true
        }
        return false
    }

    fun rejectWithdrawal(requestId: String, reason: String = "Rejected by Admin"): Boolean {
        var reqToRefund: WithdrawalRequest? = null
        val updated = _withdrawalRequests.value.map {
            if (it.id == requestId && it.status == TransactionStatus.PENDING) {
                reqToRefund = it
                it.copy(status = TransactionStatus.REJECTED, reviewNote = reason)
            } else it
        }
        if (reqToRefund != null) {
            _withdrawalRequests.value = updated
            saveWithdrawRequests(updated)
            // Refund the deducted amount back to user!
            refundUserBalance(reqToRefund!!.userPhone, reqToRefund!!.amount)
            updateTransactionStatus(reqToRefund!!.userPhone, reqToRefund!!.username, TransactionType.WITHDRAW, TransactionStatus.REJECTED)

            SharedDataStore.notifyWithdrawStatusChanged(
                appContext,
                reqToRefund!!.id,
                "REJECTED",
                reqToRefund!!.amount,
                reqToRefund!!.userPhone,
                reqToRefund!!.username
            )
            SnxCloudSyncService.pushWithdrawStatusUpdate(appContext, reqToRefund!!.id, "REJECTED", reason)
            SnxCloudSyncService.pushToCloud(appContext)
            return true
        }
        return false
    }

    private fun updateTransactionStatus(phone: String, username: String, type: TransactionType, status: TransactionStatus) {
        val txs = sessionManager.getTransactions() ?: return
        val updated = txs.map { tx ->
            if (tx.type == type && tx.status == TransactionStatus.PENDING) {
                tx.copy(status = status)
            } else tx
        }
        sessionManager.saveTransactions(updated)
    }

    // ==========================================
    // 5. USER MANAGEMENT & BALANCE ADJUSTMENT
    // ==========================================
    fun getAllRegisteredUsers(): List<RegisteredAccount> {
        val accounts = sessionManager.getRegisteredAccounts().toMutableList()
        if (accounts.isEmpty()) {
            val defaults = listOf(
                RegisteredAccount(
                    username = "Sakib77",
                    phone = "01712-348901",
                    email = "sakib77@gmail.com",
                    password = "••••",
                    balanceBDT = 1250.0,
                    totalDeposited = 3000.0,
                    totalWithdrawn = 1500.0,
                    vipLevel = "VIP 2",
                    registeredDate = "12 Sep 2026, 02:15 PM"
                ),
                RegisteredAccount(
                    username = "RahimPro",
                    phone = "01823-456789",
                    email = "rahimpro@gmail.com",
                    password = "••••",
                    balanceBDT = 500.0,
                    totalDeposited = 1000.0,
                    totalWithdrawn = 200.0,
                    vipLevel = "VIP 1",
                    registeredDate = "10 Sep 2026, 04:30 PM"
                ),
                RegisteredAccount(
                    username = "TanvirGamer",
                    phone = "01934-567890",
                    email = "tanvir@gmail.com",
                    password = "••••",
                    balanceBDT = 4500.0,
                    totalDeposited = 10000.0,
                    totalWithdrawn = 5000.0,
                    vipLevel = "VIP 3",
                    registeredDate = "08 Sep 2026, 09:10 AM"
                ),
                RegisteredAccount(
                    username = "KamalBoss",
                    phone = "01755-667788",
                    email = "kamal@gmail.com",
                    password = "••••",
                    balanceBDT = 250.0,
                    totalDeposited = 500.0,
                    totalWithdrawn = 0.0,
                    vipLevel = "VIP 1",
                    registeredDate = "13 Sep 2026, 11:00 AM"
                )
            )
            defaults.forEach { sessionManager.saveRegisteredAccount(it) }
            accounts.addAll(defaults)
        }
        val currentSession = sessionManager.getUserSession()
        if (currentSession.isLoggedIn && accounts.none { it.phone == currentSession.phone }) {
            accounts.add(
                RegisteredAccount(
                    username = currentSession.username,
                    phone = currentSession.phone,
                    email = currentSession.email,
                    password = "••••",
                    balanceBDT = currentSession.balanceBDT,
                    totalDeposited = currentSession.totalDeposited,
                    totalWithdrawn = currentSession.totalWithdrawn,
                    vipLevel = currentSession.vipLevel,
                    registeredDate = "Live Active User"
                )
            )
        }
        return accounts
    }

    fun loadRegisteredUsers() {
        val users = getAllRegisteredUsers()
        _registeredUsers.value = users
    }

    fun updateUserStatus(phone: String, newStatus: String): Boolean {
        val cleanPhone = phone.trim()
        val accounts = sessionManager.getRegisteredAccounts().map {
            if (it.phone == cleanPhone) it.copy(status = newStatus) else it
        }
        sessionManager.saveRegisteredAccounts(accounts)

        val currentSession = sessionManager.getUserSession()
        if (currentSession.phone == cleanPhone) {
            sessionManager.saveUserSession(currentSession.copy(status = newStatus))
        }

        loadRegisteredUsers()
        SharedDataStore.notifyUserStatusChanged(appContext, cleanPhone, newStatus)
        return true
    }

    fun adjustUserBalance(phone: String, delta: Double, note: String = ""): Boolean {
        val cleanPhone = phone.trim()
        val current = _registeredUsers.value.find { it.phone == cleanPhone } ?: return false
        val newBalance = (current.balanceBDT + delta).coerceAtLeast(0.0)
        return updateUserBalance(cleanPhone, newBalance)
    }

    fun updateUserBalance(phone: String, newBalance: Double): Boolean {
        val cleanPhone = phone.trim()
        val accounts = sessionManager.getRegisteredAccounts().map {
            if (it.phone == cleanPhone) it.copy(balanceBDT = newBalance) else it
        }
        sessionManager.saveRegisteredAccounts(accounts)

        // Also check if current session matches
        val currentSession = sessionManager.getUserSession()
        if (currentSession.phone == cleanPhone) {
            sessionManager.saveUserSession(currentSession.copy(balanceBDT = newBalance))
        }

        loadRegisteredUsers()
        SharedDataStore.notifyBalanceAdjusted(appContext, cleanPhone, newBalance)
        SnxCloudSyncService.pushToCloud(appContext)
        return true
    }

    private fun creditUserBalance(phone: String, username: String, totalCredit: Double, depositAmount: Double) {
        val cleanPhone = phone.trim()
        val currentSession = sessionManager.getUserSession()
        if (currentSession.phone == cleanPhone || currentSession.username == username) {
            val updated = currentSession.copy(
                balanceBDT = currentSession.balanceBDT + totalCredit,
                totalDeposited = currentSession.totalDeposited + depositAmount
            )
            sessionManager.saveUserSession(updated)
        }

        // Update in registered accounts
        val accounts = sessionManager.getRegisteredAccounts().map {
            if (it.phone == cleanPhone || it.username == username) {
                it.copy(
                    balanceBDT = it.balanceBDT + totalCredit,
                    totalDeposited = it.totalDeposited + depositAmount
                )
            } else it
        }
        sessionManager.saveRegisteredAccounts(accounts)
        loadRegisteredUsers()
    }

    private fun refundUserBalance(phone: String, amount: Double) {
        val cleanPhone = phone.trim()
        val currentSession = sessionManager.getUserSession()
        if (currentSession.phone == cleanPhone) {
            sessionManager.saveUserSession(
                currentSession.copy(balanceBDT = currentSession.balanceBDT + amount)
            )
        }

        val accounts = sessionManager.getRegisteredAccounts().map {
            if (it.phone == cleanPhone) {
                it.copy(balanceBDT = it.balanceBDT + amount)
            } else it
        }
        sessionManager.saveRegisteredAccounts(accounts)
        loadRegisteredUsers()
    }

    // ==========================================
    // 6. SITE & GAME CUSTOMIZATION
    // ==========================================
    private fun loadSiteConfig() {
        val raw = prefs.getString(KEY_SITE_CONFIG_JSON, null) ?: return
        try {
            val obj = JSONObject(raw)
            _siteConfig.value = SiteCustomization(
                siteName = obj.optString("siteName", "SNX WORLD"),
                marqueeTicker = obj.optString("marqueeTicker", _siteConfig.value.marqueeTicker),
                bannerNotice = obj.optString("bannerNotice", _siteConfig.value.bannerNotice),
                aviatorGameTitle = obj.optString("aviatorGameTitle", "SPRIBE AVIATOR"),
                isMaintenanceMode = obj.optBoolean("isMaintenanceMode", false)
            )
        } catch (_: Exception) {}
    }

    fun updateSiteConfig(newConfig: SiteCustomization) {
        _siteConfig.value = newConfig
        try {
            val obj = JSONObject().apply {
                put("siteName", newConfig.siteName)
                put("marqueeTicker", newConfig.marqueeTicker)
                put("bannerNotice", newConfig.bannerNotice)
                put("aviatorGameTitle", newConfig.aviatorGameTitle)
                put("isMaintenanceMode", newConfig.isMaintenanceMode)
            }
            val jsonStr = obj.toString()
            prefs.edit().putString(KEY_SITE_CONFIG_JSON, jsonStr).apply()
            SharedDataStore.notifySiteConfigChanged(appContext, jsonStr)
            SnxCloudSyncService.pushToCloud(appContext)
        } catch (_: Exception) {}
    }

    // ==========================================
    // 7. DYNAMIC GAME MANAGEMENT
    // ==========================================
    fun loadGames() {
        val raw = prefs.getString(KEY_GAMES_LIST_JSON, null)
        if (raw.isNullOrEmpty()) {
            val defaults = getDefaultGamesList()
            _gamesList.value = defaults
            saveGamesListDirect(defaults)
        } else {
            try {
                val array = JSONArray(raw)
                val list = mutableListOf<GameItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val catStr = obj.optString("category", "SLOTS")
                    val category = try {
                        GameCategory.valueOf(catStr)
                    } catch (_: Exception) {
                        GameCategory.SLOTS
                    }
                    list.add(
                        GameItem(
                            id = obj.getString("id"),
                            titleBn = obj.getString("titleBn"),
                            titleEn = obj.getString("titleEn"),
                            category = category,
                            badge = if (obj.isNull("badge") || obj.optString("badge").isEmpty()) null else obj.optString("badge"),
                            iconEmoji = obj.optString("iconEmoji", "🎰"),
                            minBet = obj.optDouble("minBet", 10.0),
                            playersCount = obj.optInt("playersCount", 1420),
                            imageUrl = obj.optString("imageUrl", ""),
                            isActive = obj.optBoolean("isActive", true),
                            serverStatus = GameServerStatus.fromCode(obj.optString("serverStatus", "ACTIVE"))
                        )
                    )
                }
                _gamesList.value = list
            } catch (_: Exception) {
                val defaults = getDefaultGamesList()
                _gamesList.value = defaults
            }
        }
    }

    fun getDefaultGamesList(): List<GameItem> = listOf(
        GameItem("slot_777", "মেগা জ্যাকপট ৭৭৭", "Mega Jackpot 777", GameCategory.SLOTS, "HOT", "🎰", 10.0, 3420),
        GameItem("aviator_crash", "SPRIBE AVIATOR", "SPRIBE AVIATOR", GameCategory.CRASH, "HOT", "✈️", 50.0, 9480),
        GameItem("cricket_live", "বিপিএল ক্রিকেট লাইভ প্রেডিকশন", "BPL Cricket Live", GameCategory.SPORTS, "LIVE", "🏏", 50.0, 4120),
        GameItem("lucky_wheel", "দৈনিক লাকি স্পিন হুইল", "Daily Lucky Spin", GameCategory.HOT, "BONUS", "🎡", 0.0, 8910),
        GameItem("teen_patti", "তিন পাত্তি রয়েল", "Teen Patti Royal", GameCategory.CASINO, "HOT", "🃏", 20.0, 1850),
        GameItem("dragon_tiger", "ড্রাগন ভার্সেস টাইগার", "Dragon vs Tiger", GameCategory.CASINO, "LIVE", "🐉", 20.0, 2310),
        GameItem("fruit_frenzy", "ফ্রুট স্লট ব্লাস্ট", "Fruit Slot Blast", GameCategory.SLOTS, null, "🍒", 10.0, 980),
        GameItem("roulette_pro", "ইউরোপিয়ান রুলেট", "European Roulette", GameCategory.TABLE, null, "🎯", 50.0, 1150),
        GameItem("fish_hunter", "ওশান কিং ফিশ শুটার", "Ocean King Fish Hunter", GameCategory.HOT, "POPULAR", "🐟", 10.0, 1640),
        GameItem("dice_roll", "লাকি ডাইস হাই-লো", "Lucky Dice Hi-Lo", GameCategory.TABLE, null, "🎲", 10.0, 820),
        GameItem("gold_rush_slot", "গোল্ড রাশ মেগাওয়েস", "Gold Rush Megaways", GameCategory.SLOTS, "JACKPOT", "💰", 20.0, 2740),
        GameItem("crazy_time", "ক্রেজি টাইম শো", "Crazy Time Live Show", GameCategory.CASINO, "LIVE", "🎪", 50.0, 6200)
    )

    fun addGame(game: GameItem): Boolean {
        val list = _gamesList.value.toMutableList()
        list.add(0, game)
        _gamesList.value = list
        saveGamesListDirect(list)
        return true
    }

    fun updateGame(updated: GameItem): Boolean {
        val list = _gamesList.value.map {
            if (it.id == updated.id) updated else it
        }
        _gamesList.value = list
        saveGamesListDirect(list)
        return true
    }

    fun deleteGame(gameId: String): Boolean {
        val list = _gamesList.value.filterNot { it.id == gameId }
        _gamesList.value = list
        saveGamesListDirect(list)
        return true
    }

    fun toggleGameActive(gameId: String): Boolean {
        val list = _gamesList.value.map {
            if (it.id == gameId) it.copy(isActive = !it.isActive) else it
        }
        _gamesList.value = list
        saveGamesListDirect(list)
        return true
    }

    fun setGameServerStatus(gameId: String, status: GameServerStatus): Boolean {
        val list = _gamesList.value.map {
            if (it.id == gameId) it.copy(serverStatus = status, isActive = (status != GameServerStatus.OFFLINE)) else it
        }
        _gamesList.value = list
        saveGamesListDirect(list)
        return true
    }

    private fun saveGamesListDirect(list: List<GameItem>) {
        try {
            val array = JSONArray()
            list.forEach { g ->
                val obj = JSONObject().apply {
                    put("id", g.id)
                    put("titleBn", g.titleBn)
                    put("titleEn", g.titleEn)
                    put("category", g.category.name)
                    put("badge", g.badge ?: "")
                    put("iconEmoji", g.iconEmoji)
                    put("minBet", g.minBet)
                    put("playersCount", g.playersCount)
                    put("imageUrl", g.imageUrl)
                    put("isActive", g.isActive)
                    put("serverStatus", g.serverStatus.code)
                }
                array.put(obj)
            }
            val jsonStr = array.toString()
            prefs.edit().putString(KEY_GAMES_LIST_JSON, jsonStr).apply()
            SharedDataStore.notifyGamesChanged(appContext, jsonStr)
            SnxCloudSyncService.pushToCloud(appContext)
        } catch (_: Exception) {}
    }
}
