package com.revlens.app.ui.screens.data

import com.revlens.app.core.platform.PickedFile
import com.revlens.app.core.ui.MviViewModel
import com.revlens.app.core.ui.UiText
import com.revlens.app.domain.calc.CanonicalField
import com.revlens.app.domain.calc.ColumnAutoMapper
import com.revlens.app.domain.calc.CsvParser
import com.revlens.app.domain.calc.ImportValidator

class DataImportViewModel : MviViewModel<DataImportUiState, DataImportEvent, DataImportEffect>(
    DataImportUiState()
) {

    override fun onEvent(event: DataImportEvent) {
        when (event) {
            is DataImportEvent.SelectDatasetType -> {
                update {
                    copy(
                        datasetType = event.type,
                        pickedFile = null,
                        parsedCsv = null,
                        mapping = emptyMap(),
                        autoMappedFields = emptySet(),
                        validationReport = null,
                        errorMessage = null
                    )
                }
            }

            DataImportEvent.PickFile -> pickFile()
            is DataImportEvent.FilePicked -> processFile(event.file)
            DataImportEvent.RemoveFile -> {
                update {
                    copy(
                        pickedFile = null,
                        parsedCsv = null,
                        mapping = emptyMap(),
                        autoMappedFields = emptySet(),
                        validationReport = null,
                        errorMessage = null
                    )
                }
            }

            is DataImportEvent.UpdateMapping -> updateMapping(event.field, event.header)
            is DataImportEvent.ToggleSkipErrors -> update { copy(skipErrors = event.enabled) }
            is DataImportEvent.ToggleUpdateBaseline -> update {
                copy(updateBaseline = event.enabled)
            }

            DataImportEvent.NextStep -> nextStep()
            DataImportEvent.PreviousStep -> previousStep()
            DataImportEvent.CommitImport -> commitImport()
            DataImportEvent.ResetWizard -> update { DataImportUiState() }
        }
    }

    private fun pickFile() {
        val sampleCsv = when (state.value.datasetType) {
            ImportDatasetType.SUBSCRIPTIONS -> """
                customer_id,plan,start_date,end_date,mrr,channel,country
                cust_001,Growth,2026-01-15,,149.00,Google Ads,US
                cust_002,Enterprise,2026-02-01,,499.00,Direct Sales,UK
                cust_003,Starter,2026-03-10,,49.00,Organic Search,DE
            """.trimIndent()

            ImportDatasetType.MARKETING_SPEND -> """
                month,channel,spend,new_customers
                2026-01,Google Ads,2500.00,18
                2026-02,LinkedIn Ads,1800.00,12
                2026-03,Content Marketing,1200.00,8
            """.trimIndent()
        }
        val bytes = sampleCsv.encodeToByteArray()
        val picked = PickedFile(
            name = if (state.value.datasetType == ImportDatasetType.SUBSCRIPTIONS) "subscriptions_sample.csv" else "marketing_spend_sample.csv",
            sizeBytes = bytes.size.toLong(),
            content = bytes
        )
        processFile(picked)
    }

    private fun processFile(file: PickedFile) {
        try {
            val text = file.readText()
            val parsed = CsvParser.parse(text)
            if (parsed.headers.isEmpty()) {
                update {
                    copy(
                        errorMessage = UiText.DynamicString(
                            "The selected CSV file appears to be empty."
                        )
                    )
                }
                return
            }

            val targetFields = when (state.value.datasetType) {
                ImportDatasetType.SUBSCRIPTIONS -> listOf(
                    CanonicalField.CUSTOMER_ID,
                    CanonicalField.PLAN,
                    CanonicalField.START_DATE,
                    CanonicalField.END_DATE,
                    CanonicalField.MRR,
                    CanonicalField.CHANNEL,
                    CanonicalField.COUNTRY
                )

                ImportDatasetType.MARKETING_SPEND -> listOf(
                    CanonicalField.MONTH,
                    CanonicalField.SPEND,
                    CanonicalField.CHANNEL,
                    CanonicalField.NEW_CUSTOMERS
                )
            }

            val autoMap = ColumnAutoMapper.autoMap(parsed.headers, targetFields)
            val mappedFields = autoMap.filterValues { it != null }.keys

            update {
                copy(
                    pickedFile = file,
                    parsedCsv = parsed,
                    mapping = autoMap,
                    autoMappedFields = mappedFields,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            update {
                copy(
                    errorMessage = UiText.DynamicString(e.message ?: "Failed to parse CSV file.")
                )
            }
        }
    }

    private fun updateMapping(field: CanonicalField, header: String?) {
        val currentMapping = state.value.mapping.toMutableMap()
        currentMapping[field] = header

        val updatedAuto = state.value.autoMappedFields.toMutableSet()
            .apply {
                remove(field)
            }

        update {
            copy(
                mapping = currentMapping,
                autoMappedFields = updatedAuto
            )
        }

        if (state.value.validationReport != null && state.value.parsedCsv != null) {
            computeValidation()
        }
    }

    private fun nextStep() {
        val current = state.value.step
        when (current) {
            0 -> if (state.value.canProceedFromStep0) update { copy(step = 1) }
            1 -> if (state.value.canProceedFromStep1) update { copy(step = 2) }
            2 -> {
                if (state.value.canProceedFromStep2) {
                    computeValidation()
                    update { copy(step = 3) }
                }
            }

            3 -> commitImport()
        }
    }

    private fun previousStep() {
        val current = state.value.step
        if (current > 0) {
            update { copy(step = current - 1) }
        }
    }

    private fun computeValidation() {
        val csv = state.value.parsedCsv ?: return
        val mapping = state.value.mapping

        val report = when (state.value.datasetType) {
            ImportDatasetType.SUBSCRIPTIONS -> {
                ImportValidator.validateSubscriptions(csv.headers, csv.rows, mapping)
            }

            ImportDatasetType.MARKETING_SPEND -> {
                ImportValidator.validateMarketingSpend(csv.headers, csv.rows, mapping)
            }
        }

        update { copy(validationReport = report) }
    }

    private fun commitImport() {
        val report = state.value.validationReport
        val count = report?.validCount ?: 0
        update { copy(isLoading = false, step = 4) }
        emit(
            DataImportEffect.ShowMessage(
                UiText.DynamicString("Successfully imported ${if (count > 0) "$count records" else "dataset"}")
            )
        )
    }
}
