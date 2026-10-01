package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY lastPlayedTimestamp DESC, id DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedTimestamp DESC")
    fun getFavoriteGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: Long): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("UPDATE games SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("UPDATE games SET playTimeMinutes = playTimeMinutes + :minutes, lastPlayedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordPlaySession(id: Long, minutes: Int, timestamp: Long)

    // Save States
    @Query("SELECT * FROM save_states WHERE gameId = :gameId ORDER BY slot ASC")
    fun getSaveStatesForGame(gameId: Long): Flow<List<SaveStateEntity>>

    @Query("SELECT * FROM save_states WHERE gameId = :gameId AND slot = :slot LIMIT 1")
    suspend fun getSaveState(gameId: Long, slot: Int): SaveStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaveState(saveState: SaveStateEntity): Long

    @Query("DELETE FROM save_states WHERE gameId = :gameId AND slot = :slot")
    suspend fun deleteSaveState(gameId: Long, slot: Int)

    // Controller Mappings
    @Query("SELECT * FROM controller_mappings WHERE deviceName = :deviceName")
    fun getMappingsForDevice(deviceName: String): Flow<List<ControllerMappingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(mapping: ControllerMappingEntity): Long

    @Query("DELETE FROM controller_mappings WHERE deviceName = :deviceName")
    suspend fun clearMappingsForDevice(deviceName: String)
}

@Database(
    entities = [GameEntity::class, SaveStateEntity::class, ControllerMappingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
