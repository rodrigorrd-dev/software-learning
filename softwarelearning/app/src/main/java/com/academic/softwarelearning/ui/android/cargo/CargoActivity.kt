package com.academic.softwarelearning.ui.role

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.academic.softwarelearning.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CargoActivity : AppCompatActivity() {

    private lateinit var tvNome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var spRole: Spinner
    private lateinit var btnSalvar: Button

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseDatabase.getInstance().reference }

    private val roles = listOf("ALUNO", "PROFESSOR", "ADMIN")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cargo)

        tvNome = findViewById(R.id.tvNome)
        tvEmail = findViewById(R.id.tvEmail)
        spRole = findViewById(R.id.spRole)
        btnSalvar = findViewById(R.id.btnSalvar)

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Faça login primeiro", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        // popula infos básicas
        tvNome.text = user.displayName ?: "(sem nome)"
        tvEmail.text = user.email ?: ""

        // spinner
        spRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        // carrega role atual ou define ALUNO
        val userRef = db.child("usuarios").child(user.uid)
        userRef.child("role").get().addOnSuccessListener { snap ->
            val atual = snap.getValue(String::class.java) ?: "ALUNO"
            val idx = roles.indexOf(atual).takeIf { it >= 0 } ?: 0
            spRole.setSelection(idx)

            // se não existia, cria padrão ALUNO
            if (!snap.exists()) {
                userRef.updateChildren(mapOf(
                    "id" to user.uid,
                    "nome" to (user.displayName ?: ""),
                    "email" to (user.email ?: ""),
                    "role" to "ALUNO",
                    "createdAt" to System.currentTimeMillis()
                ))
            }
        }

        btnSalvar.setOnClickListener {
            val novoRole = roles[spRole.selectedItemPosition]
            userRef.child("role").setValue(novoRole)
                .addOnSuccessListener {
                    Toast.makeText(this, "Papel atualizado: $novoRole", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
