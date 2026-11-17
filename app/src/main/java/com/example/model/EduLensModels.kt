package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StructurePart(
    val layerOrder: Int,
    val name: String,
    val materialOrRole: String,
    val functionDescription: String
)

@JsonClass(generateAdapter = true)
data class EducationalFact(
    val discipline: String,
    val title: String,
    val detail: String
)

@JsonClass(generateAdapter = true)
data class PracticalSolution(
    val problemTitle: String,
    val solutionSteps: String,
    val careOrSustainabilityTip: String
)

@JsonClass(generateAdapter = true)
data class QuickQuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ObjectAnalysisResult(
    val id: String,
    val objectTitle: String,
    val scientificOrTechnicalName: String,
    val category: String,
    val summary: String,
    val languageCode: String,
    val structures: List<StructurePart>,
    val facts: List<EducationalFact>,
    val solutions: List<PracticalSolution>,
    val quiz: QuickQuizQuestion?,
    val imageSource: String?,
    val estimatedPayloadKb: Int,
    val isSamplePlaceholder: Boolean = true,
    val isSavedOffline: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class StudyLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val regionLabel: String,
    val isRtl: Boolean = false
)

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}
