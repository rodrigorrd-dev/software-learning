package com.academic.softwarelearning.ui.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.domain.model.Curso

class CursoAdapter(
    private val cursos: List<Curso>,
    private val onEdit: (Curso) -> Unit,
    private val onDelete: (Curso) -> Unit
) : RecyclerView.Adapter<CursoAdapter.CursoViewHolder>() {

    inner class CursoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeCurso: TextView = view.findViewById(R.id.textNomeCurso)
        // Usa View para ser compatível com MaterialButton OU ImageButton
        val btnEditar: View = view.findViewById(R.id.btnEditar)
        val btnDeletar: View = view.findViewById(R.id.btnDeletar)
        // opcional: área clicável do item, se existir no layout
        val layoutCurso: View? = view.findViewById(R.id.layoutCurso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso, parent, false)
        return CursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val curso = cursos[position]
        holder.nomeCurso.text = curso.nome ?: ""

        holder.btnEditar.setOnClickListener { onEdit(curso) }
        holder.btnDeletar.setOnClickListener { onDelete(curso) }

        holder.layoutCurso?.setOnClickListener { onEdit(curso) } // se quiser abrir detalhes ao tocar no item
    }

    override fun getItemCount() = cursos.size
}
