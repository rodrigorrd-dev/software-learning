package com.academic.softwarelearning.domain.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class VisualizadorArquivo(private val context: Context) {
    fun abrir(uri: Uri, mimeType: String) {
        try {
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Fallback para qualquer app se nenhum específico resolver
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Buscar apps preferidos (Google e Microsoft)
            val preferredApps = listOf(
                "com.google.android.apps.docs",
                "com.google.android.apps.photos",
                "com.google.android.gm",
                "com.microsoft.office.word",
                "com.microsoft.office.excel",
                "com.microsoft.skydrive"
            )

            val pm = context.packageManager
            val resolvedIntents = mutableListOf<Intent>()

            preferredApps.forEach { packageName ->
                val intent = openIntent.clone() as Intent
                intent.setPackage(packageName)
                if (intent.resolveActivity(pm) != null) {
                    resolvedIntents.add(intent)
                }
            }

            val chooser = Intent.createChooser(openIntent, "Abrir arquivo com")
            if (resolvedIntents.isNotEmpty()) {
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, resolvedIntents.toTypedArray())
            }

            context.startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Arquivo salvo, mas não foi possível abrir: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
