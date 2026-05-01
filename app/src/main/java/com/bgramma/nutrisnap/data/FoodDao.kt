package com.bgramma.nutrisnap.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM food_history ORDER BY id DESC")
    fun getAllHistory() : Flow<List<FoodEntry>>

    @Insert
    suspend fun insert(entry: FoodEntry) : Long

    @Query("DELETE FROM food_history WHERE id = :id")
    suspend fun deleteById(id : Long) : Int
}