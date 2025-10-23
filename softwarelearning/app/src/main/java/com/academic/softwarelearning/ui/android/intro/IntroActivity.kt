package com.academic.softwarelearning.ui.intro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.academic.softwarelearning.R
import com.academic.softwarelearning.databinding.ActivityIntroBinding
import com.academic.softwarelearning.domain.model.UserClaims
import com.academic.softwarelearning.domain.service.AuthSessionService
import com.academic.softwarelearning.ui.login.LoginActivity
import com.academic.softwarelearning.ui.main.MainActivity
import com.academic.softwarelearning.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import android.Manifest
import android.content.pm.PackageManager

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    // 🔹 Launcher para login com Google
    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            showLoading(true)

            auth.signInWithCredential(credential).addOnCompleteListener { t ->
                if (t.isSuccessful) {
                    ensureDefaultRole()
                    loadRoleFromRTDBAndGo()
                } else {
                    showLoading(false)
                    snack("Falha no Google: ${t.exception?.message}")
                }
            }
        } catch (e: Exception) {
            showLoading(false)
            snack("Erro no Google Sign-In: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Se o usuário já estiver logado, pula direto
        auth.currentUser?.let {
            ensureDefaultRole()
            loadRoleFromRTDBAndGo()
            return
        }

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicita permissões (importante no Android 13+)
        requestNecessaryPermissions()

        setupGoogle()

        binding.regiterBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.googleBtn.setOnClickListener {
            googleLauncher.launch(googleClient.signInIntent)
        }
    }

    /** 🔹 Configura o login via Google */
    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // do google-services.json
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)
    }

    /** 🔹 Pede permissões de mídia e notificações (Android 13+) */
    private fun requestNecessaryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.POST_NOTIFICATIONS
            )
            val notGranted = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (notGranted.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val notGranted = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (notGranted.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
            }
        }
    }

    /** 🔹 Busca o papel (role) no Realtime Database e navega */
    private fun loadRoleFromRTDBAndGo() {
        val u = FirebaseAuth.getInstance().currentUser ?: run {
            goToMainAndFinish(); return
        }
        val ref = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(u.uid)

        ref.get().addOnSuccessListener { snap ->
            val role = snap.child("role").getValue(String::class.java) ?: "ALUNO"
            AuthSessionService.saveClaims(this, UserClaims(role))
            goToMainAndFinish()
        }.addOnFailureListener {
            AuthSessionService.saveClaims(this, UserClaims("ALUNO"))
            goToMainAndFinish()
        }
    }

    /** 🔹 Cria o perfil se não existir */
    private fun ensureDefaultRole() {
        val u = FirebaseAuth.getInstance().currentUser ?: return
        val ref = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(u.uid)

        ref.child("role").get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                val perfil = mapOf(
                    "id" to u.uid,
                    "nome" to (u.displayName ?: ""),
                    "email" to (u.email ?: ""),
                    "role" to "ALUNO",
                    "createdAt" to System.currentTimeMillis()
                )
                ref.updateChildren(perfil)
            }
        }
    }

    /** 🔹 Navega para MainActivity */
    private fun goToMainAndFinish() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /** 🔹 Mostra / oculta o loading */
    private fun showLoading(show: Boolean) {
        if (!this::binding.isInitialized) return
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.googleBtn.isEnabled = !show
        binding.loginBtn.isEnabled = !show
        binding.regiterBtn.isEnabled = !show
    }

    /** 🔹 Toast rápido */
    private fun snack(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}