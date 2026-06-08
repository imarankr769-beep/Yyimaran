package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.models.DownloadedMovieEntity
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun DownloadsScreen(
    viewModel: MovieViewModel,
    onPlayMovie: (String) -> Unit
) {
    val downloadsList by viewModel.downloadedMovies.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("downloads_screen")
    ) {
        // Shelf Header
        Text(
            text = "التنزيلات",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.8).sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
        )

        if (downloadsList.isEmpty()) {
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
                        "📥",
                        fontSize = 50.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "ليس لديك تنزيلات حالياً.\nقم بالدخول على أي فيلم، وانقر على زر التنزيل لمشاهدته لاحقاً أوفلاين في أي وقت بدون إنترنت!",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloadsList) { download ->
                    DownloadItemRow(
                        download = download,
                        onPlayClick = { onPlayMovie(download.videoUrl) },
                        onDeleteClick = { viewModel.deleteDownloadedMovie(download.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItemRow(
    download: DownloadedMovieEntity,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster thumbnail with Play indicator overlay
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(130.dp)
                    .clickable { onPlayClick() }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(download.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" })
                        .crossfade(true)
                        .build(),
                    contentDescription = download.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Center Play Icon inside semi-transparent box
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Download",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Info and storage size
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = download.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = download.overview,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "الحجم: ${download.sizeMb} ميجا بايت",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete local item Button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Download",
                    tint = Color.Gray
                )
            }
        }
    }
}
