package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.domain.models.FavoriteMovieEntity
import com.example.domain.models.DownloadedMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMovieDao {
    @Query("SELECT * FROM favorite_movies ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteMovieEntity)

    @Query("DELETE FROM favorite_movies WHERE id = :movieId")
    suspend fun deleteFavoriteById(movieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE id = :movieId LIMIT 1)")
    suspend fun isFavorite(movieId: Int): Boolean
}

@Dao
interface DownloadedMovieDao {
    @Query("SELECT * FROM downloaded_movies ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(movie: DownloadedMovieEntity)

    @Query("DELETE FROM downloaded_movies WHERE id = :movieId")
    suspend fun deleteDownloadById(movieId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_movies WHERE id = :movieId LIMIT 1)")
    suspend fun isDownloaded(movieId: Int): Boolean
}

@Database(
    entities = [FavoriteMovieEntity::class, DownloadedMovieEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao
    abstract fun downloadedMovieDao(): DownloadedMovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getDatabase(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "imaranflix_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
