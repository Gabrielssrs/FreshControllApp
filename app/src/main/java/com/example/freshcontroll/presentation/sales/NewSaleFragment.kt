package com.example.freshcontroll.presentation.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.databinding.FragmentNewSaleBinding
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.presentation.sales.adapter.SaleProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewSaleFragment : Fragment() {

    private var _binding: FragmentNewSaleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewSaleViewModel by viewModels()
    private lateinit var adapter: SaleProductAdapter
    private var productSearchAdapter: ArrayAdapter<Product>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SaleProductAdapter(
            onQuantityChange = { productId, newQty -> viewModel.updateProductQuantity(productId, newQty) },
            onRemoveClick = { productId -> viewModel.removeProductFromCart(productId) }
        )
        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        setupSearchAdapter()
        setupListeners()
        observeUiState()
    }

    private fun setupSearchAdapter() {
        productSearchAdapter = ArrayAdapter<Product>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.etSearchProducts.setAdapter(productSearchAdapter)

        binding.etSearchProducts.setOnItemClickListener { parent, _, position, _ ->
            val product = parent.getItemAtPosition(position) as Product
            viewModel.addProductToCart(product, 1.0)
            binding.etSearchProducts.setText("")
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnScanBarcode.setOnClickListener {
            // CORRECCIÓN: Le indicamos al escáner que venimos de Nueva Venta
            val action = NewSaleFragmentDirections.actionNewSaleToBarcodeScanner(caller = "newSale")
            findNavController().navigate(action)
        }

        binding.btnConfirmSale.setOnClickListener {
            viewModel.checkoutCart()
        }

        // CORRECCIÓN: Recibimos el código del escáner pasivo
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scanned_barcode")
            ?.observe(viewLifecycleOwner) { scannedBarcode ->
                if (scannedBarcode != null) {
                    viewModel.addProductToCartByBarcode(scannedBarcode)
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")
                }
            }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { 
                    viewModel.currentCart.collect { 
                        adapter.submitList(it.toList()) 
                        binding.tvCartCountLabel.text = "Artículos (${it.size})"
                    } 
                }

                launch {
                    viewModel.availableProducts.collect { products ->
                        productSearchAdapter?.clear()
                        productSearchAdapter?.addAll(products)
                        productSearchAdapter?.notifyDataSetChanged()
                    }
                }

                launch { viewModel.saleTotals.collect { (_, _, total) ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}