package com.academic.softwarelearning.domain.model

import android.net.Uri

class FileManager {
    private var anexos: MutableList<Uri> = mutableListOf()

    fun addArquivo(uri: Uri) {
        anexos.add(uri)
    }

    fun getArquivos(): MutableList<Uri> = anexos

    fun clearArquivos() {
        anexos.clear()
    }
}
