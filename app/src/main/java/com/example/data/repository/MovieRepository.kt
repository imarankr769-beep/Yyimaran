package com.example.data.repository

import com.example.data.api.TmdbApiService
import com.example.data.database.FavoriteMovieDao
import com.example.data.database.DownloadedMovieDao
import com.example.domain.models.Movie
import com.example.domain.models.MovieVideo
import com.example.domain.models.FavoriteMovieEntity
import com.example.domain.models.DownloadedMovieEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class MovieRepository(
    private val apiService: TmdbApiService,
    private val favoriteMovieDao: FavoriteMovieDao,
    private val downloadedMovieDao: DownloadedMovieDao
) {
    // TMDB API Details
    private val apiKey = "fe989735ac851dfb7a139a3dc228addd"

    // Fallback movies in case of lack of internet, TMDB limitations, etc.
    private val fallbackMovies = listOf(
        Movie(1, "The Batman", "في عامه الثاني من مكافحة الجريمة، يكشف باتمان عن الفساد في مدينة غوثام.", "/b0PljRCOgArAor0bhm4gLI49gGs.jpg", "/5P8vEqS9Uas9KLIe0zqX9gZ9677.jpg", "2022-03-01", 7.7),
        Movie(2, "Interstellar", "مجموعة من المستكشفين يسافرون عبر ثقب دودي في الفضاء لضمان بقاء البشرية.", "/gEU2m6GAnvIGvjgcj1IBg29HLOu.jpg", "/xJH0Xek7goS9o6i6g2f9gZ9677.jpg", "2014-11-05", 8.4),
        Movie(3, "Inception", "لص يسرق أسرار الشركات من خلال استخدام تكنولوجيا مشاركة الأحلام.", "/edv5CZv00G9Z5iO6g2f9gZ9677.jpg", "/8Z9X0o6g2f9gZ9677.jpg", "2010-07-15", 8.3),
        Movie(4, "Spider-Man: No Way Home", "بعد الكشف عن تفاصيل هويته، يطلب بيتر باركر المساعدة من دكتور سترينج.", "/1g0bGtIEgArAor0bhm4gLI49gGs.jpg", "/iQF9X0o6g2f9gZ9677.jpg", "2021-12-15", 8.0),
        Movie(5, "Dune: Part Two", "يتحالف بول أتريدس مع تشاني وفريمن للانتقام من المتآمرين الذين دمروا عائلته.", "/8b8S8Hn77gSyvXn77gSyvXn77gS.jpg", "/z9X0o6g2f9gZ9677.jpg", "2024-02-27", 8.2)
    )

    // Fetch popular movies
    suspend fun getPopularMovies(): List<Movie> {
        return try {
            val response = apiService.getPopularMovies(apiKey)
            if (response.results.isEmpty()) fallbackMovies else response.results.map { it.toDomain() }
        } catch (e: Exception) {
            fallbackMovies
        }
    }

    // Fetch top rated movies
    suspend fun getTopRatedMovies(): List<Movie> {
        return try {
            val response = apiService.getTopRatedMovies(apiKey)
            val domainMovies = response.results.map { it.toDomain() }
            if (domainMovies.isEmpty()) fallbackMovies.reversed() else domainMovies
        } catch (e: Exception) {
            fallbackMovies.reversed()
        }
    }

    // Fetch trending movies
    suspend fun getTrendingMovies(): List<Movie> {
        return try {
            val response = apiService.getTrendingMovies(apiKey)
            val domainMovies = response.results.map { it.toDomain() }
            if (domainMovies.isEmpty()) fallbackMovies.shuffled() else domainMovies
        } catch (e: Exception) {
            fallbackMovies.shuffled()
        }
    }

    // Fetch new releases (now playing)
    suspend fun getNewReleases(): List<Movie> {
        return try {
            val response = apiService.getNowPlayingMovies(apiKey)
            val domainMovies = response.results.map { it.toDomain() }
            if (domainMovies.isEmpty()) fallbackMovies else domainMovies
        } catch (e: Exception) {
            fallbackMovies
        }
    }

    // Fetch Arabic movies (via discover)
    suspend fun getArabicMovies(): List<Movie> {
        return try {
            val response = apiService.discoverArabicMovies(apiKey)
            val domainMovies = response.results.map { it.toDomain() }
            if (domainMovies.isEmpty()) {
                fallbackMovies.map { it.copy(title = it.title + " (مدبلج/مترجم)") }
            } else {
                domainMovies
            }
        } catch (e: Exception) {
            fallbackMovies.map { it.copy(title = it.title + " (مدبلج/مترجم)") }
        }
    }

    // Load detailed movie with appends (to get IMDb id, etc.)
    suspend fun getMovieDetailsWithExternalIds(movieId: Int): Movie? {
        return try {
            val response = apiService.getMovieDetailsWithExternalIds(movieId, apiKey)
            response.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    // Fetch Cast/Credits for a movie
    suspend fun getMovieCredits(movieId: Int): List<com.example.domain.models.CastMember> {
        return try {
            val response = apiService.getMovieCredits(movieId, apiKey)
            response.cast.map { it.toDomain() }
        } catch (e: java.lang.Exception) {
            emptyList()
        }
    }

    // Search movies
    suspend fun searchMovies(query: String): List<Movie> {
        if (query.isBlank()) return emptyList()
        return try {
            val response = apiService.searchMovies(query, apiKey)
            response.results.map { it.toDomain() }
        } catch (e: Exception) {
            // Local fallback filter
            fallbackMovies.filter { it.title.contains(query, ignoreCase = true) || it.overview.contains(query, ignoreCase = true) }
        }
    }

    // Get videos (e.g. trailer)
    suspend fun getMovieTrailerUrl(movieId: Int): String? {
        return try {
            val response = apiService.getMovieVideos(movieId, apiKey)
            // Look for a YouTube trailer
            val trailer = response.results.firstOrNull { 
                it.site.equals("YouTube", ignoreCase = true) && 
                (it.type.equals("Trailer", ignoreCase = true) || it.type.equals("Teaser", ignoreCase = true))
            }
            if (trailer != null) {
                // Return a playlink or just the raw youtube link
                "https://www.youtube.com/watch?v=${trailer.key}"
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // --- FAVORITES (LIBRARY) LAYER ---
    val favoriteMoviesFlow: Flow<List<Movie>> = favoriteMovieDao.getAllFavorites()
        .map { list -> list.map { it.toMovie() } }

    suspend fun addToFavorites(movie: Movie) {
        favoriteMovieDao.insertFavorite(FavoriteMovieEntity.fromMovie(movie))
    }

    suspend fun removeFromFavorites(movieId: Int) {
        favoriteMovieDao.deleteFavoriteById(movieId)
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        return favoriteMovieDao.isFavorite(movieId)
    }

    // --- DOWNLOADS LAYER ---
    val downloadedMoviesFlow: Flow<List<DownloadedMovieEntity>> = downloadedMovieDao.getAllDownloads()

    suspend fun addDownload(movie: Movie, sizeMb: Int, videoUrl: String) {
        val entity = DownloadedMovieEntity(
            id = movie.id,
            title = movie.title,
            overview = movie.overview,
            posterPath = movie.posterPath,
            sizeMb = sizeMb,
            videoUrl = videoUrl
        )
        downloadedMovieDao.insertDownload(entity)
    }

    suspend fun deleteDownload(movieId: Int) {
        downloadedMovieDao.deleteDownloadById(movieId)
    }

    suspend fun isDownloaded(movieId: Int): Boolean {
        return downloadedMovieDao.isDownloaded(movieId)
    }
}
