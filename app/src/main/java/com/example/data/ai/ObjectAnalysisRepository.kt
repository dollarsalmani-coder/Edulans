package com.example.data.ai

import com.example.model.ObjectAnalysisResult
import com.example.model.StudyLanguage

/**
 * Single interface routing all object analysis and multilingual translation requests.
 * Live Gemini vision & translation can be plugged in via Firebase AI Logic when enabled.
 */
interface ObjectAnalysisRepository {
    suspend fun analyzeObject(
        queryText: String,
        compressedImagePath: String?,
        payloadKb: Int,
        targetLanguage: StudyLanguage,
        lowBandwidthMode: Boolean
    ): ObjectAnalysisResult

    suspend fun translateAnalysis(
        existing: ObjectAnalysisResult,
        targetLanguage: StudyLanguage
    ): ObjectAnalysisResult

    companion object {
        /**
         * Builds the multilingual educational prompt passed to the analysis model,
         * explicitly instructing that all fields be returned in [targetLanguage].
         */
        fun buildAnalysisPrompt(
            queryText: String,
            targetLanguage: StudyLanguage,
            lowBandwidthMode: Boolean
        ): String {
            val subject = queryText.trim().ifBlank { "the photographed object" }
            val verbosity = if (lowBandwidthMode) {
                "Keep descriptions concise for low-bandwidth delivery."
            } else {
                "Provide rich, clear educational explanations."
            }
            return buildString {
                append("Analyze $subject and return structured educational content ")
                append("(overview, structural layers, science/history facts, practical solutions, and a quick quiz). ")
                append("Preferred output language: ${targetLanguage.englishName} ")
                append("(${targetLanguage.nativeName}, locale=${targetLanguage.code}). ")
                append("Return all user-facing titles, summaries, facts, and solutions in ${targetLanguage.englishName}. ")
                append(verbosity)
            }
        }
    }
}
