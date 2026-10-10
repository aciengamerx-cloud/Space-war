package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.HighScoreEntity
import com.example.data.ShipUnlockEntity
import com.example.game.audio.SoundManager
import com.example.game.engine.GameEngine
import com.example.game.engine.GameStateMode
import com.example.ui.ShipInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    PLAYING_GAME,
    HANGAR_WORKSHOP,
    LEADERBOARD
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GameRepository(application)
    val soundManager = SoundManager(application)
    val gameEngine = GameEngine(soundManager)

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN_MENU)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    val highScores: StateFlow<List<HighScoreEntity>> = repository.topScores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shipUnlocks: StateFlow<List<ShipUnlockEntity>> = repository.allShipUnlocks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val credits = repository.credits
    val selectedShipId = repository.selectedShipId

    private var isGameLoopRunning = false

    init {
        viewModelScope.launch {
            repository.seedDefaultShipsIfEmpty()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun startCampaign() {
        viewModelScope.launch {
            val selectedId = selectedShipId.value
            val unlocks = shipUnlocks.value
            val currentUnlock = unlocks.find { it.shipId == selectedId }
            val hexColor = currentUnlock?.skinColorHex ?: "#00E5FF"

            gameEngine.startNewGame(selectedId, hexColor, 1)
            _currentScreen.value = AppScreen.PLAYING_GAME
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        if (isGameLoopRunning) return
        isGameLoopRunning = true

        viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()
            while (isGameLoopRunning) {
                val now = System.nanoTime()
                val deltaTime = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                if (_currentScreen.value == AppScreen.PLAYING_GAME) {
                    gameEngine.updateGameLoop(deltaTime)
                }

                delay(16) // ~60 FPS
            }
        }
    }

    fun saveCurrentFlightScore(playerName: String) {
        val score = gameEngine.score.value
        val kills = gameEngine.kills.value
        val ast = gameEngine.asteroidsDestroyed.value
        val shots = gameEngine.shotsFired.value
        val hits = gameEngine.shotsHit.value
        val accuracy = if (shots > 0) ((hits.toFloat() / shots) * 100).toInt() else 0

        val earnedCredits = (score / 10) + (kills * 50) + (ast * 20)
        repository.addCredits(earnedCredits)

        viewModelScope.launch {
            repository.saveHighScore(
                HighScoreEntity(
                    playerName = playerName,
                    score = score,
                    sectorName = gameEngine.getSectorConfig(gameEngine.currentSector.value).name,
                    kills = kills,
                    asteroidsDestroyed = ast,
                    accuracy = accuracy,
                    shipName = gameEngine.playerState.value.shipId.uppercase()
                )
            )
        }
    }

    fun selectShip(shipId: String) {
        repository.setSelectedShip(shipId)
    }

    fun unlockShip(shipInfo: ShipInfo) {
        if (repository.spendCredits(shipInfo.unlockCost)) {
            viewModelScope.launch {
                repository.saveShipUnlock(
                    ShipUnlockEntity(
                        shipId = shipInfo.id,
                        isUnlocked = true,
                        skinColorHex = "#00E5FF",
                        shieldLevel = 1,
                        engineLevel = 1,
                        weaponLevel = 1,
                        selectedPrimaryWeapon = "Plasma Repeater"
                    )
                )
                repository.setSelectedShip(shipInfo.id)
            }
        }
    }

    fun updateShipColor(shipId: String, hexColor: String) {
        viewModelScope.launch {
            val current = shipUnlocks.value.find { it.shipId == shipId }
            if (current != null) {
                repository.saveShipUnlock(current.copy(skinColorHex = hexColor))
            }
        }
    }

    fun upgradeAttribute(shipId: String, attribute: String) {
        val current = shipUnlocks.value.find { it.shipId == shipId } ?: return
        val currentLevel = when (attribute) {
            "shield" -> current.shieldLevel
            "engine" -> current.engineLevel
            else -> current.weaponLevel
        }

        val cost = currentLevel * 250
        if (repository.spendCredits(cost)) {
            viewModelScope.launch {
                val updated = when (attribute) {
                    "shield" -> current.copy(shieldLevel = current.shieldLevel + 1)
                    "engine" -> current.copy(engineLevel = current.engineLevel + 1)
                    else -> current.copy(weaponLevel = current.weaponLevel + 1)
                }
                repository.saveShipUnlock(updated)
            }
        }
    }
}
