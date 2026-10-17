package com.example.revlens.domain.model

enum class ReportTemplate(val displayName: String, val description: String) {
    INVESTOR_UPDATE("Investor Update", "Key SaaS metrics, MRR trajectory, runway & top highlights"),
    MONTHLY_REVENUE("Monthly Revenue", "MRR bridge, churn decay, ARPU & plan-tier breakdown"),
    SCENARIO_COMPARISON("Scenario Comparison", "Side-by-side impact analysis across modeled scenarios"),
    FORECAST_SUMMARY("Forecast Summary", "12-month projections, cash runway & target milestones")
}

enum class ReportFormat(val extension: String, val mimeType: String, val label: String) {
    PDF("pdf", "application/pdf", "PDF Document"),
    CSV("csv", "text/csv", "CSV Spreadsheet")
}

enum class ReportSection(val title: String, val description: String) {
    KPIS("Executive KPIs", "MRR, ARR, ARPU, Churn, CAC, LTV summary"),
    MRR_BRIDGE("MRR Bridge", "New, expansion, contraction, and churn breakdown"),
    SCENARIOS("Scenario Models", "Base vs simulated pricing and growth targets"),
    FORECAST("12-Month Forecast", "Projected monthly compounding revenue"),
    COHORTS("Retention Cohorts", "Logo and revenue retention decay tables"),
    NOTES("Executive Notes", "Strategic commentary and next steps")
}

enum class PeriodPreset(val displayName: String) {
    LAST_30_DAYS("Last 30 Days"),
    THIS_QUARTER("This Quarter"),
    YEAR_TO_DATE("Year to Date"),
    CUSTOM("Custom")
}

data class ReportSnapshot(
    val id: String,
    val workspaceId: String,
    val title: String,
    val template: ReportTemplate,
    val format: ReportFormat,
    val periodStart: String,
    val periodEnd: String,
    val includedSections: List<ReportSection>,
    val createdAt: Long,
    val pageCount: Int = 1,
    val fileSizeBytes: Long = 1024L,
    val csvData: String = "",
    val summaryMetrics: Map<String, String> = emptyMap(),
    val notes: String = ""
)
