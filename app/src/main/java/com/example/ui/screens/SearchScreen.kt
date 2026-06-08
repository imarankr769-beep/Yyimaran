package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.viewmodel.SearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchUiState by viewModel.searchUiState.collectAsState()
    val homeUiState by viewModel.homeUiState.collectAsState()

    // Interactive Filter States
    var selectedGenre by remember { mutableStateOf<Int?>(null) }
    var selectedYear by remember { mutableStateOf<String?>(null) }
    var selectedRating by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("search_screen")
    ) {
        // Search Input Header
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .testTag("search_input_field"),
            placeholder = { Text("بحث عن أفلام، مسلسلات...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.LightGray)
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        // Interactive Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Genre Selector
            var showGenreMenu by remember { mutableStateOf(false) }
            Box {
                FilterChip(
                    selected = selectedGenre != null,
                    onClick = { showGenreMenu = true },
                    label = {
                        Text(
                            text = when (selectedGenre) {
                                28 -> "أكشن"
                                18 -> "دراما"
                                35 -> "كوميدي"
                                878 -> "خيال علمي"
                                else -> "التصنيف"
                            },
                            color = if (selectedGenre != null) Color.Black else Color.White
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
                DropdownMenu(
                    expanded = showGenreMenu,
                    onDismissRequest = { showGenreMenu = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("الكل", color = Color.White) },
                        onClick = { selectedGenre = null; showGenreMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("أكشن", color = Color.White) },
                        onClick = { selectedGenre = 28; showGenreMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("دراما", color = Color.White) },
                        onClick = { selectedGenre = 18; showGenreMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("كوميدي", color = Color.White) },
                        onClick = { selectedGenre = 35; showGenreMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("خيال علمي", color = Color.White) },
                        onClick = { selectedGenre = 878; showGenreMenu = false }
                    )
                }
            }

            // Year Selector
            var showYearMenu by remember { mutableStateOf(false) }
            Box {
                FilterChip(
                    selected = selectedYear != null,
                    onClick = { showYearMenu = true },
                    label = { Text(selectedYear ?: "السنة", color = if (selectedYear != null) Color.Black else Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
                DropdownMenu(
                    expanded = showYearMenu,
                    onDismissRequest = { showYearMenu = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("الكل", color = Color.White) },
                        onClick = { selectedYear = null; showYearMenu = false }
                    )
                    listOf("2026", "2025", "2024", "2023", "2022", "2021", "2020").forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year, color = Color.White) },
                            onClick = { selectedYear = year; showYearMenu = false }
                        )
                    }
                }
            }

            // Rating Selector
            var showRatingMenu by remember { mutableStateOf(false) }
            Box {
                FilterChip(
                    selected = selectedRating != null,
                    onClick = { showRatingMenu = true },
                    label = { Text(if (selectedRating == null) "التقييم" else "⭐️ $selectedRating+", color = if (selectedRating != null) Color.Black else Color.White) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
                DropdownMenu(
                    expanded = showRatingMenu,
                    onDismissRequest = { showRatingMenu = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("الكل", color = Color.White) },
                        onClick = { selectedRating = null; showRatingMenu = false }
                    )
                    listOf(8.0, 7.0, 6.0).forEach { rating ->
                        DropdownMenuItem(
                            text = { Text("⭐️ $rating+", color = Color.White) },
                            onClick = { selectedRating = rating; showRatingMenu = false }
                        )
                    }
                }
            }

            if (selectedGenre != null || selectedYear != null || selectedRating != null) {
                Text(
                    text = "إعادة ضبط",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            selectedGenre = null
                            selectedYear = null
                            selectedRating = null
                        }
                        .padding(horizontal = 4.dp)
                )
            }
        }

        // Contents Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            val genreFilter = selectedGenre
            val yearFilter = selectedYear
            val ratingFilter = selectedRating

            when (val state = searchUiState) {
                is SearchUiState.Idle -> {
                    // Show recommended/popular movies list on idle state
                    val recommendedMovies = when (val homeState = homeUiState) {
                        is com.example.ui.viewmodel.HomeUiState.Success -> homeState.popularMovies
                        else -> emptyList()
                    }

                    val filteredRecommended = recommendedMovies.filter { movie ->
                        val matchesGenre = genreFilter == null || movie.genreIds?.contains(genreFilter) == true
                        val matchesYear = yearFilter == null || movie.releaseDate?.startsWith(yearFilter) == true
                        val matchesRating = ratingFilter == null || movie.voteAverage >= ratingFilter
                        matchesGenre && matchesYear && matchesRating
                    }

                    if (filteredRecommended.isNotEmpty()) {
                        Column {
                            Text(
                                text = "أكثر عمليات البحث شيوعاً",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 90.dp)
                            ) {
                                items(filteredRecommended) { movie ->
                                    SearchGridItem(movie = movie, onMovieClick = { onMovieClick(movie) })
                                }
                            }
                        }
                    } else {
                        EmptyStatePrompt(message = "لم يتم العثور على توصيات تطابق هذه الفلاتر")
                    }
                }
                is SearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is SearchUiState.Success -> {
                    val filteredResults = state.results.filter { movie ->
                        val matchesGenre = genreFilter == null || movie.genreIds?.contains(genreFilter) == true
                        val matchesYear = yearFilter == null || movie.releaseDate?.startsWith(yearFilter) == true
                        val matchesRating = ratingFilter == null || movie.voteAverage >= ratingFilter
                        matchesGenre && matchesYear && matchesRating
                    }

                    if (filteredResults.isEmpty()) {
                        EmptyStatePrompt(message = "لم يتم العثور على نتائج لـ \"$searchQuery\" بفلاترك الحالية")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(filteredResults) { movie ->
                                SearchGridItem(movie = movie, onMovieClick = { onMovieClick(movie) })
                            }
                        }
                    }
                }
                is SearchUiState.Error -> {
                    EmptyStatePrompt(message = "حدث خطأ أثناء تحميل نتائج البحث")
                }
            }
        }
    }
}

@Composable
fun SearchGridItem(
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
            letterSpacing = (-0.3).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EmptyStatePrompt(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "🎬",
                fontSize = 44.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = message,
                color = Color.LightGray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
