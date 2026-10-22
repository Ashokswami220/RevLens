package com.revlens.app.data

import com.revlens.app.domain.model.Dataset
import com.revlens.app.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultAnalyticsRepository : AnalyticsRepository {
    private val datasets = mutableMapOf<String, Dataset>()
    private val _datasetFlow = MutableStateFlow<Dataset?>(null)
    override val datasetFlow: Flow<Dataset?> = _datasetFlow.asStateFlow()

    override suspend fun getDataset(workspaceId: String): Dataset? {
        return datasets[workspaceId]
    }

    override suspend fun saveDataset(dataset: Dataset) {
        datasets[dataset.workspaceId] = dataset
        _datasetFlow.value = dataset
    }

    override suspend fun deleteDataset(workspaceId: String) {
        datasets.remove(workspaceId)
        _datasetFlow.value = null
    }

    override suspend fun clearLocalData() {
        datasets.clear()
        _datasetFlow.value = null
    }
}
