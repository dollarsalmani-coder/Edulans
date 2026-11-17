package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.ZoomState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.CameraFocusOverlay
import com.example.ui.components.CameraZoomControls
import com.example.ui.components.FocusStatus
import com.example.ui.components.TapFocusState
import com.example.ui.components.clampZoomRatio
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val TAG = "CameraPreviewScreen"

@Composable
fun CameraPreviewScreen(
    lowBandwidthMode: Boolean,
    languageNativeName: String,
    onBack: () -> Unit,
    onUploadFromGallery: () -> Unit,
    onUseSystemCamera: () -> Unit,
    onPhotoCaptured: (bytes: ByteArray, rotationDegrees: Int, autoAnalyze: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionRequestedOnce = true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !permissionRequestedOnce) {
            permissionRequestedOnce = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionFallback(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onUploadFromGallery = onUploadFromGallery,
            onUseSystemCamera = onUseSystemCamera,
            onBack = onBack,
            modifier = modifier
        )
    } else {
        CameraXLivePreviewContent(
            lowBandwidthMode = lowBandwidthMode,
            languageNativeName = languageNativeName,
            onBack = onBack,
            onUploadFromGallery = onUploadFromGallery,
            onUseSystemCamera = onUseSystemCamera,
            onPhotoCaptured = onPhotoCaptured,
            modifier = modifier
        )
    }
}

@Composable
private fun CameraPermissionFallback(
    onRequestPermission: () -> Unit,
    onUploadFromGallery: () -> Unit,
    onUseSystemCamera: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Camera Access Needed",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Allow camera permission to scan any object in real time, or use your device camera app / photo gallery.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("grant_camera_permission_button")
                ) {
                    Text("Grant Camera Permission")
                }

                OutlinedButton(
                    onClick = onUseSystemCamera,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("camera_fallback_system_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Use System Camera")
                }

                OutlinedButton(
                    onClick = onUploadFromGallery,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("camera_fallback_upload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload From Gallery")
                }

                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("camera_permission_back_button")
                ) {
                    Text("Back to Explore")
                }
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun CameraXLivePreviewContent(
    lowBandwidthMode: Boolean,
    languageNativeName: String,
    onBack: () -> Unit,
    onUploadFromGallery: () -> Unit,
    onUseSystemCamera: () -> Unit,
    onPhotoCaptured: (bytes: ByteArray, rotationDegrees: Int, autoAnalyze: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var useFrontCamera by remember { mutableStateOf(false) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var autoAnalyze by remember { mutableStateOf(true) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraErrorMessage by remember { mutableStateOf<String?>(null) }
    var hasFrontCamera by remember { mutableStateOf(false) }
    var hasBackCamera by remember { mutableStateOf(true) }

    val captureExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var focusState by remember { mutableStateOf(TapFocusState()) }
    var focusRequestCounter by remember { mutableStateOf(0L) }
    var currentZoomRatio by remember { mutableFloatStateOf(1f) }
    var minZoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(1f) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    // Observe CameraX ZoomState continuously for real-time pinch & preset updates
    DisposableEffect(boundCamera, lifecycleOwner) {
        val camera = boundCamera
        if (camera == null) {
            currentZoomRatio = 1f
            minZoomRatio = 1f
            maxZoomRatio = 1f
            onDispose {}
        } else {
            val observer = Observer<ZoomState> { state ->
                currentZoomRatio = state.zoomRatio
                minZoomRatio = state.minZoomRatio
                maxZoomRatio = state.maxZoomRatio
            }
            camera.cameraInfo.zoomState.observe(lifecycleOwner, observer)
            onDispose {
                camera.cameraInfo.zoomState.removeObserver(observer)
            }
        }
    }

    // Auto-hide the focus reticle shortly after focus completes
    LaunchedEffect(focusState.requestId, focusState.status) {
        if (focusState.status == FocusStatus.LOCKED || focusState.status == FocusStatus.UNLOCKED) {
            delay(1800L)
            focusState = TapFocusState()
        } else if (focusState.status == FocusStatus.FOCUSING) {
            delay(4200L)
            if (focusState.status == FocusStatus.FOCUSING) {
                focusState = TapFocusState()
            }
        }
    }

    // Reset reticle when switching between front/back lenses
    LaunchedEffect(useFrontCamera) {
        focusState = TapFocusState()
    }

    // Pinch-to-zoom + Tap-to-focus + exposure metering coordinated on PreviewView
    LaunchedEffect(previewView, boundCamera) {
        var isPinchingInGesture = false
        val scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    isPinchingInGesture = true
                    return boundCamera != null
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val camera = boundCamera ?: return false
                    val zoomState = camera.cameraInfo.zoomState.value
                    val currentRatio = zoomState?.zoomRatio ?: currentZoomRatio
                    val minRatio = zoomState?.minZoomRatio ?: minZoomRatio
                    val maxRatio = zoomState?.maxZoomRatio ?: maxZoomRatio
                    val nextRatio = clampZoomRatio(
                        targetRatio = currentRatio * detector.scaleFactor,
                        minRatio = minRatio,
                        maxRatio = maxRatio
                    )
                    camera.cameraControl.setZoomRatio(nextRatio)
                    return true
                }
            }
        )

        previewView.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isPinchingInGesture = false
                    boundCamera != null
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    isPinchingInGesture = true
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val camera = boundCamera ?: return@setOnTouchListener false
                    if (!isPinchingInGesture && !scaleGestureDetector.isInProgress) {
                        val reqId = ++focusRequestCounter
                        val tapOffset = Offset(event.x, event.y)
                        focusState = TapFocusState(
                            tapOffset = tapOffset,
                            status = FocusStatus.FOCUSING,
                            requestId = reqId
                        )

                        val point = previewView.meteringPointFactory.createPoint(event.x, event.y)
                        val action = FocusMeteringAction.Builder(
                            point,
                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                        )
                            .setAutoCancelDuration(4, TimeUnit.SECONDS)
                            .build()

                        if (camera.cameraInfo.isFocusMeteringSupported(action)) {
                            val focusFuture = camera.cameraControl.startFocusAndMetering(action)
                            focusFuture.addListener(
                                {
                                    if (focusRequestCounter != reqId) return@addListener
                                    val result = runCatching { focusFuture.get() }.getOrNull()
                                    focusState = focusState.copy(
                                        status = if (result?.isFocusSuccessful == true) {
                                            FocusStatus.LOCKED
                                        } else {
                                            FocusStatus.UNLOCKED
                                        }
                                    )
                                },
                                mainExecutor
                            )
                        } else {
                            focusState = focusState.copy(status = FocusStatus.LOCKED)
                        }

                        view.performClick()
                    }
                    isPinchingInGesture = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    isPinchingInGesture = false
                    true
                }
                else -> boundCamera != null
            }
        }
    }

    // Update flash mode instantaneously without rebinding CameraX use cases
    LaunchedEffect(flashMode, imageCapture) {
        imageCapture?.flashMode = flashMode
    }

    DisposableEffect(Unit) {
        onDispose {
            captureExecutor.shutdown()
        }
    }

    DisposableEffect(lifecycleOwner, useFrontCamera, lowBandwidthMode) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var boundProvider: ProcessCameraProvider? = null

        val listener = Runnable {
            try {
                val provider = cameraProviderFuture.get()
                boundProvider = provider

                val backAvailable = runCatching {
                    provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                }.getOrDefault(false)
                val frontAvailable = runCatching {
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                }.getOrDefault(false)

                hasBackCamera = backAvailable
                hasFrontCamera = frontAvailable

                val selector = when {
                    useFrontCamera && frontAvailable -> CameraSelector.DEFAULT_FRONT_CAMERA
                    backAvailable -> CameraSelector.DEFAULT_BACK_CAMERA
                    frontAvailable -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> {
                        cameraErrorMessage = "No camera hardware detected on this device."
                        return@Runnable
                    }
                }

                val targetSize = if (lowBandwidthMode) Size(640, 480) else Size(1024, 768)
                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            targetSize,
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }

                val captureUseCase = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setResolutionSelector(resolutionSelector)
                    .setFlashMode(flashMode)
                    .build()

                provider.unbindAll()
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    captureUseCase
                )
                boundCamera = camera
                imageCapture = captureUseCase
                cameraErrorMessage = null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind CameraX use cases", e)
                cameraErrorMessage = e.localizedMessage ?: "Unable to start camera preview."
            }
        }

        cameraProviderFuture.addListener(listener, mainExecutor)

        onDispose {
            boundProvider?.unbindAll()
            boundCamera = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraErrorMessage == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camerax_preview_view")
            )

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 42.dp, vertical = 156.dp)
            ) {
                val stroke = 3.dp.toPx()
                val cornerLen = 32.dp.toPx()
                val bracketColor = Color.White.copy(alpha = 0.85f)

                drawLine(bracketColor, Offset(0f, 0f), Offset(cornerLen, 0f), stroke, StrokeCap.Round)
                drawLine(bracketColor, Offset(0f, 0f), Offset(0f, cornerLen), stroke, StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), stroke, StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width, 0f), Offset(size.width, cornerLen), stroke, StrokeCap.Round)
                drawLine(bracketColor, Offset(0f, size.height), Offset(cornerLen, size.height), stroke, StrokeCap.Round)
                drawLine(bracketColor, Offset(0f, size.height), Offset(0f, size.height - cornerLen), stroke, StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), stroke, StrokeCap.Round)
                drawLine(bracketColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), stroke, StrokeCap.Round)
            }

            CameraFocusOverlay(
                focusState = focusState,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Camera Preview Unavailable",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = cameraErrorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onUseSystemCamera,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("camera_error_system_camera_button")
                        ) {
                            Text("Open System Camera")
                        }
                        OutlinedButton(
                            onClick = onUploadFromGallery,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("camera_error_gallery_button")
                        ) {
                            Text("Upload Photo Instead")
                        }
                    }
                }
            }
        }

        // Top overlay bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onBack,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.52f),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("camera_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Explore"
                )
            }

            Surface(
                color = Color.Black.copy(alpha = 0.56f),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = if (lowBandwidthMode) {
                        "$languageNativeName · Fast Low-Bandwidth"
                    } else {
                        "$languageNativeName · Fast Capture"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            FilledTonalIconButton(
                onClick = {
                    flashMode = when (flashMode) {
                        ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_AUTO
                        ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                        else -> ImageCapture.FLASH_MODE_OFF
                    }
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.52f),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("camera_flash_button")
            ) {
                val (flashIcon, flashDesc) = when (flashMode) {
                    ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn to "Flash on"
                    ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto to "Flash auto"
                    else -> Icons.Default.FlashOff to "Flash off"
                }
                Icon(imageVector = flashIcon, contentDescription = flashDesc)
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (cameraErrorMessage == null) {
                CameraZoomControls(
                    currentZoomRatio = currentZoomRatio,
                    minZoomRatio = minZoomRatio,
                    maxZoomRatio = maxZoomRatio,
                    onSelectZoomRatio = { ratio ->
                        boundCamera?.cameraControl?.setZoomRatio(
                            clampZoomRatio(ratio, minZoomRatio, maxZoomRatio)
                        )
                    }
                )

                val (focusText, focusTint) = when (focusState.status) {
                    FocusStatus.FOCUSING -> "Focusing on subject…" to Color.White
                    FocusStatus.LOCKED -> "Focus sharp · Ready to capture" to Color(0xFF4ADE80)
                    FocusStatus.UNLOCKED -> "Focus adjusted · Tap again to refine" to Color(0xFFFBBF24)
                    FocusStatus.IDLE -> "Tap to focus · Pinch to zoom" to Color.White.copy(alpha = 0.86f)
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("camera_focus_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            tint = focusTint,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = focusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = focusTint
                        )
                    }
                }
            }

            FilterChip(
                selected = autoAnalyze,
                onClick = { autoAnalyze = !autoAnalyze },
                label = {
                    Text(
                        if (autoAnalyze) {
                            "Instant Analyze: ON"
                        } else {
                            "Attach to Search Bar"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = Color.Black.copy(alpha = 0.45f),
                    labelColor = Color.White,
                    iconColor = Color.White
                ),
                modifier = Modifier.testTag("camera_auto_analyze_chip")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onUploadFromGallery,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("camera_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Choose photo from gallery"
                    )
                }

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) Color.White.copy(alpha = 0.6f) else Color.White)
                        .clickable(enabled = !isCapturing && imageCapture != null && cameraErrorMessage == null) {
                            val capture = imageCapture ?: return@clickable
                            isCapturing = true
                            capture.takePicture(
                                captureExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bytes = try {
                                            val buffer = image.planes.firstOrNull()?.buffer
                                            if (buffer != null) {
                                                ByteArray(buffer.remaining()).also { buffer.get(it) }
                                            } else {
                                                ByteArray(0)
                                            }
                                        } finally {
                                            image.close()
                                        }
                                        val rotation = image.imageInfo.rotationDegrees
                                        mainExecutor.execute {
                                            isCapturing = false
                                            if (bytes.isNotEmpty()) {
                                                onPhotoCaptured(bytes, rotation, autoAnalyze)
                                            } else {
                                                cameraErrorMessage = "Captured image was empty."
                                            }
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                                        mainExecutor.execute {
                                            isCapturing = false
                                            cameraErrorMessage = exception.localizedMessage ?: "Capture failed."
                                        }
                                    }
                                }
                            )
                        }
                        .semantics {
                            contentDescription = "Capture object photo"
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = { useFrontCamera = !useFrontCamera },
                    enabled = hasFrontCamera && hasBackCamera,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.08f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("camera_flip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch camera lens"
                    )
                }
            }
        }
    }
}
