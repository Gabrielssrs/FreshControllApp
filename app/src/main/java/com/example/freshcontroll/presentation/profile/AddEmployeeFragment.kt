package com.example.freshcontroll.presentation.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.example.freshcontroll.databinding.FragmentAddEmployeeBinding
import com.example.freshcontroll.domain.model.UserRole
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEmployeeFragment : Fragment() {
    private var _binding: FragmentAddEmployeeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddEmployeeViewModel by viewModels()

    // Mapeo inicial a EMPLOYEE
    private var selectedRole: UserRole = UserRole.EMPLOYEE
    private var currentPassword: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Mapeo: Cajero -> EMPLOYEE, Administrador -> OWNER
        binding.btnRoleCajero.setOnClickListener { 
            selectedRole = UserRole.EMPLOYEE
            updateRoleUI()
        }
        binding.btnRoleAdministrador.setOnClickListener { 
            selectedRole = UserRole.OWNER
            updateRoleUI()
        }

        // Incluye email y phone como se requiere
        binding.btnSubmitEmployee.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                // Podrías mostrar un Snackbar aquí si quieres validación básica
                return@setOnClickListener
            }

            viewModel.createEmployeeAccount(
                name = name,
                email = email,
                phone = phone,
                role = selectedRole
            )
        }

        // Lógica de copia movida fuera del collect
        binding.btnCopyShare.setOnClickListener {
            currentPassword?.let { pass ->
                val clip = (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                clip.setPrimaryClip(ClipData.newPlainText("Password", pass))
            }
        }
    }

    private fun updateRoleUI() {
        if (selectedRole == UserRole.EMPLOYEE) {
            binding.btnRoleCajero.setBackgroundColor(requireContext().getColor(com.example.freshcontroll.R.color.verde_primario))
            binding.btnRoleCajero.setTextColor(requireContext().getColor(com.example.freshcontroll.R.color.blanco))
            
            binding.btnRoleAdministrador.setBackgroundColor(requireContext().getColor(android.R.color.transparent))
            binding.btnRoleAdministrador.setTextColor(requireContext().getColor(com.example.freshcontroll.R.color.texto_principal))
        } else {
            binding.btnRoleAdministrador.setBackgroundColor(requireContext().getColor(com.example.freshcontroll.R.color.verde_primario))
            binding.btnRoleAdministrador.setTextColor(requireContext().getColor(com.example.freshcontroll.R.color.blanco))

            binding.btnRoleCajero.setBackgroundColor(requireContext().getColor(android.R.color.transparent))
            binding.btnRoleCajero.setTextColor(requireContext().getColor(com.example.freshcontroll.R.color.texto_principal))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generatedPassword.collect { pass ->
                    pass?.let {
                        currentPassword = it
                        binding.cvTempPassword.isVisible = true
                        binding.tvGeneratedPassword.text = it
                        // Opcional: Ocultar botón de crear tras el éxito para evitar duplicados
                        binding.btnSubmitEmployee.isEnabled = false
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