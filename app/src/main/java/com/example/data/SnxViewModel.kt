package com.example.data

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.util.StringRes

class SnxViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        val updated = sessionManager.getUserSession()
        _userProfile.value = updated
        sessionManager.getTransactions()?.let { saved ->
            val realOnly = saved.filterNot { it.id in setOf("TX9841", "TX9820", "TX9755") }
            _transactions.value = realOnly
        }
    }

    private val _language = MutableStateFlow(sessionManager.getLanguage() ?: AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _userProfile = MutableStateFlow(sessionManager.getUserSession())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        StringRes.currentLanguage = _language.value
    }

    private val _activeTab = MutableStateFlow(0) // 0: Home, 1: Games, 2: Deposit, 3: Withdraw, 4: Profile
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(GameCategory.ALL)
    val selectedCategory: StateFlow<GameCategory> = _selectedCategory.asStateFlow()

    private val _activeModalGame = MutableStateFlow<String?>(null)
    val activeModalGame: StateFlow<String?> = _activeModalGame.asStateFlow()

    private val _isInsufficientBalanceModalOpen = MutableStateFlow(false)
    val isInsufficientBalanceModalOpen: StateFlow<Boolean> = _isInsufficientBalanceModalOpen.asStateFlow()

    private val _pendingGameId = MutableStateFlow<String?>(null)
    val pendingGameId: StateFlow<String?> = _pendingGameId.asStateFlow()

    private val _isAuthModalOpen = MutableStateFlow(false)
    val isAuthModalOpen: StateFlow<Boolean> = _isAuthModalOpen.asStateFlow()

    private val _authModalInitialTab = MutableStateFlow(0) // 0: Login, 1: Register
    val authModalInitialTab: StateFlow<Int> = _authModalInitialTab.asStateFlow()

    private val _isSupportModalOpen = MutableStateFlow(false)
    val isSupportModalOpen: StateFlow<Boolean> = _isSupportModalOpen.asStateFlow()

    private val _isPromotionalAdsOpen = MutableStateFlow(true)
    val isPromotionalAdsOpen: StateFlow<Boolean> = _isPromotionalAdsOpen.asStateFlow()

    private val _jackpotPool = MutableStateFlow(24850900.0)
    val jackpotPool: StateFlow<Double> = _jackpotPool.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // All available default games
    val allGames: List<GameItem> = listOf(
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

    private val _dynamicGames = MutableStateFlow<List<GameItem>>(emptyList())
    val dynamicGames: StateFlow<List<GameItem>> = _dynamicGames.asStateFlow()

    private val _siteConfig = MutableStateFlow(SiteCustomization())
    val siteConfig: StateFlow<SiteCustomization> = _siteConfig.asStateFlow()

    private val _gameServerNotice = MutableStateFlow<GameNoticeInfo?>(null)
    val gameServerNotice: StateFlow<GameNoticeInfo?> = _gameServerNotice.asStateFlow()

    fun dismissGameServerNotice() {
        _gameServerNotice.value = null
    }

    init {
        _dynamicGames.value = loadGamesFromPrefs()
        _siteConfig.value = loadSiteConfigFromPrefs()
        // Load saved transaction history if present (filter out any legacy mock data)
        sessionManager.getTransactions()?.let { saved ->
            val realOnly = saved.filterNot { it.id in setOf("TX9841", "TX9820", "TX9755") }
            if (realOnly.isNotEmpty()) {
                _transactions.value = realOnly
            }
        }

        // Pull any updates from other app on startup
        SharedDataStore.pullFromOtherApp(getApplication())

        // Listen for preference changes (e.g., when Admin approves deposit or adjusts balance)
        sessionManager.registerListener(prefListener)

        // Listen for cross-app live sync events
        viewModelScope.launch {
            SharedDataStore.syncFlow.collect {
                reloadUserData()
            }
        }

        // Auto-pull sync every 2.5 seconds to guarantee 100% instant real-time sync across separate APKs
        viewModelScope.launch {
            while (true) {
                delay(2500)
                SharedDataStore.pullFromOtherApp(getApplication())
            }
        }

        // Auto-persist user profile updates to session manager (like cookies)
        viewModelScope.launch {
            _userProfile.collect { profile ->
                sessionManager.saveUserSession(profile)
            }
        }

        // Auto-persist transactions history
        viewModelScope.launch {
            _transactions.collect { list ->
                sessionManager.saveTransactions(list)
            }
        }

        // Increment jackpot slightly to look alive
        viewModelScope.launch {
            while (true) {
                delay(3000)
                _jackpotPool.update { it + (15..85).random() }
            }
        }
    }

    fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN
        _language.value = next
        StringRes.currentLanguage = next
        sessionManager.saveLanguage(next)
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        StringRes.currentLanguage = lang
        sessionManager.saveLanguage(lang)
    }

    fun setActiveTab(tabIndex: Int) {
        _activeTab.value = tabIndex
    }

    fun setCategory(category: GameCategory) {
        _selectedCategory.value = category
    }

    fun openAuthModal(initialTab: Int = 0) {
        _authModalInitialTab.value = initialTab
        _isAuthModalOpen.value = true
    }

    fun closeAuthModal() {
        _isAuthModalOpen.value = false
    }

    fun openSupportModal() {
        _isSupportModalOpen.value = true
    }

    fun closeSupportModal() {
        _isSupportModalOpen.value = false
    }

    fun closePromotionalAds() {
        _isPromotionalAdsOpen.value = false
    }

    fun openPromotionalAds() {
        _isPromotionalAdsOpen.value = true
    }

    fun openGame(gameId: String) {
        if (_userProfile.value.status == "BANNED" || _userProfile.value.status == "SUSPENDED") {
            showToast(
                if (_language.value == AppLanguage.BN)
                    "🚫 আপনার একাউন্টটি সাময়িক বা স্থায়ীভাবে স্থগিত/ব্যান করা হয়েছে। অনুগ্রহ করে এডমিন সাপোর্টে যোগাযোগ করুন।"
                else
                    "🚫 Your account is suspended or banned. Please contact admin support."
            )
            return
        }

        val game = _dynamicGames.value.firstOrNull { it.id == gameId }
            ?: allGames.firstOrNull { it.id == gameId }

        if (game != null) {
            if (game.serverStatus == GameServerStatus.SERVER_UPDATE ||
                game.serverStatus == GameServerStatus.SERVER_ERROR ||
                game.serverStatus == GameServerStatus.OFFLINE ||
                !game.isActive
            ) {
                _gameServerNotice.value = GameNoticeInfo(game, game.serverStatus)
                return
            }
        }

        if (gameId == "aviator_crash") {
            if (!_userProfile.value.isLoggedIn) {
                showToast(
                    if (_language.value == AppLanguage.BN)
                        "SPRIBE AVIATOR গেম খেলতে অনুগ্রহ করে প্রথমে একাউন্ট রেজিস্ট্রেশন বা লগইন করুন"
                    else
                        "Please register or login to play SPRIBE AVIATOR"
                )
                openAuthModal(1)
                return
            }
            _activeModalGame.value = "aviator_crash"
            return
        }

        if (gameId == "lucky_wheel") {
            if (!_userProfile.value.isLoggedIn) {
                showToast(
                    if (_language.value == AppLanguage.BN)
                        "আপনার একাউন্ট খোলা হয় নাই, দয়া করে একাউন্ট খুলুন তারপর প্রবেশ করতে পারবেন"
                    else
                        "Account not created. Please register/login to spin the wheel"
                )
                openAuthModal(1)
                return
            }
            _activeModalGame.value = "lucky_wheel"
            return
        }

        if (!_userProfile.value.isLoggedIn) {
            showToast(
                if (_language.value == AppLanguage.BN)
                    "আপনার একাউন্ট খোলা হয় নাই, দয়া করে একাউন্ট খুলুন তারপর প্রবেশ করতে পারবেন"
                else
                    "Your account has not been created yet. Please register or login to proceed"
            )
            openAuthModal(1)
            return
        }

        // As requested by user: Clicking any game will NOT enter the game, it simply indicates that there is no balance
        _pendingGameId.value = gameId
        _isInsufficientBalanceModalOpen.value = true
        showToast(
            if (_language.value == AppLanguage.BN)
                "আপনার একাউন্ট এ ব্যালেন্স নেই! গেম খেলতে অনুগ্রহ করে ডিপোজিট করুন।"
            else
                "Your account has no balance! Please deposit to play."
        )
    }

    fun closeGame() {
        _activeModalGame.value = null
    }

    fun closeInsufficientBalanceModal() {
        _isInsufficientBalanceModalOpen.value = false
        _pendingGameId.value = null
    }

    fun goToDepositFromGameModal() {
        _isInsufficientBalanceModalOpen.value = false
        _activeTab.value = 2 // Switch to Deposit Screen
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun dismissToast() {
        _toastMessage.value = null
    }

    fun login(identifier: String, pass: String): Pair<Boolean, String?> {
        val cleanId = identifier.trim()
        if (cleanId.length < 6 || pass.length < 4) {
            val msg = if (_language.value == AppLanguage.BN) "অনুগ্রহ করে সঠিক ফোন নম্বর/ইমেইল ও পাসওয়ার্ড দিন" else "Please enter valid phone/email & password"
            showToast(msg)
            return Pair(false, msg)
        }

        val account = sessionManager.findRegisteredAccount(cleanId, pass)
        if (account != null) {
            if (account.status == "BANNED") {
                val msg = if (_language.value == AppLanguage.BN)
                    "🚫 আপনার একাউন্টটি অ্যাডমিন কর্তৃক ব্যান করা হয়েছে। অনুগ্রহ করে সাপোর্টে যোগাযোগ করুন।"
                else
                    "🚫 Your account has been banned by Admin. Please contact support."
                showToast(msg)
                return Pair(false, msg)
            }
            _userProfile.update {
                it.copy(
                    username = account.username,
                    phone = account.phone,
                    email = account.email,
                    balanceBDT = account.balanceBDT,
                    totalDeposited = account.totalDeposited,
                    totalWithdrawn = account.totalWithdrawn,
                    vipLevel = account.vipLevel,
                    isLoggedIn = true,
                    status = account.status
                )
            }
        } else {
            // Allow login for existing session or default identifier
            _userProfile.update {
                it.copy(
                    username = if (it.username.isNotBlank()) it.username else "User" + cleanId.takeLast(4),
                    phone = if (cleanId.all { c -> c.isDigit() }) cleanId else it.phone,
                    email = if (cleanId.contains("@")) cleanId else it.email,
                    isLoggedIn = true
                )
            }
        }
        sessionManager.saveUserSession(_userProfile.value)
        closeAuthModal()
        openPromotionalAds()
        showToast(if (_language.value == AppLanguage.BN) "স্বাগতম! সফলভাবে লগইন হয়েছে" else "Welcome back! Login successful")
        return Pair(true, null)
    }

    fun register(username: String, phone: String, pass: String, refCode: String = ""): Pair<Boolean, String?> {
        val cleanPhone = phone.trim()

        if (cleanPhone.length < 10 || pass.length < 4) {
            val msg = if (_language.value == AppLanguage.BN)
                "সব তথ্য (সঠিক মোবাইল নম্বর ও পাসওয়ার্ড) পূরণ করুন"
            else
                "Please provide a valid mobile number and password"
            showToast(msg)
            return Pair(false, msg)
        }

        // Check if phone number is already registered by any user
        if (sessionManager.isPhoneOrEmailRegistered(cleanPhone, "")) {
            val duplicateMsg = if (_language.value == AppLanguage.BN)
                "এই মোবাইল নম্বর টি ইতিমধ্যে ব্যাবহার করা হয়েছে"
            else
                "This mobile number is already in use"
            showToast(duplicateMsg)
            return Pair(false, duplicateMsg)
        }

        val displayName = if (username.isNotBlank()) username.trim() else "Player" + cleanPhone.takeLast(4)

        // Save new user in registry with STRICTLY 0.0 balance (no automatic 7 taka bonus)
        val regDateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val newAccount = RegisteredAccount(
            username = displayName,
            phone = cleanPhone,
            email = "",
            password = pass,
            balanceBDT = 0.0,
            totalDeposited = 0.0,
            totalWithdrawn = 0.0,
            vipLevel = "VIP 1",
            registeredDate = regDateStr,
            status = "ACTIVE"
        )
        sessionManager.saveRegisteredAccount(newAccount)
        SharedDataStore.notifyUserRegistered(getApplication(), newAccount)

        // Set active user profile with exactly 0.0 balance (disable automatic 7 taka)
        _userProfile.value = UserProfile(
            username = displayName,
            phone = cleanPhone,
            email = "",
            balanceBDT = 0.0, // Strictly zero balance
            vipLevel = "VIP 1",
            referralCode = "SNX777VIP",
            isLoggedIn = true,
            totalDeposited = 0.0,
            totalWithdrawn = 0.0,
            totalInvitedCount = 0,
            friendsTotalWonBDT = 0.0,
            earnedCommissionBDT = 0.0,
            pendingCouponCode = null, // Vouchers cannot be auto-saved
            lastDailySpinDate = null,
            lastDailySpinTimestamp = 0L,
            lastWeeklyCashbackClaimDate = null,
            status = "ACTIVE"
        )
        sessionManager.saveUserSession(_userProfile.value)
        closeAuthModal()
        openPromotionalAds()
        showToast(
            if (_language.value == AppLanguage.BN)
                "অভিনন্দন! রেজিস্ট্রেশন সফল হয়েছে।"
            else
                "Registration successful! Welcome to SNX 777!"
        )
        return Pair(true, null)
    }

    fun logout() {
        _userProfile.update { it.copy(isLoggedIn = false) }
        sessionManager.clearSession()
        showToast(if (_language.value == AppLanguage.BN) "লগআউট সফল হয়েছে" else "Logged out successfully")
    }

    fun submitDeposit(method: PaymentMethod, amount: Double, accountNo: String, trxId: String): Boolean {
        if (_userProfile.value.status == "BANNED" || _userProfile.value.status == "SUSPENDED") {
            showToast(
                if (_language.value == AppLanguage.BN)
                    "🚫 আপনার একাউন্টটি সাময়িক বা স্থায়ীভাবে স্থগিত/ব্যান করা হয়েছে। ডিপোজিট গ্রহণযোগ্য নয়।"
                else
                    "🚫 Your account is suspended or banned. Deposit not allowed."
            )
            return false
        }
        if (amount < 300) {
            showToast(if (_language.value == AppLanguage.BN) "সর্বনিম্ন ডিপোজিট ৳৩০০ টাকা" else "Minimum deposit is ৳300")
            return false
        }
        if (trxId.length < 5) {
            showToast(if (_language.value == AppLanguage.BN) "সঠিক ট্রানজেকশন আইডি (TrxID) প্রদান করুন" else "Enter valid TrxID")
            return false
        }

        val currentTime = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
        val record = TransactionRecord(
            id = "TX" + (1000..9999).random(),
            type = TransactionType.DEPOSIT,
            method = method,
            amount = amount,
            accountNo = accountNo,
            trxId = trxId.uppercase(),
            status = TransactionStatus.PENDING, // Pending until verified and approved by Admin
            timeFormatted = currentTime,
            username = _userProfile.value.username,
            userPhone = _userProfile.value.phone
        )

        _transactions.update { listOf(record) + it }
        sessionManager.saveTransactions(_transactions.value)

        try {
            val adminMgr = AdminManager.getInstance(getApplication())
            adminMgr.submitUserDepositRequest(
                username = _userProfile.value.username,
                userPhone = _userProfile.value.phone,
                method = method,
                amount = amount,
                agentNumberUsed = adminMgr.getActiveDepositNumber(method),
                userAccountNo = accountNo,
                trxId = trxId
            )
        } catch (_: Exception) {}

        val msg = if (_language.value == AppLanguage.BN)
            "ডিপোজিট আবেদন জমা হয়েছে! ট্রানজেকশন আইডি অ্যাডমিন যাচাই করে এপ্রুভ করার পর ব্যালেন্স যুক্ত হবে।"
        else
            "Deposit request submitted! Balance will be credited once verified and approved by Admin."
        showToast(msg)
        return true
    }

    fun submitWithdrawal(method: PaymentMethod, amount: Double, accountNo: String): Boolean {
        if (_userProfile.value.status == "BANNED" || _userProfile.value.status == "SUSPENDED") {
            showToast(
                if (_language.value == AppLanguage.BN)
                    "🚫 আপনার একাউন্টটি সাময়িক বা স্থায়ীভাবে স্থগিত/ব্যান করা হয়েছে। উইথড্র গ্রহণ করা সম্ভব নয়।"
                else
                    "🚫 Your account is suspended or banned. Withdrawal not allowed."
            )
            return false
        }
        if (amount < 500) {
            showToast(if (_language.value == AppLanguage.BN) "সর্বনিম্ন উত্তোলন ৳৫০০ টাকা" else "Minimum withdrawal is ৳500")
            return false
        }
        if (amount > _userProfile.value.balanceBDT) {
            showToast(if (_language.value == AppLanguage.BN) "অপর্যাপ্ত ব্যালেন্স!" else "Insufficient balance!")
            return false
        }
        if (accountNo.length < 10) {
            showToast(if (_language.value == AppLanguage.BN) "সঠিক বিকাশ বা নগদ পার্সোনাল নম্বর লিখুন" else "Please enter valid bKash/Nagad personal number")
            return false
        }

        val currentTime = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
        val record = TransactionRecord(
            id = "WD" + (1000..9999).random(),
            type = TransactionType.WITHDRAW,
            method = method,
            amount = amount,
            accountNo = accountNo,
            trxId = "W" + (100000..999999).random(),
            status = TransactionStatus.PENDING, // Pending until verified and approved by Admin
            timeFormatted = currentTime,
            username = _userProfile.value.username,
            userPhone = _userProfile.value.phone
        )

        _transactions.update { listOf(record) + it }
        // Deduct into escrow pending admin verification
        _userProfile.update {
            it.copy(
                balanceBDT = it.balanceBDT - amount,
                totalWithdrawn = it.totalWithdrawn + amount
            )
        }
        sessionManager.saveUserSession(_userProfile.value)
        sessionManager.saveTransactions(_transactions.value)

        try {
            val adminMgr = AdminManager.getInstance(getApplication())
            adminMgr.submitUserWithdrawRequest(
                username = _userProfile.value.username,
                userPhone = _userProfile.value.phone,
                method = method,
                amount = amount,
                targetAccountNo = accountNo
            )
        } catch (_: Exception) {}

        showToast(
            if (_language.value == AppLanguage.BN)
                "উত্তোলন আবেদন জমা হয়েছে! অ্যাডমিন যাচাই করে টাকা পাঠানোর পর সফল হবে।"
            else
                "Withdrawal request submitted! Will be dispatched upon Admin approval."
        )
        return true
    }

    fun adjustBalance(delta: Double) {
        _userProfile.update {
            it.copy(balanceBDT = (it.balanceBDT + delta).coerceAtLeast(0.0))
        }
    }

    fun claimDailyBonus() {
        if (!_userProfile.value.isLoggedIn) {
            showToast(
                if (_language.value == AppLanguage.BN)
                    "ফ্রি স্পিন খেলতে অনুগ্রহ করে প্রথমে বিনামূল্যে একাউন্ট রেজিস্ট্রেশন করুন"
                else
                    "Please register free first to spin the wheel"
            )
            openAuthModal(1)
            return
        }
        openGame("lucky_wheel")
    }

    fun awardDailySpinBonus(prize: Double) {
        val now = System.currentTimeMillis()
        val cooldownMs = 24 * 60 * 60 * 1000L
        val lastSpin = _userProfile.value.lastDailySpinTimestamp
        if (lastSpin > 0L && (now - lastSpin) < cooldownMs) {
            val remainingMs = cooldownMs - (now - lastSpin)
            val remHours = remainingMs / (1000 * 60 * 60)
            val remMins = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
            showToast(
                if (_language.value == AppLanguage.BN)
                    "২৪ ঘণ্টায় মাত্র ১ বার ফ্রি স্পিন প্রযোজ্য! পরবর্তী স্পিন %d ঘণ্টা %d মিনিট পর।".format(remHours, remMins)
                else
                    "Only 1 free spin allowed per 24 hours! Next spin in %dh %dm.".format(remHours, remMins)
            )
            return
        }
        _userProfile.update {
            it.copy(
                balanceBDT = it.balanceBDT + prize,
                lastDailySpinTimestamp = now
            )
        }
        showToast(
            if (_language.value == AppLanguage.BN)
                "🎉 অভিনন্দন! ফ্রি স্পিনে আপনি ৳%,.0f বোনাস পেয়েছেন! পরবর্তী স্পিন ২৪ ঘণ্টা পর আবার করতে পারবেন।".format(prize)
            else
                "🎉 Congrats! You won ৳%,.0f bonus! Next spin unlocks in 24 hours.".format(prize)
        )
    }

    fun recordFriendInvite() {
        _userProfile.update {
            it.copy(totalInvitedCount = it.totalInvitedCount + 1)
        }
        showToast(
            if (_language.value == AppLanguage.BN)
                "ইনভাইট লিঙ্ক সফলভাবে শেয়ার হয়েছে!"
            else
                "Invite link shared successfully!"
        )
    }

    fun claimReferralCommission() {
        val commission = _userProfile.value.earnedCommissionBDT
        if (commission <= 0.0) {
            showToast(
                if (_language.value == AppLanguage.BN)
                    "বর্তমানে কোনো অর্জিত কমিশন নেই। বন্ধুদের ইনভাইট করুন!"
                else
                    "No commission earned yet. Invite friends to start earning!"
            )
            return
        }
        adjustBalance(commission)
        _userProfile.update { it.copy(earnedCommissionBDT = 0.0) }
        showToast(
            if (_language.value == AppLanguage.BN)
                "🎉 বন্ধুদের উইনিং কমিশন ৳%,.0f সরাসরি আপনার ওয়ালেটে জমা হয়েছে!".format(commission)
            else
                "🎉 Friends winning commission ৳%,.0f credited to your wallet!".format(commission)
        )
    }

    fun applyDiscountCoupon(couponCode: String) {
        val user = _userProfile.value
        if (!user.isLoggedIn) {
            showToast(if (_language.value == AppLanguage.BN) "ভাউচার সক্রিয় করতে অনুগ্রহ করে প্রথমে লগইন বা রেজিস্ট্রেশন করুন" else "Please login or register to claim vouchers")
            openAuthModal(0)
            return
        }

        // Only allow saving/claiming when user has activity (minimum deposit ৳1,000)
        if (user.totalDeposited < 1000.0) {
            showToast(
                if (_language.value == AppLanguage.BN)
                    "🔒 এই ভাউচারটি সংরক্ষণ করতে প্রথমে একাউন্টে ন্যূনতম ৳১,০০০ ডিপোজিট বা গেম খেলার সক্রিয়তা থাকতে হবে।"
                else
                    "🔒 To save this voucher, you must have an account with a minimum deposit of ৳1,000."
            )
            return
        }

        _userProfile.update { it.copy(pendingCouponCode = couponCode) }
        sessionManager.saveUserSession(_userProfile.value)
        showToast(
            if (_language.value == AppLanguage.BN)
                "✅ ভাউচার '$couponCode' সংরক্ষিত হয়েছে! পরবর্তী ডিপোজিটে স্বয়ংক্রিয় ডিসকাউন্ট যুক্ত হবে।"
            else
                "✅ Voucher '$couponCode' saved! Discount will be credited on your next deposit."
        )
    }

    fun reloadUserData() {
        val updated = sessionManager.getUserSession()
        _userProfile.value = updated
        _siteConfig.value = loadSiteConfigFromPrefs()
        _dynamicGames.value = loadGamesFromPrefs()
        sessionManager.getTransactions()?.let { saved ->
            val realOnly = saved.filterNot { it.id in setOf("TX9841", "TX9820", "TX9755") }
            _transactions.value = realOnly
        }
    }

    private fun loadSiteConfigFromPrefs(): SiteCustomization {
        val prefs = SharedDataStore.getAdminPrefs(getApplication())
        val raw = prefs.getString("key_site_config_json", null) ?: return SiteCustomization()
        return try {
            val obj = org.json.JSONObject(raw)
            SiteCustomization(
                siteName = obj.optString("siteName", "SNX WORLD"),
                marqueeTicker = obj.optString("marqueeTicker", "📢 প্রিয় গ্রাহক, ডিপোজিট করার পূর্বে প্রতিবার এডমিন প্যানেল কর্তৃক নির্ধারিত আপডেট নম্বর চেক করে টাকা পাঠান।"),
                bannerNotice = obj.optString("bannerNotice", "🔥 শুভ স্বাগতম! বিকাশ ও নগদে ডিপোজিটে ৫% ইনস্ট্যান্ট বোনাস! নতুন গেম Spribe Aviator লাইভ! 🔥"),
                aviatorGameTitle = obj.optString("aviatorGameTitle", "SPRIBE AVIATOR"),
                isMaintenanceMode = obj.optBoolean("isMaintenanceMode", false)
            )
        } catch (_: Exception) {
            SiteCustomization()
        }
    }

    private fun loadGamesFromPrefs(): List<GameItem> {
        val prefs = SharedDataStore.getAdminPrefs(getApplication())
        val raw = prefs.getString("key_games_list_json", null)
        if (raw.isNullOrEmpty()) {
            return allGames
        }
        return try {
            val array = org.json.JSONArray(raw)
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
            if (list.isEmpty()) allGames else list
        } catch (_: Exception) {
            allGames
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.unregisterListener(prefListener)
    }
}

data class GameNoticeInfo(
    val game: GameItem,
    val status: GameServerStatus
)
