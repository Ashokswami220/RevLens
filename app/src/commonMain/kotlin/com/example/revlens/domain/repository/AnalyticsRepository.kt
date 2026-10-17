package com.example.revlens.domain.repository

import com.example.revlens.domain.model.Dataset
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    val datasetFlow: Flow<Dataset?>
    suspend fun getDataset(workspaceId: String): Dataset?
    suspend fun saveDataset(dataset: Dataset)
    suspend fun deleteDataset(workspaceId: String)
}
