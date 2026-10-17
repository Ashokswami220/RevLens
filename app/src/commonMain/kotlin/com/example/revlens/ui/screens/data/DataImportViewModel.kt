package com.example.revlens.ui.screens.data

import androidx.lifecycle.viewModelScope
import com.example.revlens.core.platform.FilePicker
import com.example.revlens.core.platform.PickedFile
import com.example.revlens.core.ui.MviViewModel
import com.example.revlens.core.ui.UiText
import com.example.revlens.core.util.IdGenerator
import com.example.revlens.domain.calc.CanonicalField
import com.example.revlens.domain.calc.ColumnAutoMapper
import com.example.revlens.domain.calc.CsvParser
import com.example.revlens.domain.calc.ImportValidator
import com.example.revlens.domain.context.WorkspaceContext
import com.example.revlens.domain.model.DataSource
import com.example.revlens.domain.model.Dataset
import com.example.revlens.domain.repository.AnalyticsRepository
import kotlinx.coroutines.launch

class DataImportViewModel(
    private val analyticsRepository: AnalyticsRepository,
    private val workspaceContext: WorkspaceContext,
    private val filePicker: FilePicker
) : MviViewModel<DataImportUiState, DataImportEvent, DataImportEffect>(
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
            is DataImportEvent.ToggleUpdateBaseline -> update { copy(updateBaseline = event.enabled) }
            DataImportEvent.NextStep -> nextStep()
            DataImportEvent.PreviousStep -> previousStep()
            DataImportEvent.CommitImport -> commitImport()
            DataImportEvent.ResetWizard -> update { DataImportUiState() }
        }
    }

    private fun pickFile() {
        viewModelScope.launch {
            try {
                val file = filePicker.pickCsv()
                if (file != null) {
                    processFile(file)
                }
            } catch (e: Exception) {
                update { copy(errorMessage = UiText.DynamicString(e.message ?: "Failed to pick file")) }
            }
        }
    }

    private fun processFile(file: PickedFile) {
        try {
            val text = file.readText()
            val parsed = CsvParser.parse(text)
            if (parsed.headers.isEmpty()) {
                update {
                    copy(
                        errorMessage = UiText.DynamicString("The selected CSV file appears to be empty.")
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

        val updatedAuto = state.value.autoMappedFields.toMutableSet().apply {
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
        val report = state.value.validationReport ?: return
        if (report.validCount == 0) return

        viewModelScope.launch {
            update { copy(isLoading = true) }
            try {
                val wsId = workspaceContext.activeWorkspaceId.value ?: "ws_default"
                val existing = analyticsRepository.getDataset(wsId)
                val fileName = state.value.pickedFile?.name ?: "import.csv"

                val updatedDataset = when (state.value.datasetType) {
                    ImportDatasetType.SUBSCRIPTIONS -> {
                        existing?.copy(
                            subscriptions = report.validRecords,
                            filename = fileName,
                            source = DataSource.Csv
                        ) ?: Dataset(
                            id = IdGenerator.newId("ds"),
                            workspaceId = wsId,
                            source = DataSource.Csv,
                            filename = fileName,
                            subscriptions = report.validRecords
                        )
                    }
                    ImportDatasetType.MARKETING_SPEND -> {
                        existing?.copy(
                            spend = report.validSpendRecords,
                            filename = fileName,
                            source = DataSource.Csv
                        ) ?: Dataset(
                            id = IdGenerator.newId("ds"),
                            workspaceId = wsId,
                            source = DataSource.Csv,
                            filename = fileName,
                            spend = report.validSpendRecords
                        )
                    }
                }

                analyticsRepository.saveDataset(updatedDataset)
                update { copy(isLoading = false, step = 4) }
                emit(DataImportEffect.ShowMessage(UiText.DynamicString("Successfully imported ${report.validCount} records")))
            } catch (e: Exception) {
                update {
                    copy(
                        isLoading = false,
                        errorMessage = UiText.DynamicString(e.message ?: "Failed to import dataset.")
                    )
                }
            }
        }
    }
}
