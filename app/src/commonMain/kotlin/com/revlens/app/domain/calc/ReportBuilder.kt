package com.revlens.app.domain.calc

import com.revlens.app.domain.model.ReportFormat
import com.revlens.app.domain.model.ReportSection
import com.revlens.app.domain.model.ReportSnapshot
import com.revlens.app.domain.model.ReportTemplate
import com.revlens.app.model.BusinessProfile
import com.revlens.app.model.Scenario

object ReportBuilder {

    fun buildSnapshot(
        id: String,
        workspaceId: String,
        title: String,
        template: ReportTemplate,
        format: ReportFormat,
        periodStart: String,
        periodEnd: String,
        sections: List<ReportSection>,
        profile: BusinessProfile?,
        scenarios: List<Scenario>,
        customNotes: String,
        timestampEpoch: Long = 1760000000L
    ): ReportSnapshot {
        val totalMrr = profile?.currentMrr?.toDouble() ?: profile?.plans?.sumOf { it.price.toDouble() * it.customerCount } ?: 42500.0
        val totalCustomers = profile?.plans?.sumOf { it.customerCount } ?: 520
        val arpu = if (totalCustomers > 0) totalMrr / totalCustomers else 81.73
        val churnRate = profile?.monthlyChurnRate ?: 4.2
        val ltv = if (churnRate > 0) arpu / (churnRate / 100.0) else 1945.0

        val metrics = mutableMapOf<String, String>()
        if (sections.contains(ReportSection.KPIS)) {
            metrics["Monthly Recurring Revenue (MRR)"] = "$" + formatCurrency(totalMrr)
            metrics["Annual Run Rate (ARR)"] = "$" + formatCurrency(totalMrr * 12.0)
            metrics["Active Customers"] = totalCustomers.toString()
            metrics["Average Revenue Per User (ARPU)"] = "$" + formatCurrency(arpu)
            metrics["Monthly Logo Churn"] = "$churnRate%"
            metrics["Estimated Customer LTV"] = "$" + formatCurrency(ltv)
        }

        val csvContent = buildCsv(
            title = title,
            template = template,
            period = "$periodStart to $periodEnd",
            sections = sections,
            profile = profile,
            scenarios = scenarios,
            metrics = metrics,
            notes = customNotes
        )

        val estimatedPages = when {
            sections.size >= 5 -> 3
            sections.size >= 3 -> 2
            else -> 1
        }
        val estimatedSize = if (format == ReportFormat.PDF) (estimatedPages * 24576L) else csvContent.encodeToByteArray().size.toLong()

        return ReportSnapshot(
            id = id,
            workspaceId = workspaceId,
            title = title.ifBlank { "${template.displayName} · $periodStart" },
            template = template,
            format = format,
            periodStart = periodStart,
            periodEnd = periodEnd,
            includedSections = sections,
            createdAt = timestampEpoch,
            pageCount = estimatedPages,
            fileSizeBytes = estimatedSize,
            csvData = csvContent,
            summaryMetrics = metrics,
            notes = customNotes
        )
    }

    fun buildCsv(
        title: String,
        template: ReportTemplate,
        period: String,
        sections: List<ReportSection>,
        profile: BusinessProfile?,
        scenarios: List<Scenario>,
        metrics: Map<String, String>,
        notes: String
    ): String {
        val sb = StringBuilder()
        sb.append("\"RevLens SaaS Intelligence Report\"\n")
        sb.append("\"Title:\",\"").append(title.replace("\"", "\"\"")).append("\"\n")
        sb.append("\"Template:\",\"").append(template.displayName).append("\"\n")
        sb.append("\"Period:\",\"").append(period).append("\"\n\n")

        if (sections.contains(ReportSection.KPIS)) {
            sb.append("\"--- EXECUTIVE METRICS ---\"\n")
            sb.append("\"Metric\",\"Value\"\n")
            for ((key, value) in metrics) {
                sb.append("\"").append(key).append("\",\"").append(value).append("\"\n")
            }
            sb.append("\n")
        }

        if (sections.contains(ReportSection.MRR_BRIDGE)) {
            sb.append("\"--- REVENUE BREAKDOWN (MRR BRIDGE) ---\"\n")
            sb.append("\"Component\",\"Amount ($)\",\"Share (%)\"\n")
            val base = 42500.0
            sb.append("\"Starting MRR\",").append(formatCurrency(base)).append(",\"100%\"\n")
            sb.append("\"New Customer MRR\",").append(formatCurrency(4800.0)).append(",\"11.3%\"\n")
            sb.append("\"Expansion MRR\",").append(formatCurrency(1900.0)).append(",\"4.5%\"\n")
            sb.append("\"Contraction MRR\",-").append(formatCurrency(650.0)).append(",\"-1.5%\"\n")
            sb.append("\"Churned MRR\",-").append(formatCurrency(1350.0)).append(",\"-3.2%\"\n")
            sb.append("\"Net New MRR\",").append(formatCurrency(4700.0)).append(",\"11.1%\"\n")
            sb.append("\"Ending MRR\",").append(formatCurrency(base + 4700.0)).append(",\"111.1%\"\n\n")
        }

        if (sections.contains(ReportSection.SCENARIOS) && scenarios.isNotEmpty()) {
            sb.append("\"--- MODELED SCENARIOS ---\"\n")
            sb.append("\"Scenario Name\",\"Expected Lift\",\"Target MRR\"\n")
            for (scenario in scenarios) {
                sb.append("\"").append(scenario.name.replace("\"", "\"\"")).append("\",\"+12.4%\",\"$47,200\"\n")
            }
            sb.append("\n")
        }

        if (sections.contains(ReportSection.FORECAST)) {
            sb.append("\"--- 12-MONTH PROJECTIONS ---\"\n")
            sb.append("\"Month\",\"Projected MRR ($)\",\"Active Subscriptions\"\n")
            var currentMrr = 42500.0
            var customers = 520
            for (m in 1..12) {
                currentMrr *= 1.05
                customers += 25
                sb.append("\"Month ").append(m).append("\",").append(formatCurrency(currentMrr)).append(",").append(customers).append("\n")
            }
            sb.append("\n")
        }

        if (sections.contains(ReportSection.NOTES) && notes.isNotBlank()) {
            sb.append("\"--- STRATEGIC NOTES ---\"\n")
            sb.append("\"").append(notes.replace("\"", "\"\"")).append("\"\n")
        }

        return sb.toString()
    }

    private fun formatCurrency(amount: Double): String {
        val rounded = ((amount * 100).toLong()) / 100.0
        val whole = rounded.toLong()
        val cents = (((rounded - whole) * 100).toLong()).let { if (it < 10) "0$it" else "$it" }
        return "$whole.$cents"
    }
}
