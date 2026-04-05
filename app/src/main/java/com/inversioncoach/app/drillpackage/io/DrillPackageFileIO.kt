package com.inversioncoach.app.drillpackage.io

import android.content.Context
import android.net.Uri
import com.inversioncoach.app.drillpackage.model.DrillPackage
import java.io.File

class DrillPackageFileIO(private val context: Context) {
    private val exportDir = context.filesDir.resolve("drill_packages/exports").apply { mkdirs() }

    fun exportToFile(pkg: DrillPackage, fileName: String = "${pkg.manifest.packageId}_${System.currentTimeMillis()}.json"): File {
        val output = exportDir.resolve(fileName)
        output.writeText(DrillPackageJsonCodec.encode(pkg))
        return output
    }

    fun importFromUri(uri: Uri): DrillPackage {
        val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Could not read drill package from $uri")
        return DrillPackageJsonCodec.decode(raw)
    }
}
