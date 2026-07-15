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
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.databinding.FragmentAuditDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AuditDetailFragment : Fragment() {
    private var _binding: FragmentAuditDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuditDetailViewModel by viewModels()
    private val args: AuditDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAuditDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Cambio 1: Método correcto
        viewModel.loadLogDetails(args.logId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logDetail.collect { log ->
                    log?.let {
                        // Info del actor
                        binding.tvActorName.text = it.userName
                        binding.tvActorInitials.text = it.userName.take(2).uppercase()
                        binding.tvAuditActionType.text = it.title // Cambio 2: usar title

                        // Cambio 3: Formateo de timestamp
                        val timeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        binding.tvTimestamp.text = timeFormat.format(Date(it.timestamp))

                        // Estrategia de granularidad: Ocultar vistas sin datos en el modelo
                        binding.cvBeforeState.isVisible = false
                        binding.cvTransitionArrow.isVisible = false
                        binding.cvAfterState.isVisible = false
                        binding.tvProductName.isVisible = false
                        binding.tvProductSkuCategory.isVisible = false

                        // Mostrar descripción narrativa
                        binding.tvAuditReason.text = it.description
                    }
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}