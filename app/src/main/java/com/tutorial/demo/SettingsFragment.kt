package com.tutorial.demo

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tutorial.demo.databinding.FragmentSettingsBinding
import com.tutorial.demo.others.Constants.KEY_NAME
import com.tutorial.demo.others.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {
    private var _binding:FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding  = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()

        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success){
                Toast.makeText(
                    requireActivity(),
                    "Saved Changes",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                Toast.makeText(
                    requireActivity(),
                    "Please Enter All Fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyChangesToSharedPref():Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .apply()
        val toolBarText ="Let's Go ${name}"
        mainActivity.supportActionBar?.apply {
            title = toolBarText

        }
        return true
    }

    private fun loadFieldsFromSharedPref(){
        val name = sharedPref.getString(KEY_NAME,"")
        val weight = sharedPref.getFloat(KEY_WEIGHT,80f)

        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }


}