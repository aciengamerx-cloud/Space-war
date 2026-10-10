package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ship_unlocks")
data class ShipUnlockEntity(
    @PrimaryKey
    val shipId: String,
    val isUnlocked: Boolean,
    val skinColorHex: String,
    val shieldLevel: Int,
    val engineLevel: Int,
    val weaponLevel: Int,
    val selectedPrimaryWeapon: String
)
