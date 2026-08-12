package com.example.freshcontroll.presentation.sales

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentSaleReceiptBinding
import com.example.freshcontroll.presentation.sales.adapter.SoldProductAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SaleReceiptFragment : Fragment() {

    private var _binding: FragmentSaleReceiptBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SaleReceiptViewModel by viewModels()
    private val args: SaleReceiptFragmentArgs by navArgs()
    private val soldAdapter = SoldProductAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSaleReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProducts.adapter = soldAdapter
        viewModel.fetchReceipt(args.saleId)

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        // Botón atrás: Volver al Inicio
        binding.btnBack.setOnClickListener {
            navigateToHome()
        }

        // Nueva Venta: Reiniciar flujo de venta
        binding.btnNewSale.setOnClickListener {
            findNavController().navigate(
                R.id.newSaleFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build()
            )
        }

        // Finalizar Venta: Confirmación simple
        binding.btnFinishSale.setOnClickListener {
            showConfirmationDialog(
                title = "Finalizar Venta",
                message = "¿Desea finalizar el proceso de esta venta?",
                onConfirm = { navigateToHome() }
            )
        }

        // Ver Recibo: Compartir como texto
        binding.btnViewReceipt.setOnClickListener {
            shareReceiptText()
        }

        // Anular Venta: Confirmación simple
        binding.btnVoidSale.setOnClickListener {
            showConfirmationDialog(
                title = "ANULAR VENTA",
                message = "Esta acción eliminará la venta por completo. ¿Está seguro?",
                onConfirm = { viewModel.voidSale(args.saleId) }
            )
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.receiptData.collect { data ->
                        data?.let { (sale, details) ->
                            binding.tvTotalAmount.text = String.format("S/ %.2f", sale.total)
                            binding.tvSubtotalAmount.text = String.format("S/ %.2f", sale.subtotal)
                            binding.tvTaxesAmount.text = String.format("S/ %.2f", sale.taxes)
                            binding.tvTicketNumber.text = "Ticket #${sale.ticketNumber}"

                            val itemCount = details.sumOf { it.quantity }.toInt()
                            binding.tvSoldItemsSection.text = "Artículos Vendidos ($itemCount)"

                            soldAdapter.submitList(details.toList())
                        }
                    }
                }

                launch {
                    viewModel.voidEvent.collectLatest { result ->
                        result.onSuccess {
                            Snackbar.make(binding.root, "Venta anulada con éxito", Snackbar.LENGTH_SHORT).show()
                            navigateToHome()
                        }.onFailure {
                            Snackbar.make(binding.root, "Error al anular venta", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun shareReceiptText() {
        val data = viewModel.receiptData.value ?: return
        val (sale, details) = data
        
        val shareBody = buildString {
            appendLine("Resumen de Venta - FreshControll")
            appendLine("Ticket: #${sale.ticketNumber}")
            appendLine("---------------------------")
            details.forEach { 
                appendLine("${it.productName} x ${it.quantity} = S/ ${it.totalPrice}")
            }
            appendLine("---------------------------")
            appendLine("Total: S/ ${sale.total}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Recibo de Venta #${sale.ticketNumber}")
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }
        startActivity(Intent.createChooser(intent, "Compartir recibo"))
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Continuar") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun navigateToHome() {
        findNavController().navigate(
            R.id.homeFragment,
            null,
            NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build()
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
