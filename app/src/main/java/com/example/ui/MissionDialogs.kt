package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun GameOverDialog(
    score: Int,
    kills: Int,
    asteroidsDestroyed: Int,
    accuracy: Int,
    creditsEarned: Int,
    onSaveScore: (String) -> Unit,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    var playerName by remember { mutableStateOf("PILOT-1") }

    Dialog(onDismissRequest = {}) {
        Surface(
            color = Color(0xEE0A1128),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Red, RoundedCornerShape(12.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "CRITICAL HULL FAILURE",
                    color = Color.Red,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "STARSHIP VAPORIZED IN COMBAT",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Stats breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xAA1A0000), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    StatRow("FINAL SCORE", "$score", Color(0xFFFFD54F))
                    StatRow("ENEMIES DESTROYED", "$kills", Color.White)
                    StatRow("ASTEROIDS VAPORIZED", "$asteroidsDestroyed", Color.White)
                    StatRow("ACCURACY", "$accuracy%", Color(0xFF00E5FF))
                    StatRow("CREDITS EARNED", "+$creditsEarned", Color(0xFF00E676))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { if (it.length <= 12) playerName = it },
                    label = { Text("CALLSIGN / PILOT NAME", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .testTag("callsign_input")
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            onSaveScore(playerName.ifBlank { "PILOT-1" })
                            onPlayAgain()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .testTag("retry_button")
                            .weight(1f)
                    ) {
                        Text("RETRY MISSION", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSaveScore(playerName.ifBlank { "PILOT-1" })
                            onMainMenu()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .testTag("menu_button")
                            .weight(1f)
                    ) {
                        Text("MAIN MENU", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun SectorClearDialog(
    sectorName: String,
    score: Int,
    kills: Int,
    creditsEarned: Int,
    onNextSector: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            color = Color(0xEE0A1128),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "SECTOR SECURED!",
                    color = Color(0xFF00E676),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = sectorName.uppercase(),
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xAA001A0A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    StatRow("MISSION SCORE", "$score", Color(0xFFFFD54F))
                    StatRow("HOSTILES CLEARED", "$kills", Color.White)
                    StatRow("CREDITS BOUNTY", "+$creditsEarned", Color(0xFF00E676))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onNextSector,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .testTag("next_sector_button")
                            .weight(1f)
                    ) {
                        Text("NEXT SECTOR", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onMainMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .testTag("sector_clear_menu_button")
                            .weight(1f)
                    ) {
                        Text("MAIN MENU", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(label, color = Color.LightGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = valueColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
