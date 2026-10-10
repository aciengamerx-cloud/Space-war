package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(context: Context) {
    private val db = GameDatabase.getDatabase(context)
    private val highScoreDao = db.highScoreDao()
    private val shipUnlockDao = db.shipUnlockDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("starstreak_prefs", Context.MODE_PRIVATE)

    val topScores: Flow<List<HighScoreEntity>> = highScoreDao.getTopScores()
    val allShipUnlocks: Flow<List<ShipUnlockEntity>> = shipUnlockDao.getAllShipUnlocks()
    val highestScore: Flow<Int?> = highScoreDao.getHighestScore()

    private val _credits = MutableStateFlow(prefs.getInt("credits", 1500))
    val credits: StateFlow<Int> = _credits.asStateFlow()

    private val _selectedShipId = MutableStateFlow(prefs.getString("selected_ship_id", "starstreak_mk1") ?: "starstreak_mk1")
    val selectedShipId: StateFlow<String> = _selectedShipId.asStateFlow()

    suspend fun saveHighScore(scoreEntity: HighScoreEntity) {
        highScoreDao.insertHighScore(scoreEntity)
    }

    suspend fun saveShipUnlock(ship: ShipUnlockEntity) {
        shipUnlockDao.insertOrUpdateShip(ship)
    }

    fun setSelectedShip(shipId: String) {
        _selectedShipId.value = shipId
        prefs.edit().putString("selected_ship_id", shipId).apply()
    }

    fun addCredits(amount: Int) {
        val newBalance = _credits.value + amount
        _credits.value = newBalance
        prefs.edit().putInt("credits", newBalance).apply()
    }

    fun spendCredits(amount: Int): Boolean {
        if (_credits.value >= amount) {
            val newBalance = _credits.value - amount
            _credits.value = newBalance
            prefs.edit().putInt("credits", newBalance).apply()
            return true
        }
        return false
    }

    suspend fun seedDefaultShipsIfEmpty() {
        // Ensure default starstreak_mk1 is available
        val defaultShip = shipUnlockDao.getShipUnlock("starstreak_mk1")
        if (defaultShip == null) {
            shipUnlockDao.insertOrUpdateShip(
                ShipUnlockEntity(
                    shipId = "starstreak_mk1",
                    isUnlocked = true,
                    skinColorHex = "#00E5FF",
                    shieldLevel = 1,
                    engineLevel = 1,
                    weaponLevel = 1,
                    selectedPrimaryWeapon = "Plasma Repeater"
                )
            )
            shipUnlockDao.insertOrUpdateShip(
                ShipUnlockEntity(
                    shipId = "vanguard_phoenix",
                    isUnlocked = false,
                    skinColorHex = "#FF5252",
                    shieldLevel = 1,
                    engineLevel = 1,
                    weaponLevel = 1,
                    selectedPrimaryWeapon = "Vulcan Railgun"
                )
            )
            shipUnlockDao.insertOrUpdateShip(
                ShipUnlockEntity(
                    shipId = "aether_wing",
                    isUnlocked = false,
                    skinColorHex = "#7C4DFF",
                    shieldLevel = 1,
                    engineLevel = 1,
                    weaponLevel = 1,
                    selectedPrimaryWeapon = "Laser Cannon"
                )
            )
            shipUnlockDao.insertOrUpdateShip(
                ShipUnlockEntity(
                    shipId = "nemesis_dread",
                    isUnlocked = false,
                    skinColorHex = "#FFD700",
                    shieldLevel = 1,
                    engineLevel = 1,
                    weaponLevel = 1,
                    selectedPrimaryWeapon = "Quantum Pulse"
                )
            )
        }
    }
}
