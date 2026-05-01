package com.bgramma.nutrisnap.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_history")
data class FoodEntry (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val name: String,
    val calories: String,
    val time: String
)