package com.example.freshcontroll

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.freshcontroll.databinding.ActivityMainBinding
import com.example.freshcontroll.domain.model.UserRole
import com.example.freshcontroll.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Vincular BottomNavigationView con el NavController
        binding.bottomNav.setupWithNavController(navController)

        // Interceptar clics en el menú para navegación condicional por rol
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.salesHistoryFragment -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        val user = authRepository.getCurrentUser()
                        if (user?.role == UserRole.OWNER) {
                            navController.navigate(R.id.salesHistoryFragment)
                        } else {
                            // Mostrar notificación de acceso restringido para empleados
                            com.google.android.material.snackbar.Snackbar.make(
                                binding.root,
                                "Acceso solo para Administradores",
                                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                    // Retornamos false para que no se marque el ítem si no tiene acceso
                    false 
                }
                else -> {
                    // Para los demás ítems, usamos la navegación estándar
                    androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
                    true
                }
            }
        }

        // Controlar visibilidad del BottomNav según el destino
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment,
                R.id.inventoryFragment,
                R.id.salesHistoryFragment,
                R.id.employeeSalesHistoryFragment,
                R.id.profileFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNav.visibility = View.GONE
                }
            }
        }
    }
}
