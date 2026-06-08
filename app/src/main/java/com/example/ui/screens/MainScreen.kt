package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.models.Movie
import com.example.ui.navigation.BottomNavItem
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun MainScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    onPlayMovie: (String) -> Unit
) {
    // Standard tabs tracking
    var currentTab by rememberSaveable { mutableStateOf<String>(BottomNavItem.Home.route) }

    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Library,
        BottomNavItem.Downloads
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .drawBehind {
                        drawLine(
                            color = Color(0xFFFFFFFF).copy(alpha = 0.05f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .testTag("app_bottom_nav_bar"),
                containerColor = Color(0xFF0A0A0A),
                tonalElevation = 0.dp
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentTab == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab.route },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.titleEn
                            )
                        },
                        label = {
                            Text(
                                text = tab.titleAr, // Premium Arabic localized label
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // Account for bottom bar spacing safely
        ) {
            when (currentTab) {
                BottomNavItem.Home.route -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onMovieClick = onMovieClick,
                        onPlayMovie = onPlayMovie
                    )
                }
                BottomNavItem.Search.route -> {
                    SearchScreen(
                        viewModel = viewModel,
                        onMovieClick = onMovieClick
                    )
                }
                BottomNavItem.Library.route -> {
                    LibraryScreen(
                        viewModel = viewModel,
                        onMovieClick = onMovieClick
                    )
                }
                BottomNavItem.Downloads.route -> {
                    DownloadsScreen(
                        viewModel = viewModel,
                        onPlayMovie = onPlayMovie
                    )
                }
            }
        }
    }
}
