package com.example.freshcontroll.presentation.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.databinding.FragmentNewSaleBinding
import com.example.freshcontroll.presentation.sales.adapter.SaleProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewSaleFragment : Fragment() {

    private var _binding: FragmentNewSaleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewSaleViewModel by viewModels()
    private lateinit var adapter: SaleProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Corrección: usando removeProductFromCart
        adapter = SaleProductAdapter { viewModel.removeProductFromCart(it) }

        // Corrección: rvProducts (XML) en lugar de rvCart
        binding.rvProducts.adapter = adapter

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        // Correcciones de botones: btnScanBarcode y btnConfirmSale
        binding.btnScanBarcode.setOnClickListener {
            findNavController().navigate(NewSaleFragmentDirections.actionNewSaleToBarcodeScanner())
        }

        binding.btnConfirmSale.setOnClickListener {
            viewModel.checkoutCart()
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scanned_barcode")
            ?.observe(viewLifecycleOwner) {
                Toast.makeText(context, "Producto escaneado: $it", Toast.LENGTH_SHORT).show()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")
            }

        // TODO: implementar búsqueda manual con lista de resultados usando et_search_products
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.currentCart.collect { adapter.submitList(it) } }

                // Corrección: desestructuración correcta y conteo basado en el adaptador
                launch { viewModel.saleTotals.collect { (_, _, total) ->
                    binding.tvCartCountLabel.text = "Artículos (${adapter.currentList.size})"
                    binding.tvProvisionalTotalAmount.text = "Total provisional:\nS/ $total"
                }}

                launch { viewModel.saleCompletedEvent.collect { saleId ->
                    saleId?.let {
                        findNavController().navigate(NewSaleFragmentDirections.actionNewSaleToSaleReceipt(it))
                        viewModel.clearCompletedEvent()
                    }
                }}
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}