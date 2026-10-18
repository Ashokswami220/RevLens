package com.revlens.app.ui.screens.data

import com.revlens.app.core.platform.PickedFile
import com.revlens.app.core.ui.UiText
import com.revlens.app.domain.calc.CanonicalField
import com.revlens.app.domain.calc.ParsedCsv
import com.revlens.app.domain.calc.ValidationReport

enum class ImportDatasetType {
    SUBSCRIPTIONS,
    MARKETING_SPEND
}

data class DataImportUiState(
    val step: Int = 0, // 0: Type, 1: File, 2: Columns, 3: Review, 4: Done
    val datasetType: ImportDatasetType = ImportDatasetType.SUBSCRIPTIONS,
    val pickedFile: PickedFile? = null,
    val parsedCsv: ParsedCsv? = null,
    val mapping: Map<CanonicalField, String?> = emptyMap(),
    val autoMappedFields: Set<CanonicalField> = emptySet(),
    val validationReport: ValidationReport? = null,
    val skipErrors: Boolean = true,
    val updateBaseline: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
) {
    val canProceedFromStep0: Boolean get() = true
    val canProceedFromStep1: Boolean get() = pickedFile != null && parsedCsv != null
    val canProceedFromStep2: Boolean
        get() {
            // required fields must have non-null mapping
            val required = when (datasetType) {
                ImportDatasetType.SUBSCRIPTIONS -> listOf(
                    CanonicalField.CUSTOMER_ID,
                    CanonicalField.PLAN,
                    CanonicalField.START_DATE,
                    CanonicalField.MRR
                )

                ImportDatasetType.MARKETING_SPEND -> listOf(
                    CanonicalField.MONTH,
                    CanonicalField.SPEND
                )
            }
            return required.all { mapping[it] != null }
        }
    val canCommit: Boolean get() = validationReport != null && validationReport.validCount > 0 && !isLoading
}

sealed interface DataImportEvent {
    data class SelectDatasetType(val type: ImportDatasetType) : DataImportEvent
    data object PickFile : DataImportEvent
    data class FilePicked(val file: PickedFile) : DataImportEvent
    data object RemoveFile : DataImportEvent
    data class UpdateMapping(val field: CanonicalField, val header: String?) : DataImportEvent
    data class ToggleSkipErrors(val enabled: Boolean) : DataImportEvent
    data class ToggleUpdateBaseline(val enabled: Boolean) : DataImportEvent
    data object NextStep : DataImportEvent
    data object PreviousStep : DataImportEvent
    data object CommitImport : DataImportEvent
    data object ResetWizard : DataImportEvent
}

sealed interface DataImportEffect {
    data object NavigateToAnalytics : DataImportEffect
    data class ShowMessage(val text: UiText) : DataImportEffect
}
