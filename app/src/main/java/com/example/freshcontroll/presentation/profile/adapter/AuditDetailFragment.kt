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

                        // Mostrar estados Antes/Después
                        binding.cvBeforeState.isVisible = !it.beforeState.isNullOrBlank()
                        binding.cvTransitionArrow.isVisible = !it.beforeState.isNullOrBlank()
                        binding.cvAfterState.isVisible = !it.afterState.isNullOrBlank()

                        binding.tvBeforeValue.text = it.beforeState ?: ""
                        binding.tvBeforeValue.textSize = 18f // Reducimos tamaño para que quepa texto
                        
                        binding.tvAfterValue.text = it.afterState ?: ""
                        binding.tvAfterValue.textSize = 18f

                        // Ocultamos badge de ajuste si no es numérico
                        binding.cvAdjustmentBadge.isVisible = false

                        // Mostrar descripción narrativa
                        binding.tvAuditReason.text = it.description
                        
                        // Si es una venta anulada, podemos mostrar info extra si se desea
                        binding.cvItemDetails.isVisible = true
                        binding.tvProductName.text = it.title
                        binding.tvProductSkuCategory.text = "Operación de Auditoría"
                    }
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}