package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController

@Composable
fun ServerChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color(0xFF141414))
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 11.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.LightGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    initialUrl: String,
    isMovie: Boolean,
    tmdbId: Int,
    imdbId: String? = null,
    season: Int? = null,
    episode: Int? = null,
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as? ComponentActivity

    val resolvedId = remember(imdbId, tmdbId) {
        imdbId?.takeIf { it.startsWith("tt") } ?: imdbId?.let { "tt$it" } ?: tmdbId.toString()
    }

    val servers = remember {
        listOf(
            "https://vidsrc.to/embed/" to "VidSrc.to",
            "https://vidsrc.dev/embed/" to "VidSrc.dev",
            "https://embed.su/embed/" to "Embed.su",
            "https://2embed.to/embed/" to "2Embed",
            "https://www.2embed.online/embed/" to "2Embed Online"
        )
    }

    // Current State
    var selectedServerIndex by remember { mutableStateOf(0) }
    val resolvedDefaultUrl = remember(isMovie, resolvedId, season, episode) {
        buildDefaultUrl(isMovie, tmdbId, imdbId, season, episode)
    }
    var currentUrl by remember { mutableStateOf(initialUrl.ifBlank { resolvedDefaultUrl }) }
    
    // Auto native vs web mode
    val isStreamOnly = remember(initialUrl) {
        initialUrl.contains(".mp4") || initialUrl.contains(".m3u8") || tmdbId == 0
    }
    
    var isNativePlayerMode by remember { mutableStateOf(isStreamOnly) }
    var isLandscape by remember { mutableStateOf(false) }

    // Initialize ExoPlayer state
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(initialUrl.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" })
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // Manage Lifecycle & pause ExoPlayer when necessary
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isNativePlayerMode) {
                        exoPlayer.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.stop()
            exoPlayer.release()
            // Reset portrait orientation on exit
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Monitor screen rotation change manually
    LaunchedEffect(activity?.requestedOrientation) {
        val r = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        isLandscape = (r == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || r == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    }

    // Helper toggle orientations
    val toggleOrientation = {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isLandscape = false
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isLandscape = true
        }
    }

    // Main Layout Content
    if (isLandscape) {
        // LANDSCAPE MODE: True fullscreen experience
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("player_screen_fullscreen")
        ) {
            if (isNativePlayerMode) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                mediaPlaybackRequiresUserGesture = false
                                allowFileAccess = true
                                allowContentAccess = true
                                loadsImagesAutomatically = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36"
                            }
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val url = request?.url?.toString() ?: ""
                                    val isAllowed = url.contains("vidsrc") || 
                                                    url.contains("embed") || 
                                                    url.contains("2embed") || 
                                                    url.contains("player") ||
                                                    url.contains("google")
                                    return !isAllowed
                                }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript("""
                                        setTimeout(() => {
                                            document.querySelectorAll('video').forEach(v => {
                                                v.muted = false;
                                                v.play().catch(console.log);
                                            });
                                        }, 2000);
                                    """.trimIndent(), null)
                                }
                            }
                            webChromeClient = WebChromeClient()
                            loadUrl(currentUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Controls Toolbar Overlay in Fullscreen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { toggleOrientation() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "تدوير الشاشة",
                        tint = Color.White
                    )
                }
            }
        }
    } else {
        // PORTRAIT MODE: Clean informative hierarchy layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("player_screen_portrait")
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(100.dp))
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Player",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "مشغل الوسائط المحسّن",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { toggleOrientation() },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(100.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Landscape full screen mode",
                        tint = Color.White
                    )
                }
            }

            // 16:9 Immersive Video Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color(0xFF0D0D0D))
            ) {
                if (isNativePlayerMode) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    mediaPlaybackRequiresUserGesture = false
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    loadsImagesAutomatically = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36"
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        val url = request?.url?.toString() ?: ""
                                        val isAllowed = url.contains("vidsrc") || 
                                                        url.contains("embed") || 
                                                        url.contains("2embed") || 
                                                        url.contains("player") ||
                                                        url.contains("google")
                                        return !isAllowed
                                    }
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        view?.evaluateJavascript("""
                                            setTimeout(() => {
                                                document.querySelectorAll('video').forEach(v => {
                                                    v.muted = false;
                                                    v.play().catch(console.log);
                                                });
                                            }, 2000);
                                        """.trimIndent(), null)
                                    }
                                }
                                webChromeClient = WebChromeClient()
                                loadUrl(currentUrl)
                            }
                        },
                        update = { webView ->
                            if (webView.url != currentUrl) {
                                webView.loadUrl(currentUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Controls, Server list and Info Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF050505))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Interactive Mode Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { isNativePlayerMode = false; exoPlayer.pause() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isNativePlayerMode) MaterialTheme.colorScheme.primary else Color(0xFF141414),
                            contentColor = if (!isNativePlayerMode) Color.White else Color.Gray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("سيرفرات الويب", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = { isNativePlayerMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNativePlayerMode) MaterialTheme.colorScheme.primary else Color(0xFF141414),
                            contentColor = if (isNativePlayerMode) Color.White else Color.Gray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Native Player", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 16.dp))

                if (!isNativePlayerMode) {
                    // Web Server Selection List
                    Text(
                        text = "اختر سيرفر البث (Server Selector):",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        servers.forEachIndexed { index, pair ->
                            val (base, label) = pair
                            val isSelected = index == selectedServerIndex
                            ServerChip(
                                label = label,
                                selected = isSelected,
                                onClick = {
                                    selectedServerIndex = index
                                    currentUrl = if (isMovie) "${base}movie/$resolvedId"
                                                else "${base}tv/$resolvedId/${season ?: 1}/${episode ?: 1}"
                                }
                            )
                        }
                    }
                } else {
                    // Native Player Information display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(16.dp)
                            .padding(bottom = 20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تشغيل البث الداخلي (ExoPlayer)",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "يستخدم هذا المشغل البث المحلي لمعاينة الفيديو الترويجي بجودة عالية وبدون أي إعلانات منبثقة.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action primary launch browser button
                Button(
                    onClick = {
                        val targetUrl = if (!isNativePlayerMode) currentUrl else initialUrl
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("open_in_browser_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "فتح في المتصفح الخارجي"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "فتح في المتصفح (مضمون)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "🚨 ملاحظة: إذا تجمّد السيرفر أو لم يعمل البث بشكل صحيح، اضغط على زر \"فتح في المتصفح (مضمون)\" للتشغيل المباشر والسلس خارج التطبيق.",
                    color = Color.LightGray.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun buildDefaultUrl(isMovie: Boolean, tmdbId: Int, imdbId: String?, season: Int?, episode: Int?): String {
    val id = imdbId?.takeIf { it.startsWith("tt") } ?: imdbId?.let { "tt$it" } ?: tmdbId.toString()
    return if (isMovie) "https://vidsrc.to/embed/movie/$id"
           else "https://vidsrc.to/embed/tv/$id/${season ?: 1}/${episode ?: 1}"
}
