package com.academic.softwarelearning.infrastructure.repository

import android.net.Uri

interface FileAddedListener {
    fun onArquivoAdicionado(uri: Uri)
}