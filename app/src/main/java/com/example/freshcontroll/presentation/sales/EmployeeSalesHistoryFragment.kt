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
import com.example.freshcontroll.databinding.FragmentEmployeeSalesHistoryBinding
import com.example.freshcontroll.presentation.sales.adapter.EmployeeSaleAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmployeeSalesHistoryFragment : Fragment() {

    private var _binding: FragmentEmployeeSalesHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmployeeSalesHistoryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmployeeSalesHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = EmployeeSaleAdapter { findNavController().navigate(EmployeeSalesHistoryFragmentDirections.actionEmployeeSalesHistoryToSaleReceipt(it)) }
        binding.rvEmployeeSales.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.personalSales.collect { sales ->
                    adapter.submitList(sales)

                    // Correcciones aplicadas:
                    // Usamos tvMyTotalDayAmount (antes tvTotalDay)
                    binding.tvMyTotalDayAmount.text = "S/ ${sales.sumOf { it.total }}"

                    // Usamos tvSalesPerformedCount (antes tvCount)
                    binding.tvSalesPerformedCount.text = "${sales.size}"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}