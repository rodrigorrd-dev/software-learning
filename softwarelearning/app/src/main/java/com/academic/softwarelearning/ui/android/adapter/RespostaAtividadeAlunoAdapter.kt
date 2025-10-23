package com.academic.softwarelearning.ui.android.adapter

import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.domain.model.RespostaAtividadeAluno
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class RespostaAtividadeAlunoAdapter(
    private val respostas: List<RespostaAtividadeAluno>
) : RecyclerView.Adapter<RespostaAtividadeAlunoAdapter.RespostaViewHolder>() {

    // ---------- Helpers ----------
    private fun formatDate(msOrSec: Long?): String {
        if (msOrSec == null) return "-"
        val millis = if (msOrSec < 1_000_000_000_000L) msOrSec * 1000 else msOrSec
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun normalizeStatus(raw: String?): String {
        val u = (raw ?: "").trim().uppercase()
        return when {
            u in setOf("ATRIBUIDA","ATRIBUÍDA","ATIVA","EM_ANDAMENTO","ABERTA","OPEN") -> "Atribuída"
            u in setOf("ENCERRADA","FINALIZADA","CONCLUÍDA","CLOSED") -> "Encerrada"
            u in setOf("PENDENTE","PENDING","AGUARDANDO") -> "Pendente"
            else -> raw ?: "-"
        }
    }

    inner class RespostaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tituloAtividade: TextView = itemView.findViewById(R.id.txtTitulo)
        val descricaoAtividade: TextView = itemView.findViewById(R.id.txtDescricao)
        val datasAtividade: TextView = itemView.findViewById(R.id.txtDatas)
        val status: TextView = itemView.findViewById(R.id.txtStatus)
        val btnToggleRespostas: TextView = itemView.findViewById(R.id.btnToggleRespostas)
        val recyclerRespostas: RecyclerView = itemView.findViewById(R.id.recyclerRespostas)

        // Ícone para abrir modal da IA
        val btnShowDeepseek: ImageView = itemView.findViewById(R.id.btnShowDeepseek)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RespostaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resposta, parent, false)
        return RespostaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RespostaViewHolder, position: Int) {
        val resposta = respostas[position]
        val database = FirebaseDatabase.getInstance().reference

        // Carrega dados da atividade
        database.child("atividades").child(resposta.atividadeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val titulo = snapshot.child("titulo").getValue(String::class.java) ?: "Sem título"
                    val descricao = snapshot.child("descricao").getValue(String::class.java) ?: ""
                    val inicio = snapshot.child("dataInicio").getValue(Long::class.java)
                    val fim = snapshot.child("dataFim").getValue(Long::class.java)
                    val statusRaw = snapshot.child("status").getValue(String::class.java)

                    holder.tituloAtividade.text = titulo
                    holder.descricaoAtividade.text = descricao
                    holder.datasAtividade.text = "Início: ${formatDate(inicio)} - Fim: ${formatDate(fim)}"
                    holder.status.text = "Status: ${normalizeStatus(statusRaw)}"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Erro ao ler atividade", error.toException())
                }
            })

        // Botão para mostrar/ocultar respostas
        holder.btnToggleRespostas.setOnClickListener {
            if (holder.recyclerRespostas.visibility == View.GONE) {
                val usuarioId = resposta.usuarioId
                database.child("usuarios").child(usuarioId).child("nome")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val nome = snapshot.getValue(String::class.java) ?: "Desconhecido"
                            val listaSimples = listOf(Pair(nome, resposta.texto))

                            holder.recyclerRespostas.apply {
                                layoutManager = LinearLayoutManager(holder.itemView.context)
                                adapter = RespostaSimplesAdapter(listaSimples)
                                visibility = View.VISIBLE
                            }
                            holder.btnToggleRespostas.text = "Ocultar respostas"
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Erro ao buscar nome do usuário", error.toException())
                        }
                    })
            } else {
                holder.recyclerRespostas.visibility = View.GONE
                holder.btnToggleRespostas.text = "Ver respostas"
            }
        }

        // Botão para mostrar modal da IA
        holder.btnShowDeepseek.setOnClickListener {
            val deepseekText = resposta.integracaoIA?.trim()?.replace("\\n", "\n") ?: "Sem resposta da IA"
            val cleanText = deepseekText
                .removePrefix("\"")
                .removeSuffix("\"")
                .replace("\\\"", "\"")

            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Resposta da IA")
                .setMessage(Html.fromHtml(cleanText, Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton("Fechar", null)
                .show()
        }

    }

    override fun getItemCount() = respostas.size
}
