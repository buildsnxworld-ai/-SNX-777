package com.example.model

enum class AppLanguage {
    BN, EN
}

data class UserProfile(
    val username: String = "",
    val phone: String = "",
    val email: String = "",
    val balanceBDT: Double = 0.0,
    val vipLevel: String = "VIP 1",
    val referralCode: String = "SNX777VIP",
    val isLoggedIn: Boolean = false,
    val totalDeposited: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val totalInvitedCount: Int = 0,
    val friendsTotalWonBDT: Double = 0.0,
    val earnedCommissionBDT: Double = 0.0,
    val pendingCouponCode: String? = null,
    val lastDailySpinDate: String? = null,
    val lastDailySpinTimestamp: Long = 0L,
    val lastWeeklyCashbackClaimDate: String? = null,
    val status: String = "ACTIVE"
)

enum class GameCategory(val bn: String, val en: String, val icon: String) {
    ALL("সব গেম", "All Games", "🔥"),
    HOT("জনপ্রিয়", "Hot Games", "⭐"),
    SLOTS("স্লট গেম", "Slots", "🎰"),
    CRASH("ক্র্যাশ গেম", "Crash", "🚀"),
    SPORTS("ক্রিকেট ও স্পোর্টস", "Sports", "🏏"),
    CASINO("লাইভ ক্যাসিনো", "Live Casino", "🃏"),
    TABLE("টেবিল গেম", "Table", "🎲")
}

enum class GameServerStatus(val code: String, val bn: String, val en: String, val colorHex: Long) {
    ACTIVE("ACTIVE", "সক্রিয়", "Active", 0xFF00E676),
    SERVER_UPDATE("SERVER_UPDATE", "সার্ভার আপডেট", "Server Update", 0xFFFF9800),
    SERVER_ERROR("SERVER_ERROR", "সার্ভার এরর", "Server Error", 0xFFEF4444),
    OFFLINE("OFFLINE", "নিষ্ক্রিয়/বন্ধ", "Offline", 0xFF64748B);

    companion object {
        fun fromCode(code: String?): GameServerStatus {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ACTIVE
        }
    }
}

data class GameItem(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val category: GameCategory,
    val badge: String? = null,
    val iconEmoji: String,
    val minBet: Double = 10.0,
    val playersCount: Int = 1420,
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val serverStatus: GameServerStatus = GameServerStatus.ACTIVE
)

enum class PaymentMethod(val displayName: String, val colorHex: Long, val number: String) {
    BKASH("bKash (বিকাশ)", 0xFFE2136E, ""),
    NAGAD("Nagad (নগদ)", 0xFFF7941D, "")
}

data class TransactionRecord(
    val id: String,
    val type: TransactionType,
    val method: PaymentMethod,
    val amount: Double,
    val accountNo: String,
    val trxId: String,
    val status: TransactionStatus,
    val timeFormatted: String
)

enum class TransactionType(val bn: String, val en: String) {
    DEPOSIT("ডিপোজিট", "Deposit"),
    WITHDRAW("উত্তোলন", "Withdrawal")
}

enum class TransactionStatus(val bn: String, val en: String, val colorHex: Long) {
    APPROVED("অনুমোদিত", "Approved", 0xFF00E676),
    PENDING("প্রক্রিয়াধীন", "Pending", 0xFFFFB300),
    REJECTED("বাতিল", "Rejected", 0xFFFF5252)
}

data class CricketMatch(
    val matchId: String,
    val league: String,
    val teamA: String,
    val teamB: String,
    val scoreA: String,
    val scoreB: String,
    val overs: String,
    val oddsA: Double,
    val oddsB: Double,
    val currentBatter: String,
    val statusText: String
)
