package com.example.data.local

import com.example.data.ai.SampleObjectAnalysisRepository
import com.example.model.ObjectAnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudyHistoryRepository(
    private val dao: SavedStudyDao,
    private val converters: StudyJsonConverters = StudyJsonConverters()
) {
    val savedStudies: Flow<List<ObjectAnalysisResult>> = dao.observeAllStudies()
        .map { entities -> entities.map { it.toDomain(converters) } }

    fun searchSavedStudies(query: String): Flow<List<ObjectAnalysisResult>> {
        return dao.searchStudies(query).map { list -> list.map { it.toDomain(converters) } }
    }

    suspend fun ensureStarterPackSeeded(sampleRepo: SampleObjectAnalysisRepository) {
        if (dao.countStudies() == 0) {
            val starters = sampleRepo.getStarterOfflineGuides().map { it.toEntity(converters) }
            dao.insertAllIfAbsent(starters)
        }
    }

    suspend fun saveForOffline(study: ObjectAnalysisResult) {
        dao.upsertStudy(study.copy(isSavedOffline = true).toEntity(converters))
    }

    suspend fun getStudyById(id: String): ObjectAnalysisResult? {
        return dao.getStudyById(id)?.toDomain(converters)
    }

    suspend fun removeFromOffline(id: String) {
        dao.deleteStudyById(id)
    }
}
