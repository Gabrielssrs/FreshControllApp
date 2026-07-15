package com.example.freshcontroll.presentation.inventory

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentAdjustStockBinding
import com.example.freshcontroll.domain.model.MovementReason
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdjustStockFragment : Fragment() {

    private var _binding: FragmentAdjustStockBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdjustStockViewModel by viewModels()
    private val args: AdjustStockFragmentArgs by navArgs()

    // Variable de estado para el motivo de ajuste seleccionado
    private var selectedReason: MovementReason = MovementReason.COMPRA

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdjustStockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadProduct(args.productId)
        setupListeners()
        observeUiState()

        // Estado inicial de las tarjetas
        selectReasonCard(binding.cvReasonPurchase)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // --- Listeners para las tarjetas de motivos de ajuste ---
        val onPurchaseClick = View.OnClickListener {
            selectedReason = MovementReason.COMPRA
            selectReasonCard(binding.cvReasonPurchase)
        }
        binding.cvReasonPurchase.setOnClickListener(onPurchaseClick)
        binding.rbReasonPurchase.setOnClickListener(onPurchaseClick)

        val onWasteClick = View.OnClickListener {
            selectedReason = MovementReason.MERMA
            selectReasonCard(binding.cvReasonWaste)
        }
        binding.cvReasonWaste.setOnClickListener(onWasteClick)
        binding.rbReasonWaste.setOnClickListener(onWasteClick)

        val onCorrectionClick = View.OnClickListener {
            selectedReason = MovementReason.CORRECCION
            selectReasonCard(binding.cvReasonCorrection)
        }
        binding.cvReasonCorrection.setOnClickListener(onCorrectionClick)
        binding.rbReasonCorrection.setOnClickListener(onCorrectionClick)

        val onReturnClick = View.OnClickListener {
            selectedReason = MovementReason.DEVOLUCION
            selectReasonCard(binding.cvReasonReturn)
        }
        binding.cvReasonReturn.setOnClickListener(onReturnClick)
        binding.rbReasonReturn.setOnClickListener(onReturnClick)


        // --- Listener Confirmación ---
        binding.btnConfirmAdjustment.setOnClickListener {
            val newQuantity = binding.etNewQuantity.text.toString().toDoubleOrNull()

            if (newQuantity == null) {
                Snackbar.make(binding.root, "Ingrese una cantidad válida", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Usamos la variable directa gestionada por los clicks de las tarjetas
            viewModel.onConfirmStockAdjustment(newQuantity, selectedReason)
        }
    }

    private fun selectReasonCard(selectedCard: MaterialCardView) {
        val context = requireContext()
        val verdePrimario = ContextCompat.getColor(context, R.color.verde_primario)
        val bordeTarjeta = ContextCompat.getColor(context, R.color.borde_tarjeta)
        val textoSecundario = ContextCompat.getColor(context, R.color.texto_secundario)
        val grisFondoIcono = ContextCompat.getColor(context, R.color.gris_fondo_icono)
        val blanco = ContextCompat.getColor(context, R.color.blanco)

        val colorStateSelected = ColorStateList.valueOf(verdePrimario)
        val colorStateNormal = ColorStateList.valueOf(textoSecundario)

        val density = resources.displayMetrics.density
        val strokeSelected = (2 * density).toInt()
        val strokeNormal = (1 * density).toInt()

        val cards = listOf(
            binding.cvReasonPurchase to binding.rbReasonPurchase,
            binding.cvReasonWaste to binding.rbReasonWaste,
            binding.cvReasonCorrection to binding.rbReasonCorrection,
            binding.cvReasonReturn to binding.rbReasonReturn
        )

        for ((card, radio) in cards) {
            if (card == selectedCard) {
                card.strokeColor = verdePrimario
                card.strokeWidth = strokeSelected
                card.setCardBackgroundColor(grisFondoIcono)
                radio.isChecked = true
                radio.buttonTintList = colorStateSelected
            } else {
                card.strokeColor = bordeTarjeta
                card.strokeWidth = strokeNormal
                card.setCardBackgroundColor(blanco)
                radio.isChecked = false
                radio.buttonTintList = colorStateNormal
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentProduct.collect { product ->
                        product?.let {
                            binding.tvProductName.text = it.name
                            binding.tvCurrentStock.text = "Stock actual: ${it.currentStock} ${it.unitType}"
                        }
                    }
                }

                launch {
                    viewModel.adjustmentSuccess.collect { isSuccess ->
                        when (isSuccess) {
                            true -> findNavController().navigateUp()
                            false -> Snackbar.make(binding.root, "Error al ajustar el stock", Snackbar.LENGTH_LONG).show()
                            null -> { /* Idle */ }
                        }
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