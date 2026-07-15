package com.example.freshcontroll.presentation.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.FragmentSaleReceiptBinding
import com.example.freshcontroll.presentation.sales.adapter.SoldProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SaleReceiptFragment : Fragment() {

    private var _binding: FragmentSaleReceiptBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SaleReceiptViewModel by viewModels()
    private val args: SaleReceiptFragmentArgs by navArgs()
    private val soldAdapter = SoldProductAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSaleReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Corrección: rvProducts (XML) en lugar de rvSoldProducts
        binding.rvProducts.adapter = soldAdapter
        viewModel.fetchReceipt(args.saleId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.receiptData.collect { data ->
                    data?.let { (sale, details) ->
                        // Corrección: IDs reales del XML
                        binding.tvTotalAmount.text = "S/ ${sale.total}"
                        binding.tvSubtotalAmount.text = "S/ ${sale.subtotal}" // Asumiendo campo en Sale
                        binding.tvTaxesAmount.text = "S/ ${sale.taxes}"       // Asumiendo campo en Sale

                        // Si no tienes el ID badge_edited en XML, esta línea fallará
                        // binding.badgeEdited.isVisible = sale.isEdited

                        soldAdapter.submitList(details)
                    }
                }
            }
        }

        // Corrección: btnConfirmSale (XML) en lugar de btnNewSale
        binding.btnConfirmSale.setOnClickListener {
            findNavController().navigate(
                R.id.newSaleFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build()
            )
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}