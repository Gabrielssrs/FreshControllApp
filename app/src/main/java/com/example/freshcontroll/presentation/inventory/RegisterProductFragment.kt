package com.example.freshcontroll.presentation.inventory

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.R // Asegúrate de tener este import correcto
import com.example.freshcontroll.databinding.FragmentRegisterProductBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class RegisterProductFragment : Fragment() {

    private var _binding: FragmentRegisterProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterProductViewModel by viewModels()
    private val args: RegisterProductFragmentArgs by navArgs()

    private var scannedBarcodeValue: String? = null
    private var prefilledImageUrlValue: String? = null
    private var selectedExpirationTimestamp: Long? = null // Variable para la fecha

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdownMenus() // 1. Cargamos categorías y unidades
        setupListeners()
        checkForPrefilledArguments()
        observeScannerResult()
        observeUiState()
    }

    private fun setupDropdownMenus() {
        val categorias = arrayOf("Abarrotes", "Bebidas", "Lácteos", "Limpieza", "Embutidos", "Verduras", "Otros")
        val adapterCategorias = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categorias)
        binding.actvCategory.setAdapter(adapterCategorias)

        val unidades = arrayOf("Unidad (Pza)", "Kilogramo (kg)", "Litro (L)", "Paquete (Pqt)", "Caja (Cj)")
        val adapterUnidades = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, unidades)
        binding.actvUnit.setAdapter(adapterUnidades)


    }



    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Lógica del DatePicker para la fecha de vencimiento
        binding.etExpirationDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, day)
                selectedExpirationTimestamp = selectedCalendar.timeInMillis
                binding.etExpirationDate.setText("$day/${month + 1}/$year")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnScanBarcode.setOnClickListener {
            val action = RegisterProductFragmentDirections.actionRegisterProductToBarcodeScanner(caller = "inventory")
            findNavController().navigate(action)
        }

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val category = binding.actvCategory.text.toString().trim()
            val unitType = binding.actvUnit.text.toString().trim()

            val price = binding.etSalePrice.text.toString().toDoubleOrNull() ?: 0.0
            val currentStock = binding.etInitialStock.text.toString().toDoubleOrNull() ?: 0.0
            val minStock = binding.etMinStockAlert.text.toString().toDoubleOrNull() ?: 0.0

            viewModel.onSaveProduct(
                barcode = scannedBarcodeValue,
                name = name,
                category = category,
                sku = "",
                price = price,
                currentStock = currentStock,
                minStock = minStock,
                unitType = unitType,
                expirationDate = selectedExpirationTimestamp, // Usamos la variable guardada
                imageUrl = prefilledImageUrlValue
            )
        }
    }

    private fun checkForPrefilledArguments() {
        args.barcode?.let { barcode ->
            scannedBarcodeValue = barcode
            if (!args.prefilledName.isNullOrBlank()) binding.etProductName.setText(args.prefilledName)
            if (!args.prefilledCategory.isNullOrBlank()) binding.actvCategory.setText(args.prefilledCategory, false)
            prefilledImageUrlValue = args.prefilledImageUrl
        }
    }

    private fun observeScannerResult() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scanned_barcode")
            ?.observe(viewLifecycleOwner) { scannedBarcode ->
                scannedBarcodeValue = scannedBarcode
                Snackbar.make(binding.root, "Código escaneado con éxito", Snackbar.LENGTH_SHORT).show()
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")
            }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RegisterProductUiState.Loading -> binding.btnSaveProduct.isEnabled = false
                        is RegisterProductUiState.Success -> {
                            binding.btnSaveProduct.isEnabled = true
                            findNavController().navigate(RegisterProductFragmentDirections.actionRegisterProductToInventory())
                        }
                        is RegisterProductUiState.Error -> {
                            binding.btnSaveProduct.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                        else -> binding.btnSaveProduct.isEnabled = true
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