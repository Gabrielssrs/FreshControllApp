package com.example.freshcontroll.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.freshcontroll.databinding.FragmentCashRegisterCloseBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CashRegisterCloseFragment : Fragment() {
    private var _binding: FragmentCashRegisterCloseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CashRegisterCloseViewModel by viewModels()

    private var currentSystemBalance: Double = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCashRegisterCloseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.etCashCounted.doAfterTextChanged { text ->
            val counted = text.toString().toDoubleOrNull() ?: 0.0
            val diff = counted - currentSystemBalance

            binding.cvDifferenceAlertCard.isVisible = diff != 0.0
            binding.tvDifferenceAmount.text = "S/ ${"%.2f".format(diff)}"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.systemBalance.collect { balance ->
                    currentSystemBalance = balance
                    binding.tvSystemTotalAmount.text = "S/ $balance"
                }
            }
        }

        binding.btnSubmitCashClose.setOnClickListener {
            val amount = binding.etCashCounted.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.submitRegisterClose(amount)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}