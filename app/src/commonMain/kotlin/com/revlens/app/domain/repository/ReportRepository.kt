package com.revlens.app.domain.repository

import com.revlens.app.domain.model.ReportSnapshot
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    val reportsFlow: Flow<List<ReportSnapshot>>
    suspend fun saveReport(snapshot: ReportSnapshot)
    suspend fun deleteReport(reportId: String)
    suspend fun getReportById(reportId: String): ReportSnapshot?
}
