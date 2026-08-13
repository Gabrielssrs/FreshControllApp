package com.example.freshcontroll.presentation.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentSalesHistoryBinding
import com.example.freshcontroll.presentation.sales.adapter.RecentSaleAdapter
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

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

        setupAdapter()
        setupFilters()
        observeUiState()

        binding.btnMenu.setOnClickListener { findNavController().popBackStack() }
    }

    private fun setupAdapter() {
        val adapter = RecentSaleAdapter(
            onItemClick = { saleId -> 
                findNavController().navigate(SalesHistoryFragmentDirections.actionSalesHistoryToSaleReceipt(saleId)) 
            },
            onEditClick = { saleId ->
                findNavController().navigate(SalesHistoryFragmentDirections.actionSalesHistoryToEditSale(saleId))
            }
        )
        binding.rvSalesHistory.adapter = adapter
    }

    private fun setupFilters() {
        binding.btnFilterToday.setOnClickListener { viewModel.setFilter(TimeFilter.TODAY) }
        binding.btnFilterWeek.setOnClickListener { viewModel.setFilter(TimeFilter.WEEK) }
        binding.btnFilterMonth.setOnClickListener { viewModel.setFilter(TimeFilter.MONTH) }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    (binding.rvSalesHistory.adapter as? RecentSaleAdapter)?.submitList(state.sales)

                    // Totales y contador
                    binding.tvTotalDayAmount.text = String.format(Locale.getDefault(), "S/ %.2f", state.totalAmount)
                    binding.tvSalesCountValue.text = state.salesCount.toString()

                    // Actualizar estilos de botones de filtro
                    updateFilterButtons(state.selectedFilter)

                    // Visibilidad de sección "Por empleado"
                    binding.cvEmployeesBreakdownCard.isVisible = state.isOwner
                    if (state.isOwner) {
                        updateEmployeeBreakdown(state.employeeBreakdown)
                    }
                }
            }
        }
    }

    private fun updateFilterButtons(selected: TimeFilter) {
        val context = requireContext()
        val buttons = mapOf(
            TimeFilter.TODAY to binding.btnFilterToday,
            TimeFilter.WEEK to binding.btnFilterWeek,
            TimeFilter.MONTH to binding.btnFilterMonth
        )

        buttons.forEach { (filter, button) ->
            if (filter == selected) {
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.verde_primario))
                button.setTextColor(ContextCompat.getColor(context, R.color.blanco))
                button.strokeWidth = 0
            } else {
                button.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                button.setTextColor(ContextCompat.getColor(context, R.color.texto_principal))
                button.strokeWidth = (1 * resources.displayMetrics.density).toInt()
                button.setStrokeColorResource(R.color.borde_tarjeta)
            }
        }
    }

    private fun updateEmployeeBreakdown(breakdown: Map<String, Double>) {
        binding.layoutEmployeesList.removeAllViews()
        
        breakdown.forEach { (name, amount) ->
            val itemView = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 4, 0, 4) }
            }

            val tvName = TextView(requireContext()).apply {
                text = name
                setTextColor(ContextCompat.getColor(context, R.color.texto_principal))
                textSize = 15f
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val tvAmount = TextView(requireContext()).apply {
                text = String.format(Locale.getDefault(), "S/ %.2f", amount)
                setTextColor(ContextCompat.getColor(context, R.color.verde_primario))
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = (8 * resources.displayMetrics.density).toInt() }
            }

            itemView.addView(tvName)
            itemView.addView(tvAmount)
            binding.layoutEmployeesList.addView(itemView)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
