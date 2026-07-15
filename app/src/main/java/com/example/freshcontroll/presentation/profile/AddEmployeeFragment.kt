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

        // Mapeo: Cajero -> EMPLOYEE, Administrador -> OWNER
        binding.btnRoleCajero.setOnClickListener { selectedRole = UserRole.EMPLOYEE }
        binding.btnRoleAdministrador.setOnClickListener { selectedRole = UserRole.OWNER }

        // Incluye email y phone como se requiere
        binding.btnSubmitEmployee.setOnClickListener {
            viewModel.createEmployeeAccount(
                name = binding.etFullName.text.toString(),
                email = binding.etEmail.text.toString(),
                phone = binding.etPhone.text.toString(),
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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generatedPassword.collect { pass ->
                    pass?.let {
                        currentPassword = it
                        binding.cvTempPassword.isVisible = true
                        binding.tvGeneratedPassword.text = it
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