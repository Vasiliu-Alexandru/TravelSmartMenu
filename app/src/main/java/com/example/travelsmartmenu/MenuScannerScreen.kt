package com.example.travelsmartmenu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onSeeAllDishesClick: (List<TranslatedMenuItem>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val darkGreen = Color(0xFF193E29)
    val bgCream = Color(0xFFF7F7F2)
    val textMuted = Color(0xFF5A655E)
    val isRomanian = selectedLanguage == "Română"

    val countryProfile = remember(selectedCountry) {
        SupportedCountries.fromDisplayName(selectedCountry)
    }
    val ocrProcessor = remember(countryProfile) {
        MenuOcrProcessor(countryProfile)
    }
    val menuTranslator = remember { MenuTranslator() }

    DisposableEffect(ocrProcessor) {
        onDispose { ocrProcessor.close() }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var isProcessing by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var translatedItems by remember { mutableStateOf<List<TranslatedMenuItem>>(emptyList()) }
    var selectedDish by remember { mutableStateOf<TranslatedMenuItem?>(null) }

    // Gallery Photo Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && !isProcessing) {
            isProcessing = true
            scope.launch {
                try {
                    val bmp = loadBitmapFromUri(context, uri)
                    previewBitmap = bmp
                    val scanned = ocrProcessor.extractFromUri(context, uri)
                    translatedItems = menuTranslator.translateItems(scanned, selectedLanguage, countryProfile)
                } catch (e: Exception) {
                    Log.e("MenuScanner", "Gallery OCR/Translation failed", e)
                } finally {
                    isProcessing = false
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
        containerColor = if (previewBitmap != null) bgCream else Color.Black
    ) { innerPadding ->
        val currentImage = previewBitmap

        if (currentImage != null) {
            // =================================================================
            // SIDE-BY-SIDE VIEW: Menu Picture on Left + Clickable Dishes on Right
            // =================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT: Menu Image + Retake Button
                    Column(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFDCDFD9)),
                            color = Color.Black,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Image(
                                bitmap = currentImage.asImageBitmap(),
                                contentDescription = "Scanned menu",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                previewBitmap = null
                                translatedItems = emptyList()
                            },
                            border = BorderStroke(1.5.dp, darkGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = darkGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRomanian) "Rescanează" else "Rescan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = darkGreen
                            )
                        }
                    }

                    // RIGHT: Clickable Translated Dishes + "See All Dishes" Button
                    Column(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight()
                    ) {
                        Text(
                            text = if (isRomanian) "Preparate (${translatedItems.size})"
                            else "Dishes (${translatedItems.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isProcessing) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = darkGreen)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isRomanian) "Se traduce..." else "Translating...",
                                        fontSize = 13.sp,
                                        color = textMuted
                                    )
                                }
                            }
                        } else if (translatedItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isRomanian) "Niciun preparat găsit."
                                    else "No dishes found.",
                                    fontSize = 14.sp,
                                    color = textMuted
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                items(translatedItems) { dish ->
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFDCDFD9)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedDish = dish }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = dish.translatedName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = darkGreen,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = dish.originalName,
                                                    fontSize = 12.sp,
                                                    color = textMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                dish.extractedPrice?.let { price ->
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = price,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = darkGreen
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onSeeAllDishesClick(translatedItems) },
                            enabled = translatedItems.isNotEmpty() && !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (isRomanian) "Vezi toate" else "See All Dishes",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // =================================================================
            // CAMERA / UPLOAD CAPTURE MODE
            // =================================================================
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
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isProcessing,
                            border = BorderStroke(1.5.dp, Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRomanian) "Încarcă" else "Upload",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        if (hasCameraPermission) {
                            Button(
                                onClick = {
                                    if (!isProcessing) {
                                        isProcessing = true
                                        captureMenuBitmap(
                                            context = context,
                                            imageCapture = imageCapture,
                                            onBitmapReady = { croppedBitmap ->
                                                previewBitmap = croppedBitmap
                                                scope.launch {
                                                    try {
                                                        val scanned = ocrProcessor.extractFromBitmap(croppedBitmap)
                                                        translatedItems = menuTranslator.translateItems(
                                                            scanned, selectedLanguage, countryProfile
                                                        )
                                                    } catch (e: Exception) {
                                                        Log.e("MenuScanner", "Camera OCR/Translation failed", e)
                                                    } finally {
                                                        isProcessing = false
                                                    }
                                                }
                                            },
                                            onError = { isProcessing = false }
                                        )
                                    }
                                },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(containerColor = darkGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(54.dp)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isRomanian) "Se traduce..." else "Translating...",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
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
    }

    selectedDish?.let { dish ->
        DishDetailDialog(
            dish = dish,
            selectedLanguage = selectedLanguage,
            onDismiss = { selectedDish = null }
        )
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
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