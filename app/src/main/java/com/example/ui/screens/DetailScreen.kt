package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun DetailScreen(
    viewModel: MovieViewModel,
    onBackClick: () -> Unit,
    onPlayMovie: (String, Boolean, Int, String?) -> Unit
) {
    val movie by viewModel.selectedMovie.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isDownloaded by viewModel.isDownloaded.collectAsState()
    val trailerUrl by viewModel.currentTrailerUrl.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    movie?.let { currentMovie ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(scrollState)
                .testTag("detail_screen")
        ) {
            // 1. Movie Hero backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(currentMovie.fullBackdropUrl.ifEmpty { currentMovie.fullPosterUrl })
                        .crossfade(true)
                        .build(),
                    contentDescription = currentMovie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark ambient overlays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black
                                )
                            )
                        )
                )

                // Floating circular Back button at top-left
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            // 2. Movie Info Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = currentMovie.title,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    lineHeight = 34.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⭐️ %s".format(String.format("%.1f", currentMovie.voteAverage)),
                        color = Color(0xFFFFD700),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = currentMovie.releaseDate?.take(4) ?: "2026",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )

                    // Netflix-style HD badge
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.Gray, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "HD",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // IMDb ID metadata badge
                    if (!currentMovie.imdbId.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFF5C518), RoundedCornerShape(2.dp))
                                .background(Color(0xFFF5C518).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "IMDb: ${currentMovie.imdbId}",
                                color = Color(0xFFF5C518),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Central Interactive Actions
                // Giant Main Play Button
                Button(
                    onClick = {
                        onPlayMovie(
                            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                            true,
                            currentMovie.id,
                            currentMovie.imdbId
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("detail_play_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Film"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تشغيل الآن",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action controls: Download & My List Custom Triggers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Download state trigger
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.downloadMovie(currentMovie) }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.FileDownloadDone else Icons.Default.Download,
                            contentDescription = "Download Movie",
                            tint = if (isDownloaded) MaterialTheme.colorScheme.primary else Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isDownloaded) "تم التنزيل" else "تنزيل أوفلاين",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }

                    // Favorite list state trigger
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.toggleFavorite(currentMovie) }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Add List",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFavorite) "في قائمتي" else "إضافة لقائمتي",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }

                    // Dynamic Trailer support / Youtube trigger
                    if (trailerUrl != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    onPlayMovie(
                                        trailerUrl!!,
                                        true,
                                        currentMovie.id,
                                        currentMovie.imdbId
                                    )
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = "Watch Trailer",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "الإعلان الترويجي",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Synopsis paragraph
                Text(
                    text = "قصة الفيلم",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentMovie.overview.ifEmpty { "لا يوجد وصف متوفر لهذا العمل الفني حالياً." },
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Cast/Credits Section
                if (currentMovie.cast.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "طاقم العمل والنجوم",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(currentMovie.cast.take(10)) { actor ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(80.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(actor.fullProfileUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = actor.name,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(35.dp))
                                        .background(Color.DarkGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = actor.name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = actor.character,
                                    color = Color.Gray,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
