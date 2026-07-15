package com.example.freshcontroll.presentation.auth

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
import com.example.freshcontroll.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        // CORRECCIÓN: btnSubmitRegister en lugar de btnCrearTienda
        binding.btnSubmitRegister.setOnClickListener {
            // CORRECCIÓN: etBusinessName en lugar de etStoreName
            val storeName = binding.etBusinessName.text.toString().trim()
            val ownerName = binding.etOwnerName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            // CORRECCIÓN: Como et_address no existe en el XML, quemamos un valor temporal
            // (Si agregas el campo al XML luego, cambia esto por binding.etAddress.text.toString().trim())
            val address = "No especificada"

            val pass = binding.etPassword.text.toString().trim()
            val confirmPass = binding.etConfirmPassword.text.toString().trim()

            viewModel.onCreateStoreAndOwner(
                storeName = storeName,
                ownerName = ownerName,
                email = email,
                phone = phone,
                address = address,
                pass = pass,
                confirmPass = confirmPass
            )
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RegisterUiState.Idle -> {
                            // CORRECCIÓN: btnSubmitRegister
                            binding.btnSubmitRegister.isEnabled = true
                        }
                        is RegisterUiState.Loading -> {
                            // CORRECCIÓN: btnSubmitRegister
                            binding.btnSubmitRegister.isEnabled = false
                        }
                        is RegisterUiState.Success -> {
                            // CORRECCIÓN: btnSubmitRegister
                            binding.btnSubmitRegister.isEnabled = true
                            val action = RegisterFragmentDirections.actionRegisterToSuccess()
                            findNavController().navigate(action)
                        }
                        is RegisterUiState.Error -> {
                            // CORRECCIÓN: btnSubmitRegister
                            binding.btnSubmitRegister.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}