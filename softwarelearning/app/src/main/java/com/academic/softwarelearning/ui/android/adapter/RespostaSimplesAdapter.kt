package com.academic.softwarelearning.ui.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R

class RespostaSimplesAdapter(
    private val lista: List<Pair<String, String>>
) : RecyclerView.Adapter<RespostaSimplesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeUsuario: TextView = view.findViewById(R.id.nomeUsuario)
        val textoResposta: TextView = view.findViewById(R.id.textoResposta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resposta_simples, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (nome, texto) = lista[position]
        holder.nomeUsuario.text = nome
        holder.textoResposta.text = texto
    }

    override fun getItemCount() = lista.size
}
