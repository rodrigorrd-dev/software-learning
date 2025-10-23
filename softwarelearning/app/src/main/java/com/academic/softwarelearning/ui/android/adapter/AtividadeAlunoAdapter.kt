package com.academic.softwarelearning.ui.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.domain.model.AtividadeAluno
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AtividadeAlunoAdapter(
    private val atividades: List<AtividadeAluno>,
    private val onAtividadeClick: (AtividadeAluno) -> Unit,
    private val onDownloadClick: (String /* responsavel */, String /* codigo */) -> Unit
) : RecyclerView.Adapter<AtividadeAlunoAdapter.AtividadeViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtividadeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_atividade_aluno, parent, false)
        return AtividadeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AtividadeViewHolder, position: Int) {
        holder.bind(atividades[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = atividades.size

    inner class AtividadeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardAtividade)
        private val tituloAtividade: TextView = itemView.findViewById(R.id.tituloAtividade)
        private val descricaoAtividade: TextView = itemView.findViewById(R.id.descricaoAtividade)
        private val dataInicioText: TextView = itemView.findViewById(R.id.dataInicioAtividade)
        private val dataFimText: TextView = itemView.findViewById(R.id.dataFimAtividade)
        private val iconeDownload: ImageView = itemView.findViewById(R.id.iconeDownload)

        fun bind(atividade: AtividadeAluno, isSelected: Boolean) {
            tituloAtividade.text = atividade.titulo
            descricaoAtividade.text = atividade.descricao
            dataInicioText.text = "Início: ${formatarData(atividade.dataInicio)}"
            dataFimText.text = "Fim: ${formatarData(atividade.dataFim)}"

            // Atualiza o estado selecionado para o selector
            cardView.isSelected = isSelected

            iconeDownload.visibility = View.VISIBLE
            iconeDownload.setOnClickListener {
                onDownloadClick(atividade.responsavel.toString(), atividade.id.toString())
            }

            // Clique no card para selecionar a atividade
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onAtividadeClick(atividade)
            }
        }

        private fun formatarData(timestamp: Long?): String {
            return if (timestamp != null && timestamp > 0) {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                sdf.format(Date(timestamp))
            } else {
                "Não definida"
            }
        }
    }

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        notifyItemChanged(previousPosition)
    }

}
