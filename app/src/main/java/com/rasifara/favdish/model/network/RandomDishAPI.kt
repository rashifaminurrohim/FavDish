package com.rasifara.favdish.model.network

import com.rasifara.favdish.model.entities.RandomDish
import com.rasifara.favdish.utils.Constants
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomDishAPI {

    @GET(Constants.API_ENDPOINT)
    fun getRandomDish (
        @Query(Constants.API_KEY) apiKey: String,
        @Query(Constants.NUMBER) number: Int,
        @Query(Constants.INCLUDE_TAGS) includeTags: String,
        @Query(Constants.EXCLUDE_TAGS) excludeTags: String,
        @Query(Constants.INCLUDE_NUTRITION) includeNutrition: Boolean
    ): Single<RandomDish.Recipes>
}