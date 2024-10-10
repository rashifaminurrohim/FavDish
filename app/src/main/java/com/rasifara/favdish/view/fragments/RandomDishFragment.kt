package com.rasifara.favdish.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rasifara.favdish.databinding.FragmentRandomDishBinding

class RandomDishFragment : Fragment() {

    private var mBinding: FragmentRandomDishBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentRandomDishBinding.inflate(layoutInflater, container, false)
        return mBinding!!.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}