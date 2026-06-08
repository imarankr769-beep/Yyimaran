package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.api.TmdbApiService
import com.example.data.database.MovieDatabase
import com.example.data.repository.MovieRepository
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.viewmodel.PlaybackParams

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize resources with robust manual DI (Singletons & Construction pattern)
                val database = recuerdenDb() ?: MovieDatabase.getDatabase(applicationContext)
                val apiService = TmdbApiService.create()
                val repository = MovieRepository(
                    apiService = apiService,
                    favoriteMovieDao = database.favoriteMovieDao(),
                    downloadedMovieDao = database.downloadedMovieDao()
                )
                
                // Create activity-scoped ViewModel
                val movieViewModel: MovieViewModel = viewModel(
                    factory = MovieViewModel.Factory(repository)
                )

                // Set up the high-level application routing NavHost
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Splash screen
                    composable("splash") {
                        SplashScreen(
                            onNavigateToMain = {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Main App bottom navigation hub hosting Home, Search, My List, Downloads
                    composable("main") {
                        MainScreen(
                            viewModel = movieViewModel,
                            onMovieClick = { movie ->
                                movieViewModel.selectMovie(movie)
                                navController.navigate("movie_details")
                            },
                            onPlayMovie = { url ->
                                movieViewModel.setPlaybackParams(null)
                                movieViewModel.setPlaybackUrl(url)
                                navController.navigate("video_player")
                            }
                        )
                    }

                    // Selected movie details sheet view
                    composable("movie_details") {
                        DetailScreen(
                            viewModel = movieViewModel,
                            onBackClick = { navController.popBackStack() },
                            onPlayMovie = { initialUrl, isMovie, tmdbId, imdbId ->
                                movieViewModel.setPlaybackParams(
                                    PlaybackParams(initialUrl, isMovie, tmdbId, imdbId)
                                )
                                movieViewModel.setPlaybackUrl(initialUrl)
                                navController.navigate("video_player")
                            }
                        )
                    }

                    // Media3 Exoplayer stream playback sheet
                    composable("video_player") {
                        val activeParams = movieViewModel.activePlaybackParams.collectAsState().value
                        val activeUrl = movieViewModel.activePlaybackUrl.collectAsState().value
                            ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        
                        PlayerScreen(
                            initialUrl = activeParams?.initialUrl ?: activeUrl,
                            isMovie = activeParams?.isMovie ?: true,
                            tmdbId = activeParams?.tmdbId ?: 0,
                            imdbId = activeParams?.imdbId,
                            season = null,
                            episode = null,
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    // Helper function to resolve scope variables cleanly
    private fun recuerdenDb(): MovieDatabase? = null
}
