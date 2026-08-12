package com.example.freshcontroll.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentHomeBinding
import com.example.freshcontroll.domain.model.UserRole
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        // CORRECCIÓN: btnRevisarAhora en lugar de btnNotifications
        binding.btnRevisarAhora.setOnClickListener {
            val userRole = viewModel.uiState.value.userRole
            if (userRole == UserRole.OWNER) {
                val action = HomeFragmentDirections.actionHomeToNotifications()
                findNavController().navigate(action)
            } else {
                showAdminOnlyMessage()
            }
        }

        // CORRECCIÓN: btnNuevaVenta en lugar de btnNewSale
        binding.btnNuevaVenta.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToNewSale()
            findNavController().navigate(action)
        }

        // CORRECCIÓN: cvRegistrarProducto en lugar de btnRegisterProduct
        binding.cvRegistrarProducto.setOnClickListener {
            val userRole = viewModel.uiState.value.userRole
            if (userRole == UserRole.OWNER) {
                val action = HomeFragmentDirections.actionHomeToRegisterProduct()
                findNavController().navigate(action)
            } else {
                showAdminOnlyMessage()
            }
        }

        // CORRECCIÓN: cvInventario en lugar de btnInventory
        binding.cvInventario.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeToInventory()
            findNavController().navigate(action)
        }

        binding.cvAddEmployee.setOnClickListener {
            val userRole = viewModel.uiState.value.userRole
            if (userRole == UserRole.OWNER) {
                findNavController().navigate(R.id.addEmployeeFragment)
            } else {
                showAdminOnlyMessage()
            }
        }

        binding.cvMisVentas.setOnClickListener {
            findNavController().navigate(R.id.employeeSalesHistoryFragment)
        }
    }

    private fun showAdminOnlyMessage() {
        Snackbar.make(
            binding.root,
            "Acceso solo para Administradores",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // CORRECCIÓN: tvVentasHoyMonto y tvTransaccionesNumero
                    binding.tvVentasHoyMonto.text = String.format("S/ %.2f", state.ventasDelDia)
                    binding.tvTransaccionesNumero.text = state.transaccionesHoy.toString()
                    binding.tvHeaderTitle.text = if (state.nombreNegocio.isBlank()) "FreshControl" else state.nombreNegocio

                    // Manejo del panel/badge de alertas
                    if (state.hayAlertas) {
                        // CORRECCIÓN: cvAlertLowStock y btnRevisarAhora
                        binding.cvAlertLowStock.visibility = View.VISIBLE
                        binding.btnRevisarAhora.visibility = View.VISIBLE

                        // CORRECCIÓN: Reutilizamos tvAlertDescription ya que tvCantidadAlertas no existe
                        binding.tvAlertDescription.text = "Tienes ${state.cantidadAlertas} alertas de inventario que requieren atención."
                    } else {
                        binding.cvAlertLowStock.visibility = View.GONE
                        binding.btnRevisarAhora.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}