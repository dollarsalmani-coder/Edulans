package com.example.model

object SupportedLanguages {
    val all: List<StudyLanguage> = listOf(
        StudyLanguage("en", "English", "English", "Global"),
        StudyLanguage("es", "Spanish", "Español", "Latin America & Spain"),
        StudyLanguage("fr", "French", "Français", "Europe & Africa"),
        StudyLanguage("ar", "Arabic", "العربية", "MENA", isRtl = true),
        StudyLanguage("hi", "Hindi", "हिन्दी", "South Asia"),
        StudyLanguage("zh", "Chinese", "中文", "East Asia"),
        StudyLanguage("bn", "Bengali", "বাংলা", "South Asia"),
        StudyLanguage("pt", "Portuguese", "Português", "Brazil & Europe"),
        StudyLanguage("sw", "Swahili", "Kiswahili", "East Africa"),
        StudyLanguage("ja", "Japanese", "日本語", "East Asia"),
        StudyLanguage("de", "German", "Deutsch", "Central Europe"),
        StudyLanguage("ko", "Korean", "한국어", "East Asia"),
        StudyLanguage("id", "Indonesian", "Bahasa Indonesia", "Southeast Asia"),
        StudyLanguage("ru", "Russian", "Русский", "Eastern Europe"),
        StudyLanguage("tr", "Turkish", "Türkçe", "Anatolia")
    )

    fun findByCode(code: String): StudyLanguage {
        return all.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: all.first()
    }
}
