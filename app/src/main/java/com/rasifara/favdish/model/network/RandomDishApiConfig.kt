package com.rasifara.favdish.model.network

import com.rasifara.favdish.model.entities.RandomDish
import com.rasifara.favdish.utils.Constants
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RandomDishApiConfig {
    private val api = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(RandomDishAPI::class.java)

    fun getRandomDish(): Single<RandomDish.Recipes>{
        return api.getRandomDish(
            Constants.API_KEY_VALUE,
            Constants.NUMBER_VALUE,
            Constants.INCLUDE_TAGS_VALUE,
            Constants.EXCLUDE_TAGS_VALUE,
            Constants.INCLUDE_NUTRITION_VALUE
        )

    }
}