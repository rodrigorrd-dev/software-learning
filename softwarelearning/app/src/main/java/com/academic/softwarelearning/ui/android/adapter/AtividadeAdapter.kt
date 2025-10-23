package com.academic.softwarelearning.ui.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.domain.model.Atividade
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AtividadeAdapter(
    private val onRemoveAtividade: (Atividade) -> Unit
) : ListAdapter<Atividade, AtividadeAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<Atividade>() {
        override fun areItemsTheSame(oldItem: Atividade, newItem: Atividade) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Atividade, newItem: Atividade) =
            oldItem == newItem
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val titulo: TextView = v.findViewById(R.id.tituloAtividade)
        val descricao: TextView = v.findViewById(R.id.descricaoAtividade)
        val chipInicio: TextView = v.findViewById(R.id.dataInicioAtividade)
        val chipFim: TextView = v.findViewById(R.id.dataFimAtividade)
        val btnRemover: MaterialButton = v.findViewById(R.id.btnRemoverAtividade)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_atividade, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val a = getItem(position)

        h.titulo.text = a.titulo
        h.descricao.text = a.descricao

        h.chipInicio.text = "Início: ${formatDate(a.dataInicio)}"
        h.chipFim.text    = "Fim: ${formatDate(a.dataFim)}"

        h.btnRemover.setOnClickListener { onRemoveAtividade(a) }
    }

    // ---------- Helpers ----------
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private fun formatDate(msOrSec: Long?): String {
        if (msOrSec == null || msOrSec <= 0L) return "--"
        // aceita segundos ou milissegundos
        val millis = if (msOrSec < 1_000_000_000_000L) msOrSec * 1000 else msOrSec
        return try { sdf.format(Date(millis)) } catch (_: Exception) { "--" }
    }
}
