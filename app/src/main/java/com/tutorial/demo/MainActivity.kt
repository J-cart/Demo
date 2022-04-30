package com.tutorial.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.tutorial.demo.databinding.ActivityMainBinding
import com.tutorial.demo.others.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var fragHost: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fragHost = supportFragmentManager.findFragmentById(R.id.fragHost) as NavHostFragment
        navController = fragHost.findNavController()
        setSupportActionBar(binding.toolBar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.settingsFragment,
                R.id.runFragment,
                R.id.statisticsFragment,
                R.id.trackingFragment
            )
        )
        binding.bottomNav.setupWithNavController(navController)
        setupActionBarWithNavController(navController,appBarConfiguration)

        navController.addOnDestinationChangedListener{_,destination,_->
            when(destination.id){
                R.id.settingsFragment,
                R.id.runFragment,
                R.id.statisticsFragment ->  binding.bottomNav.isVisible = true
                else->  binding.bottomNav.isGone = true
            }

        }


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun navigateToTrackingFragment(intent: Intent?) {
        if (intent?.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            binding.fragHost.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}