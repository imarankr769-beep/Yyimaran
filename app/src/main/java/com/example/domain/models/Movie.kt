package com.example.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val genreIds: List<Int>? = emptyList(),
    val imdbId: String? = null,
    val cast: List<CastMember> = emptyList()
) : Serializable {
    val fullPosterUrl: String
        get() = if (posterPath.isNullOrEmpty()) "" else "https://image.tmdb.org/t/p/w500$posterPath"

    val fullBackdropUrl: String
        get() = if (backdropPath.isNullOrEmpty()) "" else "https://image.tmdb.org/t/p/w780$backdropPath"
}

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?
) : Serializable {
    val fullProfileUrl: String
        get() = if (profilePath.isNullOrEmpty()) "" else "https://image.tmdb.org/t/p/w185$profilePath"
}

@Entity(tableName = "favorite_movies")
data class FavoriteMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toMovie(): Movie = Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        genreIds = emptyList()
    )

    companion object {
        fun fromMovie(movie: Movie): FavoriteMovieEntity = FavoriteMovieEntity(
            id = movie.id,
            title = movie.title,
            overview = movie.overview,
            posterPath = movie.posterPath,
            backdropPath = movie.backdropPath,
            releaseDate = movie.releaseDate,
            voteAverage = movie.voteAverage
        )
    }
}

// Simulated Download model to power the "Downloads" screen
@Entity(tableName = "downloaded_movies")
data class DownloadedMovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val sizeMb: Int,
    val videoUrl: String,
    val downloadedAt: Long = System.currentTimeMillis()
) {
    fun toMovie(): Movie = Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = null,
        releaseDate = null,
        voteAverage = 0.0
    )
}

data class Genre(
    val id: Int,
    val name: String
)

data class MovieVideo(
    val id: String,
    val key: String, // YouTube video key
    val name: String,
    val site: String,
    val type: String
)
