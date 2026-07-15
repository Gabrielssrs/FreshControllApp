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
import com.example.freshcontroll.databinding.FragmentSalesHistoryBinding
import com.example.freshcontroll.presentation.sales.adapter.RecentSaleAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SalesHistoryFragment : Fragment() {

    private var _binding: FragmentSalesHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SalesHistoryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSalesHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Corrección: Usamos rvSalesHistory (ID real del XML)
        val adapter = RecentSaleAdapter { findNavController().navigate(SalesHistoryFragmentDirections.actionSalesHistoryToSaleReceipt(it)) }
        binding.rvSalesHistory.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allStoreSales.collect { sales ->
                    adapter.submitList(sales)

                    // Actualización de los totales en la UI
                    binding.tvTotalDayAmount.text = "S/ ${sales.sumOf { it.total }}"
                    binding.tvSalesCountValue.text = "${sales.size}"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}