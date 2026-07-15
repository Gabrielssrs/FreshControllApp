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
import com.example.freshcontroll.databinding.FragmentAuditBinding
import com.example.freshcontroll.presentation.profile.adapter.AuditLogAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuditFragment : Fragment() {
    private var _binding: FragmentAuditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuditViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAuditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del botón de retroceso
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Configuración del botón de notificaciones
        binding.btnNotifications.setOnClickListener {
            // TODO: navegar a notificationsFragment si se requiere acceso directo desde Auditoría.
        }

        val adapter = AuditLogAdapter { id ->
            findNavController().navigate(AuditFragmentDirections.actionAuditToAuditDetail(id))
        }

        binding.rvAuditLogs.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.auditLogs.collect { adapter.submitList(it) }
            }
        }
    }


    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}