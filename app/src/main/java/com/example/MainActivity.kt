package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.engine.GameStateMode
import com.example.ui.AppScreen
import com.example.ui.GameCanvasView
import com.example.ui.GameOverDialog
import com.example.ui.GameViewModel
import com.example.ui.HangarScreen
import com.example.ui.LeaderboardScreen
import com.example.ui.MainMenuScreen
import com.example.ui.SciFiHUDOverlay
import com.example.ui.SectorClearDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StarstreakApp()
                }
            }
        }
    }
}

@Composable
fun StarstreakApp(viewModel: GameViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val playerState by viewModel.gameEngine.playerState.collectAsState()
    val gameStateMode by viewModel.gameEngine.gameStateMode.collectAsState()
    val score by viewModel.gameEngine.score.collectAsState()
    val kills by viewModel.gameEngine.kills.collectAsState()
    val asteroids by viewModel.gameEngine.asteroidsDestroyed.collectAsState()
    val shotsFired by viewModel.gameEngine.shotsFired.collectAsState()
    val shotsHit by viewModel.gameEngine.shotsHit.collectAsState()
    val targetEnemy by viewModel.gameEngine.targetEnemy.collectAsState()
    val currentSector by viewModel.gameEngine.currentSector.collectAsState()

    val credits by viewModel.credits.collectAsState()
    val selectedShipId by viewModel.selectedShipId.collectAsState()
    val shipUnlocks by viewModel.shipUnlocks.collectAsState()
    val highScores by viewModel.highScores.collectAsState()

    var isPaused by remember { mutableStateOf(false) }

    // System Back Navigation
    BackHandler(enabled = currentScreen != AppScreen.MAIN_MENU) {
        if (currentScreen == AppScreen.PLAYING_GAME) {
            isPaused = true
        } else {
            viewModel.navigateTo(AppScreen.MAIN_MENU)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.MAIN_MENU -> {
                MainMenuScreen(
                    credits = credits,
                    selectedShipName = selectedShipId,
                    onLaunchCampaign = { viewModel.startCampaign() },
                    onOpenHangar = { viewModel.navigateTo(AppScreen.HANGAR_WORKSHOP) },
                    onOpenLeaderboard = { viewModel.navigateTo(AppScreen.LEADERBOARD) }
                )
            }

            AppScreen.HANGAR_WORKSHOP -> {
                HangarScreen(
                    credits = credits,
                    shipUnlocks = shipUnlocks,
                    selectedShipId = selectedShipId,
                    onSelectShip = { viewModel.selectShip(it) },
                    onUnlockShip = { viewModel.unlockShip(it) },
                    onUpdateShipColor = { id, color -> viewModel.updateShipColor(id, color) },
                    onUpgradeAttribute = { id, attr -> viewModel.upgradeAttribute(id, attr) },
                    onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                )
            }

            AppScreen.LEADERBOARD -> {
                LeaderboardScreen(
                    highScores = highScores,
                    onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                )
            }

            AppScreen.PLAYING_GAME -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    // 1. 3D Game Canvas Engine
                    GameCanvasView(
                        gameEngine = viewModel.gameEngine,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 2. Sci-Fi HUD Overlay
                    SciFiHUDOverlay(
                        gameEngine = viewModel.gameEngine,
                        playerState = playerState,
                        targetEnemy = targetEnemy,
                        score = score,
                        sectorName = viewModel.gameEngine.getSectorConfig(currentSector).name,
                        onPauseClick = { isPaused = true }
                    )

                    // 3. Pause Dialog
                    if (isPaused) {
                        Dialog(onDismissRequest = { isPaused = false }) {
                            Surface(
                                color = Color(0xEE0A1128),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Text(
                                        text = "MISSION PAUSED",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    val flightSens by viewModel.gameEngine.flightSensitivity.collectAsState()
                                    Text(
                                        text = "FLIGHT SENSITIVITY: ${(flightSens * 100).toInt()}%",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    androidx.compose.material3.Slider(
                                        value = flightSens,
                                        onValueChange = { viewModel.gameEngine.setFlightSensitivity(it) },
                                        valueRange = 0.05f..1.0f,
                                        colors = androidx.compose.material3.SliderDefaults.colors(
                                            thumbColor = Color(0xFF00E5FF),
                                            activeTrackColor = Color(0xFF00E5FF)
                                        ),
                                        modifier = Modifier
                                            .testTag("sensitivity_slider")
                                            .padding(horizontal = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { isPaused = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                        modifier = Modifier
                                            .testTag("resume_button")
                                            .fillMaxWidth(0.9f)
                                    ) {
                                        Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            isPaused = false
                                            viewModel.navigateTo(AppScreen.MAIN_MENU)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                                        modifier = Modifier
                                            .testTag("abort_button")
                                            .fillMaxWidth(0.9f)
                                    ) {
                                        Text("ABORT MISSION", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }

                    // 4. Game Over Dialog
                    if (gameStateMode == GameStateMode.GAME_OVER) {
                        val accuracy = if (shotsFired > 0) ((shotsHit.toFloat() / shotsFired) * 100).toInt() else 0
                        val earnedCredits = (score / 10) + (kills * 50) + (asteroids * 20)

                        GameOverDialog(
                            score = score,
                            kills = kills,
                            asteroidsDestroyed = asteroids,
                            accuracy = accuracy,
                            creditsEarned = earnedCredits,
                            onSaveScore = { callsign -> viewModel.saveCurrentFlightScore(callsign) },
                            onPlayAgain = { viewModel.startCampaign() },
                            onMainMenu = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                        )
                    }

                    // 5. Sector Clear Dialog
                    if (gameStateMode == GameStateMode.SECTOR_CLEAR) {
                        val earnedCredits = (score / 10) + (kills * 50) + (asteroids * 20)

                        SectorClearDialog(
                            sectorName = viewModel.gameEngine.getSectorConfig(currentSector).name,
                            score = score,
                            kills = kills,
                            creditsEarned = earnedCredits,
                            onNextSector = { viewModel.gameEngine.loadNextSector() },
                            onMainMenu = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
                        )
                    }
                }
            }
        }
    }
}
