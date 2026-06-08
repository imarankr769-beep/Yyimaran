package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToMain: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val opacity = remember { Animatable(0.0f) }

    LaunchedEffect(key1 = true) {
        // Animate the ImaranFlix logo scaling and opacity concurrently
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 1200)
        )
    }

    LaunchedEffect(key1 = true) {
        opacity.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(1800) // Keep the splash screen open or warm up resources
        onNavigateToMain()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF080000),
                        Color(0xFF140101)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ImaranFlix Cinematic Logo Text
            Text(
                text = "IMARANFLIX",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 4.sp,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(opacity.value)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "سـيـنـمـا فـي جـيـبـك",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(opacity.value)
            )

            Spacer(modifier = Modifier.height(60.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(24.dp)
                    .alpha(opacity.value)
            )
        }
    }
}
