package com.tutorial.demo

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tutorial.demo.arch.MainViewmodel
import com.tutorial.demo.arch.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseFragment:Fragment() {

    protected val mainActivity: MainActivity
        get() = (activity as MainActivity)

    protected val mainViewmodel : MainViewmodel by activityViewModels()
    protected val statsViewmodel : StatisticsViewModel by activityViewModels()

}