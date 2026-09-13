package com.example.util

import com.example.model.AppLanguage

object StringRes {
    var currentLanguage: AppLanguage = AppLanguage.BN

    fun t(lang: AppLanguage, bn: String, en: String): String {
        return if (lang == AppLanguage.BN) bn else en
    }

    fun toBengaliDigits(input: String): String {
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatBDT(amount: Double, lang: AppLanguage = currentLanguage): String {
        val formatted = "৳ %,.2f".format(amount)
        return if (lang == AppLanguage.BN) toBengaliDigits(formatted) else formatted
    }

    fun formatBDTShort(amount: Double, lang: AppLanguage = currentLanguage): String {
        val formatted = "৳ %,.0f".format(amount)
        return if (lang == AppLanguage.BN) toBengaliDigits(formatted) else formatted
    }

    fun formatDigits(value: String, lang: AppLanguage = currentLanguage): String {
        return if (lang == AppLanguage.BN) toBengaliDigits(value) else value
    }
}
