package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 50")
    fun getTopScores(): Flow<List<HighScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighScore(score: HighScoreEntity)

    @Query("SELECT MAX(score) FROM high_scores")
    fun getHighestScore(): Flow<Int?>
}
