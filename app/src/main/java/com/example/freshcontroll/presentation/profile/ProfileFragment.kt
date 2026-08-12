package com.example.freshcontroll.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
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

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.updateProfilePhoto(uri)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        user?.let {
                            binding.tvBusinessName.text = it.fullName
                            binding.tvUserRole.text = if (it.role == UserRole.OWNER) "Dueño / Administrador" else "Empleado / Cajero"

                            // Carga de imagen de perfil
                            if (!it.photoUrl.isNullOrEmpty()) {
                                binding.sivLargeProfilePicture.load(it.photoUrl) {
                                    crossfade(true)
                                    placeholder(R.drawable.logo_freshcontrol)
                                    error(R.drawable.logo_freshcontrol)
                                }
                                binding.sivHeaderAvatar.load(it.photoUrl) {
                                    crossfade(true)
                                    placeholder(R.drawable.logo_freshcontrol)
                                }
                            } else {
                                binding.sivLargeProfilePicture.setImageResource(R.drawable.logo_freshcontrol)
                                binding.sivHeaderAvatar.setImageResource(R.drawable.logo_freshcontrol)
                            }

                            val isOwner = it.role == UserRole.OWNER
                            binding.cvEmployeeManagement.isVisible = isOwner
                            binding.cvAudit.isVisible = isOwner
                            binding.cvCashRegisterClose.isVisible = isOwner
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        // Por ahora solo mostramos el avatar genérico o real según el estado
                        binding.sivLargeProfilePicture.alpha = if (isLoading) 0.5f else 1.0f
                    }
                }
            }
        }

        // Listeners
        binding.btnEditPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
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
            findNavController().navigate(
                R.id.auth_nav_graph,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.root_nav_graph, true)
                    .build()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}