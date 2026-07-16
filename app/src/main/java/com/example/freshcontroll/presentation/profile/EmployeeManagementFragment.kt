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
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentEmployeeManagementBinding
import com.example.freshcontroll.presentation.profile.adapter.EmployeeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmployeeManagementFragment : Fragment() {
    private var _binding: FragmentEmployeeManagementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmployeeManagementViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmployeeManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del adaptador
        val adapter = EmployeeAdapter { id, access -> viewModel.onAccessToggleChanged(id, access) }
        binding.rvEmployees.adapter = adapter

        // Observación de empleados
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.employeeList.collect { 
                    adapter.submitList(it.toList()) 
                }
            }
        }

        // Navegación: Agregar empleado
        binding.btnAddEmployee.setOnClickListener {
            findNavController().navigate(R.id.action_employeeManagement_to_addEmployee)
        }

        // Navegación: Botón atrás (Añadido)
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}