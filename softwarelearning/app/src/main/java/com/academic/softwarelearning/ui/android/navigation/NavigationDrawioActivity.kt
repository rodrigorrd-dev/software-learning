package com.academic.softwarelearning.ui.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.academic.softwarelearning.R
import com.academic.softwarelearning.databinding.ActivityNavigationBinding

class NavigationDrawioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater) // vincule ao layout acima
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_curso,
                R.id.navigation_ai,
                R.id.navigation_atividade,
                R.id.navigation_chat_reposta_atividade_aluno
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // exemplo de badge no chat
        binding.navView.getOrCreateBadge(R.id.navigation_chat_reposta_atividade_aluno).apply {
            isVisible = true
            number = 3
        }
    }
}
