package com.rasifara.favdish.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rasifara.favdish.model.entities.FavDish

@Database(entities = [FavDish::class], version = 1, exportSchema = false)
abstract class FavDishRoomDatabase : RoomDatabase() {
    abstract fun favDishDao(): FavDishDao

    companion object {
        @Volatile
        private var INSTANCE: FavDishRoomDatabase? = null

        fun getDatabase(context: Context): FavDishRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavDishRoomDatabase::class.java,
                    "fav_dish_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}