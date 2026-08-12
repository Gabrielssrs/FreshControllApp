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
import java.util.Locale

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
        
        setupListeners()
        setupAdapter()
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnNewSale.setOnClickListener {
            findNavController().navigate(EmployeeSalesHistoryFragmentDirections.actionEmployeeSalesHistoryToNewSale())
        }
    }

    private fun setupAdapter() {
        val adapter = EmployeeSaleAdapter(
            onItemClick = { id -> 
                findNavController().navigate(EmployeeSalesHistoryFragmentDirections.actionEmployeeSalesHistoryToSaleReceipt(id)) 
            },
            onEditClick = { id ->
                findNavController().navigate(EmployeeSalesHistoryFragmentDirections.actionEmployeeSalesHistoryToEditSale(id))
            }
        )
        binding.rvEmployeeSales.adapter = adapter
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.personalSales.collect { sales ->
                    (binding.rvEmployeeSales.adapter as? EmployeeSaleAdapter)?.submitList(sales)

                    binding.tvMyTotalDayAmount.text = String.format(Locale.getDefault(), "S/ %.2f", sales.sumOf { it.total })
                    binding.tvSalesPerformedCount.text = "${sales.size}"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}