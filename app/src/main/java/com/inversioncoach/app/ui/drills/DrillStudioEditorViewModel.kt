package com.inversioncoach.app.ui.drills

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inversioncoach.app.drills.studio.DrillCatalogDraftStore
import com.inversioncoach.app.drills.studio.DrillCatalogImportExportManager
import com.inversioncoach.app.drills.studio.DrillStudioDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class DrillStudioEditorState(
    val drills: List<DrillCatalogDraftStore.DrillPickerItem> = emptyList(),
    val selectedDrillId: String = "",
    val baseline: DrillStudioDocument? = null,
    val working: DrillStudioDocument? = null,
    val status: String? = null,
) {
    val selectedMeta: DrillCatalogDraftStore.DrillPickerItem?
        get() = drills.firstOrNull { it.id == selectedDrillId }

    val hasUnsavedChanges: Boolean
        get() = baseline != null && working != null && baseline != working
}

class DrillStudioEditorViewModel(
    context: Context,
) : ViewModel() {
    private val store = DrillCatalogDraftStore(context)
    private val importExport = DrillCatalogImportExportManager(context, store)

    private val _state = MutableStateFlow(DrillStudioEditorState())
    val state: StateFlow<DrillStudioEditorState> = _state.asStateFlow()

    fun initialize(initialDrillId: String?) {
        refreshAndSelect(initialDrillId)
    }

    fun selectDrill(drillId: String) {
        refreshAndSelect(drillId)
    }

    fun updateWorking(updated: DrillStudioDocument) {
        _state.value = _state.value.copy(working = updated)
    }

    fun saveDraft() {
        val draft = _state.value.working ?: return
        runCatching {
            store.saveDraft(draft)
            refreshAndSelect(draft.id, status = "Draft saved")
        }.onFailure { throwable ->
            _state.value = _state.value.copy(status = "Save failed: ${throwable.message}")
        }
    }

    fun resetDraft() {
        val draft = _state.value.working ?: return
        runCatching {
            store.resetDraft(draft.id)
            refreshAndSelect(draft.id, status = "Draft reset")
        }.onFailure { throwable ->
            _state.value = _state.value.copy(status = "Reset failed: ${throwable.message}")
        }
    }

    fun duplicateSelected() {
        val draft = _state.value.working ?: return
        runCatching {
            val duplicate = store.duplicate(draft.id)
            refreshAndSelect(duplicate.id, status = "Duplicated to ${duplicate.displayName}")
        }.onFailure { throwable ->
            _state.value = _state.value.copy(status = "Duplicate failed: ${throwable.message}")
        }
    }

    fun importDraft(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val imported = importExport.importDraft(uri)
                refreshAndSelect(imported.id, status = "Imported ${imported.displayName}")
            }.onFailure { throwable ->
                _state.value = _state.value.copy(status = "Import failed: ${throwable.message}")
            }
        }
    }

    fun exportDraft(): File? {
        val draft = _state.value.working ?: return null
        return runCatching {
            importExport.exportDraft(draft).also { file ->
                _state.value = _state.value.copy(status = "Exported ${file.name}")
            }
        }.getOrElse { throwable ->
            _state.value = _state.value.copy(status = "Export failed: ${throwable.message}")
            null
        }
    }

    private fun refreshAndSelect(requestedId: String?, status: String? = null) {
        val drills = store.listDrills()
        val selectedId = requestedId
            ?.takeIf { id -> drills.any { it.id == id } }
            ?: drills.firstOrNull()?.id
            .orEmpty()
        val baseline = selectedId.takeIf { it.isNotBlank() }?.let(store::loadForEditor)
        _state.value = DrillStudioEditorState(
            drills = drills,
            selectedDrillId = selectedId,
            baseline = baseline,
            working = baseline,
            status = status,
        )
    }
}
