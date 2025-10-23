package com.academic.softwarelearning.ui.curso

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.academic.softwarelearning.R
import com.academic.softwarelearning.domain.model.Curso
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class AddEditCursoActivity : AppCompatActivity() {
    private lateinit var editNome: EditText
    private lateinit var btnSalvar: Button
    private val dbRef = FirebaseDatabase.getInstance().getReference("cursos")
    private var cursoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_curso)

        // Configurar a Toolbar como AppBar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (intent.getStringExtra("cursoId") != null) "Editar Curso" else "Novo Curso"

        editNome = findViewById(R.id.editNomeCurso)
        btnSalvar = findViewById(R.id.btnSalvarCurso)

        cursoId = intent.getStringExtra("cursoId")
        val nome = intent.getStringExtra("cursoNome")
        if (cursoId != null) editNome.setText(nome)

        btnSalvar.setOnClickListener {
            val nomeCurso = editNome.text.toString()
            if (cursoId == null) {
                val id = dbRef.push().key!!
                val novoCurso = Curso(id, nomeCurso)
                dbRef.child(id).setValue(novoCurso)
            } else {
                dbRef.child(cursoId!!).setValue(Curso(cursoId, nomeCurso))
            }
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}