package com.bgramma.nutrisnap.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    val allHistory : Flow<List<FoodEntry>> = foodDao.getAllHistory()

    suspend fun insert(entry: FoodEntry) {
        foodDao.insert(entry)
    }

    suspend fun delete(id : Long) {
        foodDao.deleteById(id)
    }
}