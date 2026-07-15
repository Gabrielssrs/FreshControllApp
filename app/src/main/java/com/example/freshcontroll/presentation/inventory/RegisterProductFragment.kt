package com.example.freshcontroll.presentation.inventory

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
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.databinding.FragmentRegisterProductBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterProductFragment : Fragment() {

    private var _binding: FragmentRegisterProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterProductViewModel by viewModels()

    // Recuperamos los SafeArgs opcionales que configuramos en main_nav_graph
    private val args: RegisterProductFragmentArgs by navArgs()

    // Variable para guardar el código escaneado temporalmente ya que no hay campo en XML
    private var scannedBarcodeValue: String? = null

    // Variable para almacenar de forma temporal la URL de imagen remota si se consigue en la API
    private var prefilledImageUrlValue: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        checkForPrefilledArguments() // 1. Verificamos si venimos con datos autocompletados
        observeScannerResult()        // 2. Mantiene compatibilidad con escaneo manual/pasivo anterior
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnScanBarcode.setOnClickListener {
            // Pasamos explícitamente el 'caller' como "inventory" para habilitar la búsqueda inteligente
            val action = RegisterProductFragmentDirections.actionRegisterProductToBarcodeScanner(caller = "inventory")
            findNavController().navigate(action)
        }

        binding.btnSaveProduct.setOnClickListener {
            val name = binding.etProductName.text.toString().trim()
            val category = binding.actvCategory.text.toString().trim()
            val unitType = binding.actvUnit.text.toString().trim()

            // Campos que no existen en XML
            val sku = ""
            val barcode = scannedBarcodeValue

            // Correcciones de precios y stock
            val price = binding.etSalePrice.text.toString().toDoubleOrNull() ?: 0.0
            val currentStock = binding.etInitialStock.text.toString().toDoubleOrNull() ?: 0.0
            val minStock = binding.etMinStockAlert.text.toString().toDoubleOrNull() ?: 0.0

            // TODO: Integrar DatePicker
            val expirationDate: Long? = null

            viewModel.onSaveProduct(
                barcode = barcode,
                name = name,
                category = category,
                sku = sku,
                price = price,
                currentStock = currentStock,
                minStock = minStock,
                unitType = unitType,
                expirationDate = expirationDate,
                imageUrl = prefilledImageUrlValue // Pasamos la URL obtenida si existe
            )
        }
    }

    /**
     * Revisa si se pasaron argumentos desde el escáner inteligente y autocompleta el formulario.
     */
    private fun checkForPrefilledArguments() {
        args.barcode?.let { barcode ->
            scannedBarcodeValue = barcode

            // Si además se obtuvieron datos de Open Food Facts, autocompletamos los EditText correspondientes
            if (!args.prefilledName.isNullOrBlank()) {
                binding.etProductName.setText(args.prefilledName)
            }
            if (!args.prefilledCategory.isNullOrBlank()) {
                binding.actvCategory.setText(args.prefilledCategory)
            }

            prefilledImageUrlValue = args.prefilledImageUrl

            Snackbar.make(
                binding.root,
                "Código autocompletado desde base externa exitosamente",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeScannerResult() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scanned_barcode")
            ?.observe(viewLifecycleOwner) { scannedBarcode ->
                // Guardamos el código escaneado en nuestra variable local
                scannedBarcodeValue = scannedBarcode

                // Mostramos un mensaje para que el usuario sepa que funcionó
                Snackbar.make(binding.root, "Código escaneado con éxito", Snackbar.LENGTH_SHORT).show()

                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("scanned_barcode")
            }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RegisterProductUiState.Idle -> {
                            binding.btnSaveProduct.isEnabled = true
                        }
                        is RegisterProductUiState.Loading -> {
                            binding.btnSaveProduct.isEnabled = false
                        }
                        is RegisterProductUiState.Success -> {
                            binding.btnSaveProduct.isEnabled = true
                            val action = RegisterProductFragmentDirections.actionRegisterProductToInventory()
                            findNavController().navigate(action)
                        }
                        is RegisterProductUiState.Error -> {
                            binding.btnSaveProduct.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
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