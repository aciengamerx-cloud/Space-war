package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.EnemyEntity
import com.example.game.engine.GameEngine
import com.example.game.engine.PlayerState

@Composable
fun SciFiHUDOverlay(
    gameEngine: GameEngine,
    playerState: PlayerState,
    targetEnemy: EnemyEntity?,
    score: Int,
    sectorName: String,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // 1. TOP HEADER (Ship Designation & Sector Status)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Title Callout
                Column {
                    Text(
                        text = "STARSTREAK-1",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "COM-LINK: GREEN",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Center Sector & Score Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = sectorName.uppercase(),
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "SCORE: $score",
                        color = Color(0xFFFFD54F),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Pause Button
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .testTag("pause_button")
                        .size(42.dp)
                        .background(Color(0xBB0A1128), CircleShape)
                        .border(1.dp, Color(0xFF00E5FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }

            // Boss Health Bar if Active
            val boss = gameEngine.enemies.find { it.isBoss }
            if (boss != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .background(Color(0xCC1A0000), RoundedCornerShape(6.dp))
                        .border(1.dp, Color.Red, RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = boss.name.uppercase(),
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (boss.health / boss.maxHealth).coerceIn(0f, 1f) },
                        color = Color.Red,
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // 2. LEFT WEAPONS HUD BOX
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 20.dp)
                .background(Color(0xAA0A1128), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(10.dp)
                .width(220.dp)
        ) {
            Column {
                Text(
                    text = "WEAPONS",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Primary: Plasma Repeater [active]",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (playerState.isOverheated) "OVERHEAT!" else "HEAT ${playerState.heat.toInt()}%",
                        color = if (playerState.isOverheated) Color.Red else Color.LightGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { (playerState.heat / 100f).coerceIn(0f, 1f) },
                        color = if (playerState.isOverheated) Color.Red else Color(0xFFFF9100),
                        trackColor = Color(0xFF263238),
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Secondary: Homing Missiles - ${playerState.missileCount}/${playerState.maxMissiles}",
                    color = Color(0xFFFFB74D),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 3. RIGHT POWER MANAGEMENT HUD BOX
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 20.dp)
                .background(Color(0xAA0A1128), RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(10.dp)
                .width(220.dp)
        ) {
            Column {
                Text(
                    text = "POWER MANAGEMENT",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Power Row Weapons
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("WEAPONS [1]", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${(playerState.weaponPower * 100).toInt()}%", color = Color(0xFFFF5252), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                LinearProgressIndicator(
                    progress = { playerState.weaponPower },
                    color = Color(0xFFFF5252),
                    trackColor = Color(0xFF263238),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                // Power Row Shields
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SHIELDS [2]", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${(playerState.shieldPower * 100).toInt()}%", color = Color(0xFF00E5FF), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                LinearProgressIndicator(
                    progress = { playerState.shieldPower },
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF263238),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                // Power Row Engines
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ENGINES [3]", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("${(playerState.enginePower * 100).toInt()}%", color = Color(0xFFFFD54F), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                LinearProgressIndicator(
                    progress = { playerState.enginePower },
                    color = Color(0xFFFFD54F),
                    trackColor = Color(0xFF263238),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))
                // Power Distribution Quick Toggles
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Color(0xFF37474F), RoundedCornerShape(4.dp))
                            .clickable { gameEngine.setPowerAllocation(0.6f, 0.2f, 0.2f) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("WEAP", color = Color.White, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Color(0xFF37474F), RoundedCornerShape(4.dp))
                            .clickable { gameEngine.setPowerAllocation(0.2f, 0.6f, 0.2f) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("SHLD", color = Color.White, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(Color(0xFF37474F), RoundedCornerShape(4.dp))
                            .clickable { gameEngine.setPowerAllocation(0.2f, 0.2f, 0.6f) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ENG", color = Color.White, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 4. RIGHT TARGET HUD BOX (If enemy locked)
        if (targetEnemy != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp)
                    .background(Color(0xCC1A0808), RoundedCornerShape(6.dp))
                    .border(1.dp, Color.Red.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
                    .width(180.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TARGET / Z",
                            color = Color.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${targetEnemy.pos.distanceTo(playerState.pos).toInt()}m",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = targetEnemy.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SHIELDS", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(4.dp))
                        LinearProgressIndicator(
                            progress = { (targetEnemy.shield / targetEnemy.maxShield).coerceIn(0f, 1f) },
                            color = Color(0xFF00E5FF),
                            trackColor = Color.DarkGray,
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("HULL", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(12.dp))
                        LinearProgressIndicator(
                            progress = { (targetEnemy.health / targetEnemy.maxHealth).coerceIn(0f, 1f) },
                            color = Color.Red,
                            trackColor = Color.DarkGray,
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                        )
                    }
                }
            }
        }

        // 5. BOTTOM-LEFT ARC METER (SHIELD & HULL)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 10f
                // Shield Arc Meter
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 135f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    size = Size(size.width - strokeW, size.height - strokeW),
                    topLeft = Offset(strokeW / 2, strokeW / 2)
                )
                drawArc(
                    color = Color(0xFF00E5FF),
                    startAngle = 135f,
                    sweepAngle = 180f * (playerState.shield / playerState.maxShield).coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    size = Size(size.width - strokeW, size.height - strokeW),
                    topLeft = Offset(strokeW / 2, strokeW / 2)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "SHIELD",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${playerState.shield.toInt()}%",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("HULL", color = Color.LightGray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    LinearProgressIndicator(
                        progress = { (playerState.hull / playerState.maxHull).coerceIn(0f, 1f) },
                        color = Color(0xFF00E5FF),
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .width(50.dp)
                            .height(4.dp)
                    )
                }
            }
        }

        // 6. BOTTOM-RIGHT ARC METER (ENERGY & VELOCITY)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 10f
                // Energy Arc Meter
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 225f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    size = Size(size.width - strokeW, size.height - strokeW),
                    topLeft = Offset(strokeW / 2, strokeW / 2)
                )
                drawArc(
                    color = Color(0xFFFF9100),
                    startAngle = 225f,
                    sweepAngle = 180f * (playerState.energy / playerState.maxEnergy).coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                    size = Size(size.width - strokeW, size.height - strokeW),
                    topLeft = Offset(strokeW / 2, strokeW / 2)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "ENERGY",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${playerState.energy.toInt()}%",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "VELOCITY ${playerState.velocity.toInt()} m/s",
                    color = Color(0xFFFFD54F),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 7. CENTER TARGETING RETICLE
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2
                val reticleColor = if (targetEnemy != null) Color.Red else Color(0xFF00E5FF).copy(alpha = 0.6f)

                // Reticle Outer Ring
                drawCircle(
                    color = reticleColor,
                    radius = radius,
                    style = Stroke(width = 1.5f)
                )
                // Crosshairs
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x - radius - 10f, center.y),
                    end = Offset(center.x - radius + 10f, center.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x + radius - 10f, center.y),
                    end = Offset(center.x + radius + 10f, center.y),
                    strokeWidth = 2f
                )
                // Center Dot
                drawCircle(color = reticleColor, radius = 3f, center = center)
            }
        }

        // 8. ON-SCREEN ACTION CONTROLS (FIRE BUTTON & MISSILE BUTTON ON LEFT / LOWER LEFT)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 140.dp, bottom = 10.dp)
        ) {
            // FIRE PLASMA BUTTON (Press and Hold)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .testTag("fire_button")
                    .size(70.dp)
                    .background(
                        if (playerState.isOverheated) Color.DarkGray else Color(0xFFFF1744),
                        CircleShape
                    )
                    .border(2.dp, Color.White, CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                gameEngine.setPrimaryFiring(true)
                                tryAwaitRelease()
                                gameEngine.setPrimaryFiring(false)
                            }
                        )
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Fire Plasma",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "FIRE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // HOMING MISSILE BUTTON
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .testTag("missile_button")
                    .size(56.dp)
                    .background(Color(0xFFFF9100), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { gameEngine.fireHomingMissile() }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Rocket,
                        contentDescription = "Homing Missile",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "MISSILE",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
