package com.example.freshcontroll.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.databinding.FragmentRegisterSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterSuccessFragment : Fragment() {

    private var _binding: FragmentRegisterSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        // CORRECCIÓN: btnGetStarted en lugar de btnComenzar
        binding.btnGetStarted.setOnClickListener {
            // CORRECCIÓN: El action correcto generado por tu auth_nav_graph
            val action = RegisterSuccessFragmentDirections.actionSuccessToMain()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}