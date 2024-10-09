package com.rasifara.favdish.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rasifara.favdish.application.FavDishApplication
import com.rasifara.favdish.databinding.FragmentFavouriteDishesBinding
import com.rasifara.favdish.model.entities.FavDish
import com.rasifara.favdish.view.activities.MainActivity
import com.rasifara.favdish.view.adapters.FavDishAdapter
import com.rasifara.favdish.viewmodel.DashboardViewModel
import com.rasifara.favdish.viewmodel.FavDishViewModel
import com.rasifara.favdish.viewmodel.FavDishViewModelFactory

class FavouriteDishesFragment : Fragment() {

    private var _binding: FragmentFavouriteDishesBinding? = null
    private val binding get() = _binding!!

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteDishesBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    fun dishDetails(favDish: FavDish) {
        findNavController().navigate(
            FavouriteDishesFragmentDirections.actionFavouriteDishesToDishDetails(favDish)
        )
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.hideBottomNavigationView()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvFavoriteDishesList.layoutManager = GridLayoutManager(requireActivity(), 2)
        val favDishAdapter = FavDishAdapter(this@FavouriteDishesFragment)
        binding.rvFavoriteDishesList.adapter = favDishAdapter
        mFavDishViewModel.favoriteDishes.observe(viewLifecycleOwner) { dishes ->
            dishes.let {
                for (item in dishes) {
                    if (it.isNotEmpty()) {
                        binding.rvFavoriteDishesList.visibility = View.VISIBLE
                        binding.tvNoFavoriteDishesAvailable.visibility = View.GONE
                        favDishAdapter.dishesList(it)
                    } else {
                        binding.rvFavoriteDishesList.visibility = View.GONE
                        binding.tvNoFavoriteDishesAvailable.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}