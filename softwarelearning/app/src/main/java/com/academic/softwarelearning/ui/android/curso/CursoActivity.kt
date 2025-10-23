package com.academic.softwarelearning.ui.curso

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.academic.softwarelearning.R
import com.academic.softwarelearning.ui.android.adapter.CursoAdapter
import com.academic.softwarelearning.domain.model.Curso
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class CursoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CursoAdapter
    private val cursoList: MutableList<Curso> = mutableListOf()
    private val dbRef = FirebaseDatabase.getInstance().getReference("cursos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_curso)

        recyclerView = findViewById(R.id.recyclerCursos)
        val fabAdd: FloatingActionButton = findViewById(R.id.fabAddCurso)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CursoAdapter(
            cursos = cursoList,
            onEdit = { curso -> editarCurso(curso) },
            onDelete = { curso -> deletarCurso(curso) }
        )
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditCursoActivity::class.java))
        }

        carregarCursos()
    }

    private fun carregarCursos() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cursoList.clear()
                for (item in snapshot.children) {
                    val curso = item.getValue(Curso::class.java)
                    if (curso != null) {
                        // se o nó não tiver "id" gravado, usa a key do Firebase
                        if (curso.id == null || curso.id!!.isBlank()) {
                            try {
                                // se for data class com var id, seta; senão, crie uma cópia com id
                                val field = curso.javaClass.getDeclaredField("id")
                                field.isAccessible = true
                                field.set(curso, item.key)
                            } catch (_: Throwable) {
                                // fallback: ignora, ainda assim funciona para editar/deletar via key se necessário
                            }
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
        val intent = Intent(this, AddEditCursoActivity::class.java)
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
