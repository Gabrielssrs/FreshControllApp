package com.example.freshcontroll.presentation.inventory

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
import com.example.freshcontroll.databinding.FragmentProductDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()

        // Cargar producto al iniciar
        viewModel.loadProductDetails(args.productId)
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAdjustStock.setOnClickListener {
            val action = ProductDetailFragmentDirections.actionProductDetailToAdjustStock(args.productId)
            findNavController().navigate(action)
        }

        // CORRECCIÓN: btnEditProduct en lugar de btnEdit
        binding.btnEditProduct.setOnClickListener {
            // TODO: Implementar navegación a edición (ej. reusando RegisterProductFragment con args)
        }

        // CORRECCIÓN: btnDeleteProduct en lugar de btnDelete
        binding.btnDeleteProduct.setOnClickListener {
            // TODO: Implementar lógica de eliminación y diálogo de confirmación
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.product.collect { product ->
                    product?.let {
                        binding.tvProductName.text = it.name
                        binding.tvProductCategory.text = it.category

                        // CORRECCIÓN: tvSalePriceValue en lugar de tvProductPrice
                        binding.tvSalePriceValue.text = String.format("S/ %.2f", it.price)

                        // CORRECCIÓN: tvStockCurrentAmount en lugar de tvProductStock
                        binding.tvStockCurrentAmount.text = "Stock Actual: ${it.currentStock} ${it.unitType}"

                        // Lógica de color de stock
                        val context = requireContext()
                        val stockColor = when {
                            it.currentStock <= 0 -> ContextCompat.getColor(context, R.color.error)
                            it.currentStock <= it.minStock -> ContextCompat.getColor(context, R.color.naranja_alerta)
                            else -> ContextCompat.getColor(context, R.color.verde_primario)
                        }

                        // Aplicamos el color al texto correcto
                        binding.tvStockCurrentAmount.setTextColor(stockColor)
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