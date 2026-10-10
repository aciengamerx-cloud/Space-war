package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShipUnlockEntity

data class ShipInfo(
    val id: String,
    val name: String,
    val description: String,
    val unlockCost: Int,
    val baseShield: Int,
    val baseEngine: Int,
    val baseWeapon: Int
)

val STARSHIP_CATALOG = listOf(
    ShipInfo("starstreak_mk1", "STARSTREAK MK-I", "Balanced fast interceptor with plasma repeater.", 0, 3, 3, 3),
    ShipInfo("vanguard_phoenix", "VANGUARD PHOENIX", "Heavy armor cruiser with reinforced shields.", 800, 5, 2, 4),
    ShipInfo("aether_wing", "AETHER WING", "Ultra-agile scout ship with rapid energy regen.", 1200, 2, 5, 3),
    ShipInfo("nemesis_dread", "NEMESIS DESTROYER", "Heavy dread-fighter equipped with quantum cannons.", 2000, 4, 3, 5)
)

val COLOR_SCHEMES = listOf(
    "#00E5FF" to "NEON CYAN",
    "#FF5252" to "DEEP CRIMSON",
    "#7C4DFF" to "AETHER PURPLE",
    "#FFD700" to "TITANIUM GOLD",
    "#00E676" to "EMERALD FLAME"
)

@Composable
fun HangarScreen(
    credits: Int,
    shipUnlocks: List<ShipUnlockEntity>,
    selectedShipId: String,
    onSelectShip: (String) -> Unit,
    onUnlockShip: (ShipInfo) -> Unit,
    onUpdateShipColor: (String, String) -> Unit,
    onUpgradeAttribute: (String, String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeShipId by remember { mutableStateOf(selectedShipId) }
    val currentShipInfo = STARSHIP_CATALOG.find { it.id == activeShipId } ?: STARSHIP_CATALOG.first()
    val currentUnlock = shipUnlocks.find { it.shipId == activeShipId } ?: ShipUnlockEntity(
        shipId = activeShipId,
        isUnlocked = activeShipId == "starstreak_mk1",
        skinColorHex = "#00E5FF",
        shieldLevel = 1,
        engineLevel = 1,
        weaponLevel = 1,
        selectedPrimaryWeapon = "Plasma Repeater"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF030712),
                        Color(0xFF0B1426),
                        Color(0xFF0F1A30)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("back_button")
                        .background(Color(0xAA0A1128), CircleShape)
                        .border(1.dp, Color(0xFF00E5FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "STARSHIP WORKSHOP",
                    color = Color(0xFF00E5FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            // Credits Balance Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$credits CREDITS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Main Hangar Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp)
        ) {
            // Left - Starship Selector List
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.38f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = "SELECT HULL FRAME",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))

                STARSHIP_CATALOG.forEach { ship ->
                    val unlock = shipUnlocks.find { it.shipId == ship.id }
                    val isUnlocked = unlock?.isUnlocked == true || ship.id == "starstreak_mk1"
                    val isSelected = ship.id == activeShipId

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF0288D1).copy(alpha = 0.4f) else Color(0xAA0A1128)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF00E5FF) else Color.DarkGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { activeShipId = ship.id }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = ship.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isUnlocked) "UNLOCKED" else "COST: ${ship.unlockCost} CREDITS",
                                    color = if (isUnlocked) Color(0xFF00E676) else Color(0xFFFFD54F),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Right - Ship Attributes & Paint Bay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = currentShipInfo.name,
                    color = Color(0xFF00E5FF),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = currentShipInfo.description,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (!currentUnlock.isUnlocked) {
                    // Unlock Ship Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xCC1A0808)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "STARSHIP LOCKED",
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onUnlockShip(currentShipInfo) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                                enabled = credits >= currentShipInfo.unlockCost
                            ) {
                                Text(
                                    text = "UNLOCK FOR ${currentShipInfo.unlockCost} CREDITS",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                } else {
                    // Equipped Status Button
                    if (selectedShipId == activeShipId) {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .testTag("equipped_button")
                                .padding(bottom = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ACTIVE SHIP EQUIPPED", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Button(
                            onClick = { onSelectShip(activeShipId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .testTag("equip_button")
                                .padding(bottom = 12.dp)
                        ) {
                            Text("EQUIP THIS STARSHIP", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Paint Color Scheme Chooser
                    Text(
                        text = "CUSTOM PAINT FINISH",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(modifier = Modifier.padding(bottom = 16.dp)) {
                        items(COLOR_SCHEMES) { (hex, name) ->
                            val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Cyan }
                            val isSelected = currentUnlock.skinColorHex == hex
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(36.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.DarkGray,
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdateShipColor(activeShipId, hex) }
                            ) {}
                        }
                    }

                    // Attribute Upgrades
                    Column {
                        Text(
                            text = "SYSTEM UPGRADES",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Upgrade Shield
                        UpgradeRow(
                            label = "SHIELD CAPACITY",
                            level = currentUnlock.shieldLevel,
                            cost = currentUnlock.shieldLevel * 250,
                            credits = credits,
                            onUpgrade = { onUpgradeAttribute(activeShipId, "shield") }
                        )

                        // Upgrade Engine
                        UpgradeRow(
                            label = "ENGINE THRUSTERS",
                            level = currentUnlock.engineLevel,
                            cost = currentUnlock.engineLevel * 250,
                            credits = credits,
                            onUpgrade = { onUpgradeAttribute(activeShipId, "engine") }
                        )

                        // Upgrade Weapons
                        UpgradeRow(
                            label = "WEAPON COOLING",
                            level = currentUnlock.weaponLevel,
                            cost = currentUnlock.weaponLevel * 250,
                            credits = credits,
                            onUpgrade = { onUpgradeAttribute(activeShipId, "weapon") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpgradeRow(
    label: String,
    level: Int,
    cost: Int,
    credits: Int,
    onUpgrade: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0x880A1128), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            LinearProgressIndicator(
                progress = { level / 5f },
                color = Color(0xFF00E5FF),
                trackColor = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
            )
        }
        if (level < 5) {
            Button(
                onClick = onUpgrade,
                enabled = credits >= cost,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "UPGRADE ($cost)",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Text("MAXED", color = Color(0xFF00E676), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
