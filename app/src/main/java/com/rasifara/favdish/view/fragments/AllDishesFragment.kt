package com.rasifara.favdish.view.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rasifara.favdish.R
import com.rasifara.favdish.application.FavDishApplication
import com.rasifara.favdish.databinding.DialogCustomListBinding
import com.rasifara.favdish.databinding.FragmentAllDishesBinding
import com.rasifara.favdish.model.entities.FavDish
import com.rasifara.favdish.utils.Constants
import com.rasifara.favdish.view.activities.AddUpdateDishActivity
import com.rasifara.favdish.view.activities.MainActivity
import com.rasifara.favdish.view.adapters.CustomListAdapter
import com.rasifara.favdish.view.adapters.FavDishAdapter
import com.rasifara.favdish.viewmodel.FavDishViewModel
import com.rasifara.favdish.viewmodel.FavDishViewModelFactory
import com.rasifara.favdish.viewmodel.HomeViewModel

class AllDishesFragment : Fragment() {

    private lateinit var binding: FragmentAllDishesBinding

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }

    private lateinit var mFavDishAdapter: FavDishAdapter

    private lateinit var mCustomListDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_all_dishes, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.action_add_dish -> {
                        startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                        return true
                    }
                    R.id.action_filter_dishes -> {
                        filterDishesListDialog()
                        return true
                    }
                }
                return true
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllDishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvDishesList.layoutManager = GridLayoutManager(requireActivity(), 2)
        mFavDishAdapter = FavDishAdapter(this@AllDishesFragment)
        binding.rvDishesList.adapter = mFavDishAdapter
        getAllDishes()
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.showBottomNavigationView()
        }
    }

    private fun getAllDishes() {
        mFavDishViewModel.allDishesList.observe(viewLifecycleOwner) { dishes: List<FavDish> ->
            dishes.let {
                for (item in dishes) {
                    if (it.isNotEmpty()) {
                        binding.rvDishesList.visibility = View.VISIBLE
                        binding.tvNoDishesAddedYet.visibility = View.GONE
                        mFavDishAdapter.dishesList(it)
                    } else {
                        binding.rvDishesList.visibility = View.GONE
                        binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun dishDetails(favDish: FavDish) {
        findNavController().navigate(
            AllDishesFragmentDirections.actionAllDishesToDishDetails(favDish)
        )
        if (requireActivity() is MainActivity) {
            (activity as MainActivity?)!!.hideBottomNavigationView()
        }
    }

    private fun filterDishesListDialog() {
        mCustomListDialog = Dialog(requireActivity())
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)

        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = resources.getString(R.string.title_select_item_to_filter)

        val dishTypes = Constants.dishTypes()
        dishTypes.add(0, Constants.ALL_ITEMS)

        binding.rvList.layoutManager = LinearLayoutManager(requireActivity())

        val adapter = CustomListAdapter(requireActivity(), this@AllDishesFragment, dishTypes, Constants.FILTER_SELECTION)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()
    }

    fun deleteDish(dish: FavDish) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.apply {
            setTitle(resources.getString(R.string.title_delete_dish))
            setMessage(resources.getString(R.string.msg_delete_dish_dialog, dish.title))
            setIcon(android.R.drawable.ic_dialog_alert)
            setPositiveButton(resources.getString(R.string.lbl_yes)) { dialogInterface, _ ->
                mFavDishViewModel.delete(dish)
                dialogInterface.dismiss()
            }
            setNegativeButton(resources.getString(R.string.lbl_no)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun filterSelection(filterSelection: String) {
        mCustomListDialog.dismiss()
        Log.i("Filter Selection", filterSelection)

        if (filterSelection == Constants.ALL_ITEMS) {
            getAllDishes()
        } else {
            mFavDishViewModel.getFilteredList(filterSelection).observe(viewLifecycleOwner){ dishes ->
                dishes.let {
                    if (it.isNotEmpty()) {
                        binding.rvDishesList.visibility = View.VISIBLE
                        binding.tvNoDishesAddedYet.visibility = View.GONE
                        mFavDishAdapter.dishesList(it)
                    } else {
                        binding.rvDishesList.visibility = View.GONE
                        binding.tvNoDishesAddedYet.visibility = View.VISIBLE
                    }
                }

            }
        }
    }

}