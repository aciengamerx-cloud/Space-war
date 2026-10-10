package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerName: String,
    val score: Int,
    val sectorName: String,
    val kills: Int,
    val asteroidsDestroyed: Int,
    val accuracy: Int,
    val shipName: String,
    val timestamp: Long = System.currentTimeMillis()
)
