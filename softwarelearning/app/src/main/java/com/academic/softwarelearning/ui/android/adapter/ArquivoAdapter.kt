package com.academic.softwarelearning.ui.android.adapter

import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R

class ArquivoAdapter(
    private val arquivos: MutableList<Uri>
) : RecyclerView.Adapter<ArquivoAdapter.ArquivoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArquivoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_arquivo, parent, false)
        return ArquivoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArquivoViewHolder, position: Int) {
        val arquivoUri = arquivos[position]
        holder.bind(arquivoUri)
    }

    override fun getItemCount(): Int = arquivos.size

    inner class ArquivoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val arquivoTextView: TextView = itemView.findViewById(R.id.textArquivo)
        private val btnRemover: Button = itemView.findViewById(R.id.btnRemoverArquivo)

        fun bind(uri: Uri) {
            arquivoTextView.text = getFileName(uri)
            btnRemover.setOnClickListener {
                val pos = adapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                arquivos.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }

        private fun getFileName(uri: Uri): String {
            var name = uri.lastPathSegment.orEmpty()
            val cursor: Cursor? = itemView.context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) {
                        name = it.getString(idx)
                    }
                }
            }
            return name
        }
    }
}
