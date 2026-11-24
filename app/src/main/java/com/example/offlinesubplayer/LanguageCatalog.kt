package com.example.offlinesubplayer

import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.Locale

data class LanguageOption(
    val code: String,
    val label: String
)

object LanguageCatalog {
    private val indonesianLocale = Locale.forLanguageTag("id-ID")

    private val customIndonesianNames = mapOf(
        TranslateLanguage.AFRIKAANS to "Afrikaans",
        TranslateLanguage.ALBANIAN to "Albania",
        TranslateLanguage.ARABIC to "Arab",
        TranslateLanguage.BELARUSIAN to "Belarusia",
        TranslateLanguage.BENGALI to "Bengali",
        TranslateLanguage.BULGARIAN to "Bulgaria",
        TranslateLanguage.CATALAN to "Katalan",
        TranslateLanguage.CHINESE to "Mandarin (Tionghoa)",
        TranslateLanguage.CROATIAN to "Kroasia",
        TranslateLanguage.CZECH to "Cheska",
        TranslateLanguage.DANISH to "Dansk",
        TranslateLanguage.DUTCH to "Belanda",
        TranslateLanguage.ENGLISH to "Inggris",
        TranslateLanguage.ESPERANTO to "Esperanto",
        TranslateLanguage.ESTONIAN to "Estonia",
        TranslateLanguage.FINNISH to "Finlandia",
        TranslateLanguage.FRENCH to "Prancis",
        TranslateLanguage.GALICIAN to "Galisia",
        TranslateLanguage.GEORGIAN to "Georgia",
        TranslateLanguage.GERMAN to "Jerman",
        TranslateLanguage.GREEK to "Yunani",
        TranslateLanguage.GUJARATI to "Gujarati",
        TranslateLanguage.HEBREW to "Ibrani",
        TranslateLanguage.HINDI to "Hindi",
        TranslateLanguage.HUNGARIAN to "Hungaria",
        TranslateLanguage.ICELANDIC to "Islandia",
        TranslateLanguage.INDONESIAN to "Indonesia",
        TranslateLanguage.IRISH to "Irlandia",
        TranslateLanguage.ITALIAN to "Italia",
        TranslateLanguage.JAPANESE to "Jepang",
        TranslateLanguage.KANNADA to "Kannada",
        TranslateLanguage.KOREAN to "Korea",
        TranslateLanguage.LATVIAN to "Latvia",
        TranslateLanguage.LITHUANIAN to "Lithuania",
        TranslateLanguage.MACEDONIAN to "Makedonia",
        TranslateLanguage.MALAY to "Melayu",
        TranslateLanguage.MALTESE to "Malta",
        TranslateLanguage.MARATHI to "Marathi",
        TranslateLanguage.NORWEGIAN to "Norwegia",
        TranslateLanguage.PERSIAN to "Persia (Farsi)",
        TranslateLanguage.POLISH to "Polandia",
        TranslateLanguage.PORTUGUESE to "Portugis",
        TranslateLanguage.ROMANIAN to "Rumania",
        TranslateLanguage.RUSSIAN to "Rusia",
        TranslateLanguage.SLOVAK to "Slowakia",
        TranslateLanguage.SLOVENIAN to "Slovenia",
        TranslateLanguage.SPANISH to "Spanyol",
        TranslateLanguage.SWAHILI to "Swahili",
        TranslateLanguage.SWEDISH to "Swedia",
        TranslateLanguage.TAGALOG to "Tagalog",
        TranslateLanguage.TAMIL to "Tamil",
        TranslateLanguage.TELUGU to "Telugu",
        TranslateLanguage.THAI to "Thailand",
        TranslateLanguage.TURKISH to "Turki",
        TranslateLanguage.UKRAINIAN to "Ukraina",
        TranslateLanguage.URDU to "Urdu",
        TranslateLanguage.VIETNAMESE to "Vietnam",
        TranslateLanguage.WELSH to "Welsh"
    )

    val allLanguages: List<LanguageOption> by lazy {
        TranslateLanguage.getAllLanguages()
            .distinct()
            .map { code ->
                val localeName = Locale.forLanguageTag(code).getDisplayName(indonesianLocale)
                val baseName = customIndonesianNames[code]
                    ?: localeName.takeIf { it.isNotBlank() && it != code }
                        ?.replaceFirstChar { it.titlecase(indonesianLocale) }
                    ?: code.uppercase(Locale.US)
                LanguageOption(code = code, label = "$baseName (${code.uppercase(Locale.US)})")
            }
            .sortedBy { it.label }
    }

    fun findLabel(code: String): String =
        allLanguages.firstOrNull { it.code == code }?.label ?: code.uppercase(Locale.US)
}
