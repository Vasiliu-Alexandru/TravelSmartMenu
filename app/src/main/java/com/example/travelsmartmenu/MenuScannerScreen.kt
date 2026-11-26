package com.example.travelsmartmenu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScannerScreen(
    selectedLanguage: String,
    selectedCountry: String = "China",
    onBackClick: () -> Unit,
    onMenuCaptured: (List<ScannedMenuItem>) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val darkGreen = Color(0xFF193E29)
    val bgCream = Color(0xFFF7F7F2)
    val isRomanian = selectedLanguage == "Română"

    val countryProfile = remember(selectedCountry) {
        SupportedCountries.fromDisplayName(selectedCountry)
    }
    val ocrProcessor = remember(countryProfile) {
        MenuOcrProcessor(countryProfile)
    }

    DisposableEffect(ocrProcessor) {
        onDispose { ocrProcessor.close() }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var isScanning by remember { mutableStateOf(false) }
    var extractedItems by remember { mutableStateOf<List<ScannedMenuItem>>(emptyList()) }
    var showResultsSheet by remember { mutableStateOf(false) }

    // Gallery Photo Picker (no extra permissions needed)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && !isScanning) {
            isScanning = true
            scope.launch {
                try {
                    val items = ocrProcessor.extractFromUri(context, uri)
                    extractedItems = items
                    showResultsSheet = true
                    onMenuCaptured(items)
                } catch (e: Exception) {
                    Log.e("MenuScanner", "Gallery OCR failed", e)
                } finally {
                    isScanning = false
                }
            }
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRomanian) "Scanează meniul" else "Scan Menu",
                        fontWeight = FontWeight.Bold,
                        color = darkGreen
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = darkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgCream)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Log.e("MenuScanner", "Camera binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Viewfinder framing guide
                Surface(
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.45f)
                ) {}
            } else {
                // Fallback if camera permission is denied (Upload still works!)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgCream)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRomanian) "Permite camera sau încarcă o poză din galerie."
                        else "Allow camera access or upload a menu photo from your gallery.",
                        color = darkGreen,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = darkGreen)
                    ) {
                        Text(
                            text = if (isRomanian) "Permite camera" else "Grant Camera",
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom Bar: Capture Photo OR Upload from Gallery
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasCameraPermission) {
                    Text(
                        text = if (isRomanian) "Încadrează preparatele sau încarcă o poză"
                        else "Align menu dishes inside the frame or upload a photo",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Upload from Gallery button
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isScanning,
                        border = BorderStroke(1.5.dp, Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRomanian) "Încarcă" else "Upload",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Take Picture button
                    if (hasCameraPermission) {
                        Button(
                            onClick = {
                                if (!isScanning) {
                                    isScanning = true
                                    captureMenuBitmap(
                                        context = context,
                                        imageCapture = imageCapture,
                                        onBitmapReady = { croppedBitmap ->
                                            scope.launch {
                                                try {
                                                    val items = ocrProcessor.extractFromBitmap(croppedBitmap)
                                                    extractedItems = items
                                                    showResultsSheet = true
                                                    onMenuCaptured(items)
                                                } catch (e: Exception) {
                                                    Log.e("MenuScanner", "Camera OCR failed", e)
                                                } finally {
                                                    isScanning = false
                                                }
                                            }
                                        },
                                        onError = { isScanning = false }
                                    )
                                }
                            },
                            enabled = !isScanning,
                            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(54.dp)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isRomanian) "Se scanează..." else "Scanning...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isRomanian) "Capturează" else "Capture",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet showing the extracted individual dishes
    if (showResultsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showResultsSheet = false },
            containerColor = bgCream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isRomanian) "Preparate extrase (${extractedItems.size})"
                    else "Extracted Dishes (${extractedItems.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkGreen
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (extractedItems.isEmpty()) {
                    Text(
                        text = if (isRomanian) "Nu s-au găsit preparate. Încearcă o imagine mai clară."
                        else "No dishes found. Try a clearer image or move closer.",
                        color = Color(0xFF5A655E),
                        fontSize = 15.sp
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 380.dp)
                    ) {
                        items(extractedItems) { item ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFDCDFD9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.dishName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1E2420),
                                        modifier = Modifier.weight(1f)
                                    )
                                    item.extractedPrice?.let { price ->
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = price,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = darkGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun captureMenuBitmap(
    context: Context,
    imageCapture: ImageCapture,
    onBitmapReady: (Bitmap) -> Unit,
    onError: () -> Unit
) {
    imageCapture.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                try {
                    val bitmap = image.toRotatedCroppedBitmap()
                    onBitmapReady(bitmap)
                } finally {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("MenuScanner", "Photo capture failed", exception)
                onError()
            }
        }
    )
}