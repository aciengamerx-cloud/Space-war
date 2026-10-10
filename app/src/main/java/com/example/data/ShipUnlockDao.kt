package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipUnlockDao {
    @Query("SELECT * FROM ship_unlocks")
    fun getAllShipUnlocks(): Flow<List<ShipUnlockEntity>>

    @Query("SELECT * FROM ship_unlocks WHERE shipId = :shipId")
    suspend fun getShipUnlock(shipId: String): ShipUnlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateShip(ship: ShipUnlockEntity)
}
