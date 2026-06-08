package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MovieRepository
import com.example.domain.models.DownloadedMovieEntity
import com.example.domain.models.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val billboardMovie: Movie?,
        val trendingMovies: List<Movie>,
        val popularMovies: List<Movie>,
        val topRatedMovies: List<Movie>,
        val newReleases: List<Movie>,
        val arabicMovies: List<Movie>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<Movie>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

data class PlaybackParams(
    val initialUrl: String,
    val isMovie: Boolean,
    val tmdbId: Int,
    val imdbId: String?
)

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // --- HOME STATES ---
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    // --- SEARCH STATES ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    // --- DETAILS STATES ---
    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    val selectedMovie: StateFlow<Movie?> = _selectedMovie.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded: StateFlow<Boolean> = _isDownloaded.asStateFlow()

    private val _currentTrailerUrl = MutableStateFlow<String?>(null)
    val currentTrailerUrl: StateFlow<String?> = _currentTrailerUrl.asStateFlow()

    // --- ACTIVE STREAMING STATES ---
    private val _activePlaybackUrl = MutableStateFlow<String?>(null)
    val activePlaybackUrl: StateFlow<String?> = _activePlaybackUrl.asStateFlow()

    private val _activePlaybackParams = MutableStateFlow<PlaybackParams?>(null)
    val activePlaybackParams: StateFlow<PlaybackParams?> = _activePlaybackParams.asStateFlow()

    // --- FAVORITES (LIBRARY FLOWS) ---
    val favoriteMovies: StateFlow<List<Movie>> = repository.favoriteMoviesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- DOWNLOADS FLOWS ---
    val downloadedMovies: StateFlow<List<DownloadedMovieEntity>> = repository.downloadedMoviesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _homeUiState.value = HomeUiState.Loading
            try {
                val trending = repository.getTrendingMovies()
                val popular = repository.getPopularMovies()
                val topRated = repository.getTopRatedMovies()
                val newReleases = repository.getNewReleases()
                val arabic = repository.getArabicMovies()
                
                // Select first trending movie as Billboard hero.
                val billboard = trending.firstOrNull() ?: popular.firstOrNull()

                _homeUiState.value = HomeUiState.Success(
                    billboardMovie = billboard,
                    trendingMovies = trending,
                    popularMovies = popular,
                    topRatedMovies = topRated,
                    newReleases = newReleases,
                    arabicMovies = arabic
                )
            } catch (e: Exception) {
                _homeUiState.value = HomeUiState.Error(e.localizedMessage ?: "فشل تحميل البيانات")
            }
        }
    }

    // --- SEARCH METHODS ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchUiState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            try {
                val results = repository.searchMovies(query)
                _searchUiState.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                _searchUiState.value = SearchUiState.Error(e.localizedMessage ?: "حدث خطأ أثناء البحث")
            }
        }
    }

    // --- DETAILS SCREEN CONTROLS ---
    fun selectMovie(movie: Movie) {
        _selectedMovie.value = movie
        _currentTrailerUrl.value = null
        
        // Fetch trailer, check state, fetch IMDb details & cast
        viewModelScope.launch {
            _isFavorite.value = repository.isFavorite(movie.id)
            _isDownloaded.value = repository.isDownloaded(movie.id)
            
            // Fetch credits
            val castList = repository.getMovieCredits(movie.id)
            // Fetch details & external ids to capture IMDb Id
            val details = repository.getMovieDetailsWithExternalIds(movie.id)
            
            // Build enhanced movie object
            val enhancedMovie = movie.copy(
                imdbId = details?.imdbId ?: movie.imdbId,
                cast = castList
            )
            
            _selectedMovie.value = enhancedMovie
            
            val trailer = repository.getMovieTrailerUrl(movie.id)
            _currentTrailerUrl.value = trailer
        }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            val fav = repository.isFavorite(movie.id)
            if (fav) {
                repository.removeFromFavorites(movie.id)
                _isFavorite.value = false
            } else {
                repository.addToFavorites(movie)
                _isFavorite.value = true
            }
        }
    }

    // Download dynamic simulate
    fun downloadMovie(movie: Movie) {
        viewModelScope.launch {
            val isProd = repository.isDownloaded(movie.id)
            if (!isProd) {
                // Predefined trailer link or video source for playback
                val videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                repository.addDownload(movie, sizeMb = (120..450).random(), videoUrl = videoUrl)
                _isDownloaded.value = true
            } else {
                repository.deleteDownload(movie.id)
                _isDownloaded.value = false
            }
        }
    }

    fun deleteDownloadedMovie(movieId: Int) {
        viewModelScope.launch {
            repository.deleteDownload(movieId)
            if (_selectedMovie.value?.id == movieId) {
                _isDownloaded.value = false
            }
        }
    }

    // Start movie playback (either standard promo links or YouTube trailers)
    fun setPlaybackUrl(url: String?) {
        _activePlaybackUrl.value = url
    }

    fun setPlaybackParams(params: PlaybackParams?) {
        _activePlaybackParams.value = params
    }

    // Factory Class pattern
    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MovieViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
