package com.tutorial.demo

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.tutorial.demo.databinding.FragmentSetupBinding
import com.tutorial.demo.others.Constants.KEY_FIRST_TIME_TOGGLE
import com.tutorial.demo.others.Constants.KEY_NAME
import com.tutorial.demo.others.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : BaseFragment() {

    private var _binding:FragmentSetupBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstRun = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        mainActivity.binding.bottomNav.isGone = true
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstRun) {

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        binding.tvContinue.setOnClickListener {
            val success = dataToSharedPref()
            if (success) {
                val navigate = SetupFragmentDirections.actionSetupFragmentToRunFragment()
                findNavController().navigate(navigate)
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Please Enter All Fields",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }


    fun dataToSharedPref(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }

        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        val toolbarText = "Let's go ${name}!"
        mainActivity.supportActionBar?.apply {
            title = toolbarText
        }
        return true
    }



}