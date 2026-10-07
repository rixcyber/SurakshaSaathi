package com.example.surakshasaathi.voice

import java.util.Locale

/**
 * Supported regional languages for Suraksha Saathi:
 * - English (Indian English)
 * - Hindi (हिन्दी)
 * - Odia (ଓଡ଼ିଆ)
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val primaryLocale: Locale,
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        flagEmoji = "🇮🇳",
        primaryLocale = Locale.forLanguageTag("en-IN"),
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिन्दी",
        flagEmoji = "🇮🇳",
        primaryLocale = Locale.forLanguageTag("hi-IN"),
    ),
    ODIA(
        code = "or",
        displayName = "Odia",
        nativeName = "ଓଡ଼ିଆ",
        flagEmoji = "🇮🇳",
        primaryLocale = Locale.forLanguageTag("or-IN"),
    );

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
