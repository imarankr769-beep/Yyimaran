package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun LibraryScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val favoriteList by viewModel.favoriteMovies.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("library_screen")
    ) {
        // Shelf Header
        Text(
            text = "قائمتي المفضلة",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.8).sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
        )

        if (favoriteList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "💖",
                        fontSize = 50.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "قائمتك فارغة حالياً!\nاضغط على (قائمتي) في صفحة الأفلام لحفظها هنا والوصول إليها بسرعة.",
                        color = Color.LightGray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(favoriteList) { movie ->
                    LibraryGridItem(movie = movie, onMovieClick = { onMovieClick(movie) })
                }
            }
        }
    }
}

@Composable
fun LibraryGridItem(
    movie: Movie,
    onMovieClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick() }
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0A0A))
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "⭐️ ${String.format("%.1f", movie.voteAverage)}",
            color = Color(0xFFFFD700),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
