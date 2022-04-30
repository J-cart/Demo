package com.tutorial.demo

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tutorial.demo.databinding.FragmentRunBinding
import com.tutorial.demo.others.Constants.REQUEST_CODE_LOCATION_PERMISSIONS
import com.tutorial.demo.others.RunAdapter
import com.tutorial.demo.others.SortType
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : BaseFragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!

    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        setUpAdapter()
        when(mainViewmodel.sortType){
            SortType.DATE-> binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME->binding.spFilter.setSelection(1)
            SortType.DISTANCE->binding.spFilter.setSelection(2)
            SortType.AVGSPEED->binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED->binding.spFilter.setSelection(4)
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(mAdapterView: AdapterView<*>?, mView: View?, mPos: Int, mId: Long) {
                when(mPos){
                    0->mainViewmodel.sortRuns(SortType.DATE)
                    1->mainViewmodel.sortRuns(SortType.RUNNING_TIME)
                    2->mainViewmodel.sortRuns(SortType.DISTANCE)
                    3->mainViewmodel.sortRuns(SortType.AVGSPEED)
                    4->mainViewmodel.sortRuns(SortType.CALORIES_BURNED)
                }            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}


        }

        binding.fab.setOnClickListener {
            val navigate = RunFragmentDirections.actionRunFragmentToTrackingFragment()
            findNavController().navigate(navigate)
        }

    }


    private fun setUpAdapter() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        mainViewmodel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })

    }

    private fun requestPermission() {
        if (TrackingUtility.hasLocationPermission(requireContext())) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept these permissions to use app",
                REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept these permissions to use app",
                REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}