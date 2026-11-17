package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.EducationalFact
import com.example.model.ObjectAnalysisResult
import com.example.model.PracticalSolution
import com.example.model.QuickQuizQuestion
import com.example.model.StructurePart

@Entity(tableName = "saved_studies")
data class SavedStudyEntity(
    @PrimaryKey val id: String,
    val objectTitle: String,
    val scientificOrTechnicalName: String,
    val category: String,
    val summary: String,
    val languageCode: String,
    val structuresJson: String,
    val factsJson: String,
    val solutionsJson: String,
    val quizJson: String?,
    val imageSource: String?,
    val estimatedPayloadKb: Int,
    val isSamplePlaceholder: Boolean,
    val timestamp: Long
)

fun SavedStudyEntity.toDomain(converters: StudyJsonConverters): ObjectAnalysisResult {
    return ObjectAnalysisResult(
        id = id,
        objectTitle = objectTitle,
        scientificOrTechnicalName = scientificOrTechnicalName,
        category = category,
        summary = summary,
        languageCode = languageCode,
        structures = converters.toStructureList(structuresJson),
        facts = converters.toFactList(factsJson),
        solutions = converters.toSolutionList(solutionsJson),
        quiz = quizJson?.let { converters.toQuiz(it) },
        imageSource = imageSource,
        estimatedPayloadKb = estimatedPayloadKb,
        isSamplePlaceholder = isSamplePlaceholder,
        isSavedOffline = true,
        timestamp = timestamp
    )
}

fun ObjectAnalysisResult.toEntity(converters: StudyJsonConverters): SavedStudyEntity {
    return SavedStudyEntity(
        id = id,
        objectTitle = objectTitle,
        scientificOrTechnicalName = scientificOrTechnicalName,
        category = category,
        summary = summary,
        languageCode = languageCode,
        structuresJson = converters.fromStructureList(structures),
        factsJson = converters.fromFactList(facts),
        solutionsJson = converters.fromSolutionList(solutions),
        quizJson = quiz?.let { converters.fromQuiz(it) },
        imageSource = imageSource,
        estimatedPayloadKb = estimatedPayloadKb,
        isSamplePlaceholder = isSamplePlaceholder,
        timestamp = timestamp
    )
}
