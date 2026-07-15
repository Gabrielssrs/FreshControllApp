package com.example.freshcontroll.presentation.profile

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
import com.example.freshcontroll.domain.repository.AuthRepository
import com.example.freshcontroll.databinding.FragmentEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()
    @Inject lateinit var authRepository: AuthRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val user = authRepository.getCurrentUser()
            user?.let {
                // TODO: etBusinessName debería mostrar el nombre de la tienda (Store), no del usuario.
                // Pendiente: crear GetStoreUseCase para obtener el nombre real del negocio.
                binding.etBusinessName.setText(it.fullName)
                binding.etEmail.setText(it.email)
                binding.etPhone.setText(it.phone)
            }
        }

        binding.btnSaveChanges.setOnClickListener {
            viewModel.saveProfileChanges(
                fullName = binding.etBusinessName.text.toString(),
                email = binding.etEmail.text.toString(),
                phone = binding.etPhone.text.toString(),
                photoUrl = null
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is EditProfileUiState.Success) findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}