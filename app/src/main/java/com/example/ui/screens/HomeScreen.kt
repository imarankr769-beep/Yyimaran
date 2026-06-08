package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.models.Movie
import com.example.ui.viewmodel.HomeUiState
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    onPlayMovie: (String) -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val favoriteList by viewModel.favoriteMovies.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("home_screen")
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp) // Cushion for Bottom Nav Bar
                ) {
                    // 1. Double Banner Auto-Sliding Carousel Section
                    item {
                        AutoSlidingHeroCarousel(
                            movies = state.trendingMovies,
                            favoriteList = favoriteList,
                            onMovieClick = onMovieClick,
                            onToggleList = { viewModel.toggleFavorite(it) },
                            onPlayClick = { movie ->
                                onPlayMovie("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                            }
                        )
                    }

                    // 2. Horizontally scrollable rows
                    item {
                        MovieSectionRow(
                            title = "الرائج الآن",
                            movies = state.trendingMovies,
                            onMovieClick = onMovieClick
                        )
                    }

                    item {
                        MovieSectionRow(
                            title = "الأعلى تقييماً",
                            movies = state.topRatedMovies,
                            onMovieClick = onMovieClick
                        )
                    }

                    item {
                        MovieSectionRow(
                            title = "الإصدارات الجديدة",
                            movies = state.newReleases,
                            onMovieClick = onMovieClick
                        )
                    }

                    item {
                        MovieSectionRow(
                            title = "الأفلام والمسلسلات العربية",
                            movies = state.arabicMovies,
                            onMovieClick = onMovieClick
                        )
                    }
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { viewModel.loadHomeData() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("إعادة المحاولة", color = Color.White)
                        }
                    }
                }
            }
        }

        // Top App Overlay (Netflix Brand look)
        HomeTopBar()
    }
}

@Composable
fun AutoSlidingHeroCarousel(
    movies: List<Movie>,
    favoriteList: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onToggleList: (Movie) -> Unit,
    onPlayClick: (Movie) -> Unit
) {
    if (movies.isEmpty()) return

    var currentIndex by remember { mutableStateOf(0) }

    // Auto-slide effect
    LaunchedEffect(key1 = currentIndex) {
        kotlinx.coroutines.delay(4000)
        currentIndex = (currentIndex + 1) % minOf(movies.size, 5)
    }

    val currentMovie = movies.getOrNull(currentIndex) ?: return
    val isFavorite = favoriteList.any { it.id == currentMovie.id }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
    ) {
        // Hero Backdrop
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentMovie.fullPosterUrl)
                .crossfade(true)
                .build(),
            contentDescription = currentMovie.title,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onMovieClick(currentMovie) },
            contentScale = ContentScale.Crop
        )

        // Gradient Dark Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.95f),
                            Color.Black
                        )
                    )
                )
        )

        // Indicator dots for slides
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(minOf(movies.size, 5)) { index ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.7f))
                )
            }
        }

        // Title and control overlay (Centered/Aligned towards the bottom)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "الأكثر شعبية اليوم",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Movie Title
            Text(
                text = currentMovie.title,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rating Stars indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⭐️ ${String.format("%.1f", currentMovie.voteAverage)}",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = currentMovie.releaseDate?.take(4) ?: "2026",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Play & Add List Row Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // My List Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onToggleList(currentMovie) }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = "My List",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFavorite) "في قائمتي" else "قائمتي",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }

                // Big Play button
                Button(
                    onClick = { onPlayClick(currentMovie) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(38.dp)
                        .testTag("billboard_play_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تشغيل",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Info / More Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onMovieClick(currentMovie) }
                ) {
                    Text(
                        text = "ℹ️",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "معلومات",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MovieSectionRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movies) { movie ->
                MovieCardItem(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

@Composable
fun MovieCardItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(movie.fullPosterUrl)
                .crossfade(true)
                .build(),
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.82f), Color.Transparent)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "IMARANFLIX",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1.2).sp
        )
        Spacer(modifier = Modifier.width(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "أفلام",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { }
            )
            Text(
                "مسلسلات",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
            Text(
                "الفئات",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }
    }
}
