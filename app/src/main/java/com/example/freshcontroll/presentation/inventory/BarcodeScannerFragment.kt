package com.example.freshcontroll.presentation.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.freshcontroll.databinding.FragmentBarcodeScannerBinding
import com.example.freshcontroll.domain.repository.BarcodeLookupResult
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class BarcodeScannerFragment : Fragment() {

    private var _binding: FragmentBarcodeScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BarcodeScannerViewModel by viewModels()
    private val args: BarcodeScannerFragmentArgs by navArgs()

    private lateinit var cameraExecutor: ExecutorService
    private var hasProcessedBarcode = false
    private var lastScannedBarcode: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            if (_binding != null) {
                Snackbar.make(binding.root, "Se requiere permiso de cámara para escanear", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok") { findNavController().navigateUp() }.show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarcodeScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupListeners()
        observeUiState()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnManualEntry.setOnClickListener {
            // TODO: Mostrar un diálogo simple para ingresar el código a mano
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // CORRECCIÓN ANTI-NPE: Si el usuario presionó Atrás antes de que la cámara cargue, detenemos el proceso
            if (_binding == null) return@addListener

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                exc.printStackTrace()
                if (_binding != null) {
                    Snackbar.make(binding.root, "Error al iniciar la cámara: ${exc.localizedMessage}", Snackbar.LENGTH_SHORT).show()
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: androidx.camera.core.ImageProxy) {
        // CORRECCIÓN ANTI-NPE: Protección para flujos asíncronos
        if (_binding == null) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null && !hasProcessedBarcode) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val barcodeValue = barcodes.firstOrNull { it.valueType == Barcode.TYPE_PRODUCT || it.rawValue != null }?.rawValue
                    if (barcodeValue != null && !hasProcessedBarcode) {
                        hasProcessedBarcode = true
                        lastScannedBarcode = barcodeValue

                        // CORRECCIÓN DE FLUJO: Evaluamos quién llamó al escáner
                        if (args.caller == "newSale") {
                            returnBarcodeToPreviousScreen(barcodeValue)
                        } else {
                            viewModel.processBarcode(barcodeValue)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (_binding == null) return@collect // CORRECCIÓN ANTI-NPE

                    when (state) {
                        is ScannerUiState.Idle -> {}
                        is ScannerUiState.Loading -> {}
                        is ScannerUiState.Success -> {
                            handleLookupResult(state.result)
                        }
                        is ScannerUiState.Error -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            hasProcessedBarcode = false
                        }
                    }
                }
            }
        }
    }

    private fun handleLookupResult(result: BarcodeLookupResult) {
        when (result) {
            is BarcodeLookupResult.LocalSuccess -> {
                val action = BarcodeScannerFragmentDirections.actionBarcodeScannerToAdjustStock(result.product.id)
                findNavController().navigate(action)
            }
            is BarcodeLookupResult.RemoteSuccess -> {
                val action = BarcodeScannerFragmentDirections.actionBarcodeScannerToRegisterProduct(
                    barcode = result.barcode,
                    prefilledName = result.prefilledName,
                    prefilledCategory = result.prefilledCategory,
                    prefilledImageUrl = result.prefilledImageUrl
                )
                findNavController().navigate(action)
            }
            is BarcodeLookupResult.NotFound -> {
                val action = BarcodeScannerFragmentDirections.actionBarcodeScannerToRegisterProduct(
                    barcode = result.barcode,
                    prefilledName = null,
                    prefilledCategory = null,
                    prefilledImageUrl = null
                )
                findNavController().navigate(action)
            }
            is BarcodeLookupResult.Error -> {
                if (_binding != null) {
                    Snackbar.make(binding.root, result.message, Snackbar.LENGTH_LONG).show()
                }
                hasProcessedBarcode = false
            }
        }
    }

    private fun returnBarcodeToPreviousScreen(barcode: String) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}