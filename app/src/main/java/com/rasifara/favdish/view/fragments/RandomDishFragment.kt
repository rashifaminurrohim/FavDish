package com.rasifara.favdish.view.fragments
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.rasifara.favdish.R
import com.rasifara.favdish.application.FavDishApplication
import com.rasifara.favdish.databinding.FragmentRandomDishBinding
import com.rasifara.favdish.model.entities.FavDish
import com.rasifara.favdish.model.entities.RandomDish
import com.rasifara.favdish.utils.Constants
import com.rasifara.favdish.viewmodel.FavDishViewModel
import com.rasifara.favdish.viewmodel.FavDishViewModelFactory
import com.rasifara.favdish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private var mBinding: FragmentRandomDishBinding? = null

    private lateinit var mRandomDishViewModel: RandomDishViewModel

    private var mProgressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRandomDishBinding.inflate(layoutInflater, container, false)
        return mBinding!!.root

    }

    private fun showCustomProgressDialog(){
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog?.let {
            it.setContentView(R.layout.dialog_custom_progress)
            it.show()
        }
    }

    private fun hideProgressDialog(){
        mProgressDialog?.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRandomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)

        mRandomDishViewModel.getRandomDishFromAPI()

        randomDishViewModelObserver()

        mBinding!!.srlRandomDish.setOnRefreshListener {
            mRandomDishViewModel.getRandomDishFromAPI()
        }
    }

    private fun randomDishViewModelObserver() {
        mRandomDishViewModel.randomDishResponse.observe(viewLifecycleOwner) { randomDishResponse ->
            randomDishResponse?.let {
                Log.i("RandomDishResponse", "${randomDishResponse.recipes[0]}")
                if (mBinding!!.srlRandomDish.isRefreshing) {
                    mBinding!!.srlRandomDish.isRefreshing = false
                }
                if (randomDishResponse.recipes.isNotEmpty()){

                    setRandomDishResponseInUI(randomDishResponse.recipes[0])
                }else {
                    Toast.makeText(requireActivity(), "No recipes found", Toast.LENGTH_SHORT).show()
                }
            }
        }
        mRandomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner) { dataError ->
            dataError?.let {
                Log.e("Random Dish Error", dataError.toString())
                Toast.makeText(requireActivity(), "Error: $dataError", Toast.LENGTH_LONG).show() // Menampilkan pesan error ke UI
                if (mBinding!!.srlRandomDish.isRefreshing) {
                    mBinding!!.srlRandomDish.isRefreshing = false
                }
            }
        }
        mRandomDishViewModel.loadRandomDish.observe(viewLifecycleOwner) { loadRandomDish ->
            loadRandomDish?.let {
                Log.i("Random Dish Loading", "$loadRandomDish")
                if (loadRandomDish && !mBinding!!.srlRandomDish.isRefreshing){
                    showCustomProgressDialog()
                }else{
                    hideProgressDialog()
                }
            }
        }
    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {

        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(mBinding!!.ivDishImage)

        mBinding!!.tvTitle.text = recipe.title

        var dishType: String = "other"

        if (recipe.dishTypes.isNotEmpty()) {
            dishType = recipe.dishTypes[0]
            mBinding!!.tvType.text = dishType
        }

        mBinding!!.tvCategory.text = "Other"

        var ingredients = ""
        for (value in recipe.extendedIngredients) {

            if (ingredients.isEmpty()) {
                ingredients = value.original
            } else {
                ingredients = ingredients + ", \n" + value.original
            }
        }

        mBinding!!.tvIngredients.text = ingredients

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mBinding!!.tvCookingDirection.text = Html.fromHtml(
                recipe.instructions,
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            @Suppress("DEPRECATION")
            mBinding!!.tvCookingDirection.text = Html.fromHtml(recipe.instructions)
        }

        mBinding!!.ivFavoriteDish.setImageDrawable(
            ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.ic_favorite_unselected
            )
        )

        var addedToFavourites = false


        mBinding!!.tvCookingTime.text =
            resources.getString(
                R.string.lbl_estimate_cooking_time,
                recipe.readyInMinutes.toString()
            )

        mBinding!!.ivFavoriteDish.setOnClickListener {

            if (addedToFavourites) {
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_already_added_to_favourites),
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val randomDishDetails = FavDish(
                    recipe.image,
                    Constants.DISH_IMAGE_SOURCE_ONLINE,
                    recipe.title,
                    dishType,
                    "Other",
                    ingredients,
                    recipe.readyInMinutes.toString(),
                    recipe.instructions,
                    true
                )

                val mFavDishViewModel: FavDishViewModel by viewModels {
                    FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
                }

                mFavDishViewModel.insert(randomDishDetails)

                addedToFavourites = true

                mBinding!!.ivFavoriteDish.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_favorite_selected
                    )
                )
                Toast.makeText(
                    requireActivity(),
                    resources.getString(R.string.msg_added_to_favourites),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}