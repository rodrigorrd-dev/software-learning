package com.academic.softwarelearning.core.claims

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.academic.softwarelearning.ui.main.MainActivity
import com.academic.softwarelearning.domain.service.AuthSessionService
import com.academic.softwarelearning.domain.model.UserClaims
import com.google.firebase.auth.FirebaseAuth

/** Chama após login p/ atualizar claims e ir p/ a Home */
fun AppCompatActivity.refreshClaimsAndGo() {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    user.getIdToken(true).addOnSuccessListener { tk ->
        val role = tk.claims["role"] as? String
        AuthSessionService.saveClaims(this, UserClaims(role))
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }.addOnFailureListener {
        Toast.makeText(this, "Não foi possível carregar permissões: ${it.message}", Toast.LENGTH_SHORT).show()
    }
}

/** Guard simples: termina a Activity se o papel não for permitido */
fun AppCompatActivity.requireRole(vararg allowed: String, onDenied: (() -> Unit)? = null) {
    val ok = AuthSessionService.hasRole(this, *allowed)
    if (!ok) {
        onDenied?.invoke() ?: run {
            Toast.makeText(this, "Acesso negado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
