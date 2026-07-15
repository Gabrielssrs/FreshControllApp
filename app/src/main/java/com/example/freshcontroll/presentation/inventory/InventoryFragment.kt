package com.example.freshcontroll.presentation.inventory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentInventoryBinding
import com.example.freshcontroll.presentation.inventory.adapter.ProductAdapter
// TODO: Importar ProductAdapter cuando lo crees
// import com.example.freshcontroll.presentation.inventory.adapter.ProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupListeners()
        observeUiState()
    }

    private fun setupAdapter() {
        val onProductClick: (String) -> Unit = { productId ->
            val action = InventoryFragmentDirections.actionInventoryToProductDetail(productId)
            findNavController().navigate(action)
        }
        productAdapter = ProductAdapter(onProductClick)
        binding.rvProducts.adapter = productAdapter
    }

    private fun setupListeners() {
        // 1. AÑADIR ESTA LÓGICA PARA LA FLECHA DE RETROCESO
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Botón principal flotante para añadir producto (funciona para lista y estado vacío)
        binding.btnAddProduct.setOnClickListener {
            val action = InventoryFragmentDirections.actionInventoryToRegisterProduct()
            findNavController().navigate(action)
        }

        // CORRECCIÓN: etSearchProducts en lugar de etSearch
        binding.etSearchProducts.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchProduct(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.products.collect { productList ->
                        productAdapter.submitList(productList)

                        // 2. AÑADIR ESTAS DOS LÍNEAS PARA EL CONTADOR DE ARTÍCULOS
                        val count = productList.size
                        binding.tvItemsCount.text = getString(R.string.mostrando_articulos_placeholder, count)
                    }
                }

                launch {
                    viewModel.isEmpty.collect { isEmpty ->
                        if (isEmpty) {
                            binding.rvProducts.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.rvProducts.visibility = View.VISIBLE
                            binding.layoutEmptyState.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvProducts.adapter = null
        _binding = null
    }
}