package com.academic.softwarelearning.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.academic.softwarelearning.ui.main.MainActivity
import com.academic.softwarelearning.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVoltarRegister.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()

        binding.regiterBtn2.setOnClickListener {
            val nome = binding.etNome.text.toString()
            val email = binding.etEmail.text.toString()
            val senha = binding.etSenha.text.toString()
            val senhaRepetida = binding.etSenhaRepetida.text.toString()

            if (email.isEmpty() || senha.isEmpty() || nome.isEmpty() || senhaRepetida.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (senha != senhaRepetida) {
                Toast.makeText(this, "A senha repetida está incorreta", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nome)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {

                            val database = FirebaseDatabase.getInstance().reference
                            val userId = user.uid

                            // 👇 cria/atualiza o perfil com role padrão ALUNO
                            val userMap = mapOf(
                                "id" to userId,
                                "nome" to nome,
                                "email" to (user.email ?: ""),
                                "role" to "ALUNO",
                                "createdAt" to System.currentTimeMillis()
                            )
                            database.child("usuarios").child(userId).updateChildren(userMap)

                            Toast.makeText(
                                this@RegisterActivity,
                                "Cadastro realizado com sucesso! Bem-vindo, ${user.displayName}",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Erro ao salvar o nome: ${updateTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Falha no cadastro: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
