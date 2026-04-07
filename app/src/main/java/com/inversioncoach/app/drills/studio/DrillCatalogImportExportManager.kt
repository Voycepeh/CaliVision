package com.inversioncoach.app.drills.studio

import android.content.Context
import android.net.Uri
import com.inversioncoach.app.drillpackage.importing.DrillPackageImportPipeline
import com.inversioncoach.app.drillpackage.importing.DrillPackageImportResult
import java.io.File
import org.json.JSONObject

/**
 * Transitional mobile authoring helper.
 *
 * Runtime package consumption should prefer the portable package import pipeline under
 * `drillpackage/*` so Studio-authored packages stay the primary cross-repo contract.
 */
class DrillCatalogImportExportManager(private val context: Context, private val draftStore: DrillCatalogDraftStore) {
    private val exportDir = context.filesDir.resolve("drill_studio/exports").apply { mkdirs() }

    fun exportDraft(document: DrillStudioDocument): File {
        val file = exportDir.resolve("${document.id}_${System.currentTimeMillis()}.json")
        file.writeText(DrillStudioCodec.toJson(document).toString(2))
        return file
    }

    fun importDraft(uri: Uri): DrillStudioDocument {
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Could not read file")

        val parsed = runCatching { DrillStudioCodec.fromJson(JSONObject(content)) }
            .getOrElse {
                when (val portable = DrillPackageImportPipeline.parseAndValidate(content)) {
                    is DrillPackageImportResult.Success -> {
                        val first = portable.runtimeCatalog.drills.firstOrNull()
                            ?: error("Portable package does not contain drills")
                        DrillStudioMapper.fromCatalog(first)
                    }
                    is DrillPackageImportResult.DecodeFailure -> error("Could not parse import file as Drill Studio document or portable package: ${portable.message}")
                    is DrillPackageImportResult.ValidationFailure -> {
                        val details = (portable.errors + portable.warnings).joinToString(" | ")
                        error("Portable package failed validation: $details")
                    }
                    is DrillPackageImportResult.MappingFailure -> {
                        val details = (listOf(portable.message) + portable.warnings).joinToString(" | ")
                        error("Portable package could not be mapped to runtime catalog: $details")
                    }
                }
            }

        draftStore.saveDraft(parsed)
        return parsed
    }
}
