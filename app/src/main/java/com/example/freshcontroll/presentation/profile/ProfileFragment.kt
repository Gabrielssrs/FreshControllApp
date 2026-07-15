package com.example.freshcontroll.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentProfileBinding
import com.example.freshcontroll.domain.model.UserRole
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userProfile.collect { user ->
                    user?.let {
                        // TODO: Debería mostrar el nombre de la tienda (Store), pendiente GetStoreUseCase
                        binding.tvBusinessName.text = it.fullName
                        binding.tvUserRole.text = it.role.name

                        val isOwner = it.role == UserRole.OWNER
                        // Control de visibilidad para las tarjetas de dueño
                        binding.cvEmployeeManagement.isVisible = isOwner
                        binding.cvAudit.isVisible = isOwner
                        binding.cvCashRegisterClose.isVisible = isOwner
                    }
                }
            }
        }

        // Listeners para tarjetas generales
        binding.btnEditPhoto.setOnClickListener { /* TODO: Implementar lógica de edición de foto */ }
        binding.cvOptionBusinessData.setOnClickListener { /* TODO: Navegar a datos de negocio */ }
        binding.cvOptionSecurity.setOnClickListener { /* TODO: Navegar a seguridad */ }
        binding.cvOptionHelp.setOnClickListener { /* TODO: Navegar a ayuda */ }

        // Navegación para dueño
        binding.cvEmployeeManagement.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_employeeManagement)
        }
        binding.cvAudit.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_audit)
        }
        binding.cvCashRegisterClose.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_cashRegisterClose)
        }

        // Listener de Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            viewModel.onLogout()
            requireActivity().recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}