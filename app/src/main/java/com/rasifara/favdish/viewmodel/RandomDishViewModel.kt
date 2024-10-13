package com.rasifara.favdish.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rasifara.favdish.model.entities.RandomDish
import com.rasifara.favdish.model.network.RandomDishApiConfig
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class RandomDishViewModel : ViewModel() {
    private val randomDishApiConfig = RandomDishApiConfig()
    private val compositeDisposable = CompositeDisposable()

    val loadRandomDish : MutableLiveData<Boolean> = MutableLiveData()
    val randomDishResponse : MutableLiveData<RandomDish.Recipes> = MutableLiveData()
    val randomDishLoadingError : MutableLiveData<Boolean> = MutableLiveData()

    fun getRandomDishFromAPI(){
        loadRandomDish.value = true

        compositeDisposable.add(
            randomDishApiConfig.getRandomDish()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object: DisposableSingleObserver<RandomDish.Recipes>(){
                    override fun onSuccess(value: RandomDish.Recipes) {
                       loadRandomDish.value = false
                        randomDishResponse.value = value
                        randomDishLoadingError.value = false
                    }

                    override fun onError(e: Throwable) {
                        loadRandomDish.value = false
                        randomDishLoadingError.value = true
                        e.printStackTrace()
                    }

                })
        )
    }

}