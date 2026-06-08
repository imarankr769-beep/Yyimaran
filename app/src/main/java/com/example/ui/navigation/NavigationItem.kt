package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val titleAr: String,
    val titleEn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        titleAr = "الرئيسية",
        titleEn = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Search : BottomNavItem(
        route = "search",
        titleAr = "البحث",
        titleEn = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )

    object Library : BottomNavItem(
        route = "library",
        titleAr = "قائمي",
        titleEn = "My List",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    object Downloads : BottomNavItem(
        route = "downloads",
        titleAr = "التنزيلات",
        titleEn = "Downloads",
        selectedIcon = Icons.Filled.Download,
        unselectedIcon = Icons.Outlined.Download
    )
}
