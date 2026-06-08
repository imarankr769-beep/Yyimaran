package com.example.data.api

import com.example.domain.models.Movie
import com.example.domain.models.MovieVideo
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Retrofit Data Transfer Objects (DTOs) for TMDB integration
data class TmdbMovieResponse(
    val results: List<TmdbMovieDto>
)

data class TmdbExternalIds(
    val imdb_id: String?
)

data class TmdbMovieDto(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val genre_ids: List<Int>?,
    val imdb_id: String? = null,
    val external_ids: TmdbExternalIds? = null
) {
    fun toDomain(): Movie = Movie(
        id = id,
        title = title,
        overview = overview ?: "",
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average,
        genreIds = genre_ids,
        imdbId = imdb_id ?: external_ids?.imdb_id
    )
}

data class TmdbCreditsResponse(
    val id: Int,
    val cast: List<TmdbCastDto>
)

data class TmdbCastDto(
    val id: Int,
    val name: String,
    val character: String,
    val profile_path: String?
) {
    fun toDomain(): com.example.domain.models.CastMember = com.example.domain.models.CastMember(
        id = id,
        name = name,
        character = character,
        profilePath = profile_path
    )
}

data class TmdbVideoResponse(
    val results: List<TmdbVideoDto>
)

data class TmdbVideoDto(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String
) {
    fun toDomain(): MovieVideo = MovieVideo(
        id = id,
        key = key,
        name = name,
        site = site,
        type = type
    )
}

interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ar-SA" // Match Arabic preference or default
    ): TmdbMovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ar-SA"
    ): TmdbMovieResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-SA"
    ): TmdbMovieResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "ar-SA"
    ): TmdbMovieResponse

    @GET("discover/movie")
    suspend fun discoverArabicMovies(
        @Query("api_key") apiKey: String,
        @Query("with_original_language") originalLanguage: String = "ar",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("language") language: String = "ar-SA"
    ): TmdbMovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-SA"
    ): TmdbMovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-SA"
    ): TmdbMovieDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetailsWithExternalIds(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "external_ids",
        @Query("language") language: String = "ar-SA"
    ): TmdbMovieDto

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "ar-SA"
    ): TmdbCreditsResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): TmdbVideoResponse

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"

        fun create(): TmdbApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TmdbApiService::class.java)
        }
    }
}
