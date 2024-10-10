package com.rasifara.favdish.model.database

import androidx.annotation.WorkerThread
import com.rasifara.favdish.model.entities.FavDish
import kotlinx.coroutines.flow.Flow

class FavDishRepository(private val favDishDao: FavDishDao) {

    @WorkerThread
    suspend fun insertFavDishData(favDish: FavDish) {
        favDishDao.insertFavDishDetails(favDish)
    }

    @WorkerThread
    suspend fun updateFavDishData(favDish: FavDish) {
        favDishDao.updateFavDishDetails(favDish)
    }

    @WorkerThread
    suspend fun deleteFavDishData(favDish: FavDish) {
        favDishDao.deleteFavDishDetails(favDish)
    }

    fun filteredDishes(filterType: String): Flow<List<FavDish>> =
        favDishDao.getFilteredDishesList(filterType)

    val allDishesList: Flow<List<FavDish>> = favDishDao.getAllDishesList()

    val favoriteDishes: Flow<List<FavDish>> = favDishDao.getFavoriteDishesList()
}