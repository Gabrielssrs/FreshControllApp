package com.example.freshcontroll.presentation.home

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
import com.example.freshcontroll.databinding.FragmentNotificationsBinding
import com.example.freshcontroll.presentation.home.adapter.AlertType
import com.example.freshcontroll.presentation.home.adapter.NotificationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()

    // Asumimos la existencia de este adaptador que idealmente usa un ListAdapter de Android
    private lateinit var outOfStockAdapter: NotificationAdapter
    private lateinit var expiringAdapter: NotificationAdapter
    private lateinit var lowStockAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupListeners()
        observeUiState()
    }

    private fun setupAdapters() {
        val onProductClick: (String) -> Unit = { productId ->
            val action = NotificationsFragmentDirections.actionNotificationsToProductDetail(productId)
            findNavController().navigate(action)
        }

        // Pasamos el Enum AlertType y el lambda a cada instancia
        outOfStockAdapter = NotificationAdapter(AlertType.OUT_OF_STOCK, onProductClick)
        expiringAdapter = NotificationAdapter(AlertType.EXPIRING, onProductClick)
        lowStockAdapter = NotificationAdapter(AlertType.LOW_STOCK, onProductClick)

        binding.rvOutOfStock.adapter = outOfStockAdapter
        binding.rvExpiringProducts.adapter = expiringAdapter
        binding.rvLowStock.adapter = lowStockAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.alertsState.collect { alerts ->
                    val outOfStockList = alerts["outOfStock"] ?: emptyList()
                    val expiringList = alerts["expiring"] ?: emptyList()
                    val lowStockList = alerts["lowStock"] ?: emptyList()

                    // Sección: Sin Stock
                    if (outOfStockList.isEmpty()) {
                        // CORRECCIÓN: layoutSectionOutOfStock
                        binding.layoutSectionOutOfStock.visibility = View.GONE
                    } else {
                        binding.layoutSectionOutOfStock.visibility = View.VISIBLE
                        outOfStockAdapter.submitList(outOfStockList)
                    }

                    // Sección: Por Vencer
                    if (expiringList.isEmpty()) {
                        // CORRECCIÓN: layoutSectionExpiringProducts
                        binding.layoutSectionExpiringProducts.visibility = View.GONE
                    } else {
                        binding.layoutSectionExpiringProducts.visibility = View.VISIBLE
                        expiringAdapter.submitList(expiringList)
                    }

                    // Sección: Stock Bajo
                    if (lowStockList.isEmpty()) {
                        // CORRECCIÓN: layoutSectionLowStock
                        binding.layoutSectionLowStock.visibility = View.GONE
                    } else {
                        binding.layoutSectionLowStock.visibility = View.VISIBLE
                        lowStockAdapter.submitList(lowStockList)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // CORRECCIÓN: rvExpiringProducts
        binding.rvOutOfStock.adapter = null
        binding.rvExpiringProducts.adapter = null
        binding.rvLowStock.adapter = null
        _binding = null
    }
}