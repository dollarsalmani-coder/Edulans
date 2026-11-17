package com.example.data.local

import com.example.model.EducationalFact
import com.example.model.PracticalSolution
import com.example.model.QuickQuizQuestion
import com.example.model.StructurePart
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class StudyJsonConverters {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val structureListAdapter = moshi.adapter<List<StructurePart>>(
        Types.newParameterizedType(List::class.java, StructurePart::class.java)
    )
    private val factListAdapter = moshi.adapter<List<EducationalFact>>(
        Types.newParameterizedType(List::class.java, EducationalFact::class.java)
    )
    private val solutionListAdapter = moshi.adapter<List<PracticalSolution>>(
        Types.newParameterizedType(List::class.java, PracticalSolution::class.java)
    )
    private val quizAdapter = moshi.adapter(QuickQuizQuestion::class.java)

    fun fromStructureList(items: List<StructurePart>): String =
        structureListAdapter.toJson(items) ?: "[]"

    fun toStructureList(json: String): List<StructurePart> =
        runCatching { structureListAdapter.fromJson(json) }.getOrNull().orEmpty()

    fun fromFactList(items: List<EducationalFact>): String =
        factListAdapter.toJson(items) ?: "[]"

    fun toFactList(json: String): List<EducationalFact> =
        runCatching { factListAdapter.fromJson(json) }.getOrNull().orEmpty()

    fun fromSolutionList(items: List<PracticalSolution>): String =
        solutionListAdapter.toJson(items) ?: "[]"

    fun toSolutionList(json: String): List<PracticalSolution> =
        runCatching { solutionListAdapter.fromJson(json) }.getOrNull().orEmpty()

    fun fromQuiz(quiz: QuickQuizQuestion): String =
        quizAdapter.toJson(quiz)

    fun toQuiz(json: String): QuickQuizQuestion? =
        runCatching { quizAdapter.fromJson(json) }.getOrNull()
}
