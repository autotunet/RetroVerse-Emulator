package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allGames: Flow<List<GameEntity>> = gameDao.getAllGames()
    val favoriteGames: Flow<List<GameEntity>> = gameDao.getFavoriteGames()

    suspend fun getGameById(id: Long): GameEntity? = gameDao.getGameById(id)

    suspend fun insertGame(game: GameEntity): Long = gameDao.insertGame(game)

    suspend fun insertGames(games: List<GameEntity>) = gameDao.insertGames(games)

    suspend fun updateGame(game: GameEntity) = gameDao.updateGame(game)

    suspend fun deleteGame(game: GameEntity) = gameDao.deleteGame(game)

    suspend fun toggleFavorite(id: Long) = gameDao.toggleFavorite(id)

    suspend fun recordPlaySession(id: Long, minutes: Int, timestamp: Long) =
        gameDao.recordPlaySession(id, minutes, timestamp)

    fun getSaveStatesForGame(gameId: Long): Flow<List<SaveStateEntity>> =
        gameDao.getSaveStatesForGame(gameId)

    suspend fun getSaveState(gameId: Long, slot: Int): SaveStateEntity? =
        gameDao.getSaveState(gameId, slot)

    suspend fun insertSaveState(saveState: SaveStateEntity): Long =
        gameDao.insertSaveState(saveState)

    suspend fun deleteSaveState(gameId: Long, slot: Int) =
        gameDao.deleteSaveState(gameId, slot)

    fun getMappingsForDevice(deviceName: String): Flow<List<ControllerMappingEntity>> =
        gameDao.getMappingsForDevice(deviceName)

    suspend fun saveMapping(mapping: ControllerMappingEntity) =
        gameDao.insertMapping(mapping)

    suspend fun resetMappings(deviceName: String) =
        gameDao.clearMappingsForDevice(deviceName)
}
