package com.example.freshcontroll.presentation.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.databinding.FragmentNewSaleBinding
import com.example.freshcontroll.domain.model.Product
import com.example.freshcontroll.presentation.sales.adapter.SaleProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditSaleFragment : Fragment() {

    private var _binding: FragmentNewSaleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditSaleViewModel by viewModels()
    private val args: EditSaleFragmentArgs by navArgs()
    private lateinit var adapter: SaleProductAdapter
    private var productSearchAdapter: ArrayAdapter<Product>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupSearchAdapter()
        setupAdapter()
        observeUiState()
        
        viewModel.loadSale(args.saleId)
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = "Editar Venta"
        binding.tvSectionTitle.text = "Ajustar Productos"
        binding.btnConfirmSale.text = "Actualizar Resumen"
        
        // Habilitamos búsqueda/escáner para permitir agregar más productos
        binding.btnScanBarcode.visibility = View.VISIBLE
        binding.tvLabelManualSearch.visibility = View.VISIBLE
        binding.tilSearchProducts.visibility = View.VISIBLE

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnConfirmSale.setOnClickListener { viewModel.saveChanges() }

        binding.btnScanBarcode.setOnClickListener {
            val action = EditSaleFragmentDirections.actionEditSaleToBarcodeScanner(caller = "editSale")
            findNavController().navigate(action)
        }

        // Recibimos el código del escáner
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scanned_barcode")
            ?.observe(viewLifecycleOwner) { scannedBarcode ->
                if (scannedBarcode != null) {
                    viewModel.addProductToCartByBarcode(scannedBarcode)
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")
                }
            }
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

    private fun setupAdapter() {
        adapter = SaleProductAdapter(
            onQuantityChange = { productId, newQty -> viewModel.updateProductQuantity(productId, newQty) },
            onRemoveClick = { productId -> viewModel.removeProductFromCart(productId) }
        )
        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.cartUiModels.collect { items ->
                        adapter.submitList(items)
                        binding.tvCartCountLabel.text = "Artículos (${items.size})"
                    }
                }

                launch {
                    viewModel.availableProducts.collect { products ->
                        productSearchAdapter?.clear()
                        productSearchAdapter?.addAll(products)
                        productSearchAdapter?.notifyDataSetChanged()
                    }
                }

                launch {
                    viewModel.total.collect { total ->
                        binding.tvProvisionalTotalAmount.text = "Total provisional:\nS/ %.2f".format(total)
                    }
                }

                launch {
                    viewModel.editSuccessEvent.collect { saleId ->
                        val action = EditSaleFragmentDirections.actionEditSaleToSaleReceipt(saleId)
                        findNavController().navigate(action)
                    }
                }

                launch {
                    viewModel.errorEvent.collect { error ->
                        error?.let {
                            com.google.android.material.snackbar.Snackbar.make(binding.root, it, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
                            viewModel.clearErrorEvent()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
