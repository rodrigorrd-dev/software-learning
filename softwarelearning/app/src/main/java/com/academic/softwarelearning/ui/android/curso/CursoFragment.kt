package com.academic.softwarelearning.ui.curso

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.ui.android.adapter.CursoAdapter
import com.academic.softwarelearning.domain.model.Curso
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class CursoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CursoAdapter
    private val cursoList: MutableList<Curso> = mutableListOf()
    private lateinit var fabAdd: FloatingActionButton
    private val dbRef = FirebaseDatabase.getInstance().getReference("cursos")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_curso, container, false)

        recyclerView = view.findViewById(R.id.recyclerCursos)
        fabAdd = view.findViewById(R.id.fabAddCurso)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CursoAdapter(
            cursos = cursoList,
            onEdit = { curso -> editarCurso(curso) },
            onDelete = { curso -> deletarCurso(curso) }
        )
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditCursoActivity::class.java))
        }

        carregarCursos()
        return view
    }

    private fun carregarCursos() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cursoList.clear()
                for (item in snapshot.children) {
                    val curso = item.getValue(Curso::class.java)
                    if (curso != null) {
                        if (curso.id == null || curso.id!!.isBlank()) {
                            try {
                                val field = curso.javaClass.getDeclaredField("id")
                                field.isAccessible = true
                                field.set(curso, item.key)
                            } catch (_: Throwable) { }
                        }
                        cursoList.add(curso)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) { /* noop */ }
        })
    }

    private fun editarCurso(curso: Curso) {
        val intent = Intent(requireContext(), AddEditCursoActivity::class.java)
        intent.putExtra("cursoId", curso.id)
        intent.putExtra("cursoNome", curso.nome)
        startActivity(intent)
    }

    private fun deletarCurso(curso: Curso) {
        val id = curso.id
        if (!id.isNullOrBlank()) {
            dbRef.child(id).removeValue()
        }
    }
}
