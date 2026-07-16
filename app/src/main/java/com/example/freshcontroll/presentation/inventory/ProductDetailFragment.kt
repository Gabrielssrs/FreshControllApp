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
import coil.load
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentProductDetailBinding
import com.example.freshcontroll.domain.model.UserRole
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
            val action = ProductDetailFragmentDirections.actionProductDetailToRegisterProduct(
                productId = args.productId
            )
            findNavController().navigate(action)
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
                        // Carga de imagen con Coil
                        if (!it.imageUrl.isNullOrBlank()) {
                            binding.ivProductHeroImage.load(it.imageUrl) {
                                crossfade(true)
                                placeholder(R.drawable.ic_edit)
                                error(R.drawable.ic_edit)
                            }
                            binding.ivProductHeroImage.imageTintList = null
                        } else {
                            binding.ivProductHeroImage.setImageResource(R.drawable.ic_edit)
                            binding.ivProductHeroImage.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.borde_tarjeta)
                        }

                        binding.tvProductName.text = it.name
                        binding.tvProductCategory.text = it.category
                        binding.tvProductSku.text = if (it.barcode.isNullOrBlank()) "" else "Barcode: ${it.barcode}"

                        // Precios
                        binding.tvSalePriceValue.text = String.format("S/ %.2f", it.price)
                        binding.tvCostPriceValue.text = String.format("S/ %.2f", it.costPrice)

                        // Stock
                        binding.tvStockCurrentAmount.text = "Stock Actual: ${it.currentStock} ${it.unitType}"

                        // Fecha de Vencimiento
                        if (it.expirationDate != null && it.expirationDate > 0) {
                            val sdf = java.text.SimpleDateFormat("dd 'de' MMMM, yyyy", java.util.Locale("es", "ES"))
                            binding.tvExpirationDateValue.text = sdf.format(java.util.Date(it.expirationDate))
                        } else {
                            binding.tvExpirationDateValue.text = "Sin fecha establecida"
                        }

                        // Lógica de color de stock
                        val context = requireContext()
                        val (stockColor, stockLabel) = when {
                            it.currentStock <= 0 -> {
                                ContextCompat.getColor(context, R.color.error) to "Sin stock"
                            }
                            it.currentStock <= it.minStock -> {
                                ContextCompat.getColor(context, R.color.naranja_alerta) to "Stock bajo"
                            }
                            else -> {
                                ContextCompat.getColor(context, R.color.verde_primario) to "En stock"
                            }
                        }

                        binding.tvStockCurrentAmount.setTextColor(stockColor)
                        binding.tvStockStatusLabel.text = stockLabel
                        binding.cvStockBadge.setCardBackgroundColor(
                            if (it.currentStock <= it.minStock) ContextCompat.getColor(context, R.color.rojo_fondo_alerta)
                            else ContextCompat.getColor(context, R.color.verde_pago_aprobado_fondo)
                        )
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