package com.example.freshcontroll.presentation.sales

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
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.databinding.FragmentNewSaleBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewSaleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupAdapter()
        observeUiState()
        
        viewModel.loadSale(args.saleId)
    }

    private fun setupUI() {
        binding.tvHeaderTitle.text = "Editar Venta"
        binding.tvSectionTitle.text = "Modificar Artículos"
        binding.btnConfirmSale.text = "Guardar Cambios"
        
        // Deshabilitamos búsqueda/escáner por ahora para enfocarnos en edición de lo existente
        binding.btnScanBarcode.visibility = View.GONE
        binding.tvLabelManualSearch.visibility = View.GONE
        binding.tilSearchProducts.visibility = View.GONE

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnConfirmSale.setOnClickListener { viewModel.saveChanges() }
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
                    viewModel.currentCart.collect { items ->
                        adapter.submitList(items.toList())
                        binding.tvCartCountLabel.text = "Artículos (${items.size})"
                    }
                }

                launch {
                    viewModel.total.collect { total ->
                        binding.tvProvisionalTotalAmount.text = "Nuevo Total:\nS/ %.2f".format(total)
                    }
                }

                launch {
                    viewModel.editSuccessEvent.collect { saleId ->
                        val action = EditSaleFragmentDirections.actionEditSaleToSaleReceipt(saleId)
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
