package com.tutorial.demo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tutorial.demo.databinding.FragmentTrackingBinding
import com.tutorial.demo.db.RunEntity
import com.tutorial.demo.others.Constants.ACTION_PAUSE_SERVICE
import com.tutorial.demo.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.tutorial.demo.others.Constants.ACTION_STOP_SERVICE
import com.tutorial.demo.others.Constants.KEY_NAME
import com.tutorial.demo.others.Constants.MAP_ZOOM
import com.tutorial.demo.others.Constants.POLYLINE_COLOR
import com.tutorial.demo.others.Constants.POLYLINE_WIDTH
import com.tutorial.demo.others.TrackingService
import com.tutorial.demo.others.polyline
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : BaseFragment() {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<polyline>()

    private var curTimeInMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    @Inject
    lateinit var sharedPref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        setHasOptionsMenu(true)
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getToolBarName() {
        val name = sharedPref.getString(KEY_NAME, "")
        mainActivity.supportActionBar?.apply {
            title = "Let's Go ${name}!"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        getToolBarName()
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        binding.btnFinishRun.setOnClickListener {
            if (curTimeInMillis > 0) {
                zoomwholeTrack()
                endAndSaveRun()
                mainViewmodel.processComplete.observe(viewLifecycleOwner) { completed ->
                    completed.getContentIfNotHandledOrNull()?.let {
                        android.widget.Toast.makeText(
                            requireActivity(),
                            "Item saved",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            return@setOnClickListener

        }


        binding.btnToggleRun.setOnClickListener {
//            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            toggleRun()
        }
        observeFromService()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.cancel_tracking, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        this.menu?.getItem(0)?.isVisible = curTimeInMillis > 0L
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancelTracking -> cancelDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cancelDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure you want to cancel the current run?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { DialogInterface, Int ->
                stopRun()
            }
            .setNegativeButton("No") { DialogInterface, Int ->
                DialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        val navigate = TrackingFragmentDirections.actionTrackingFragmentToRunFragment()
        findNavController().navigate(navigate)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }


    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()

            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polyLineOptions)
        }
    }

    private fun addAllPolylines() {

        // in case of layout orientation change..mapview also changes but the data doesn't cuz it is observed from a livedata...so we add all the points from the livedata
        for (polyline in pathPoints) {
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLineOptions)

        }
    }

    private fun moveCamera() {
        // zooms camera to the latest position as the tracking starts
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )

//            map?.addMarker(MarkerOptions().position(pathPoints.last().last()).title("USER"))
        }
    }

    private fun zoomwholeTrack() {
        val bounds = LatLngBounds.builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endAndSaveRun() {

        map?.snapshot { bmp ->
            var distanceInMetres = 0
            for (polyline in pathPoints) {
                distanceInMetres += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val avgSpeed =
                round((distanceInMetres / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMetres / 1000f) * weight).toInt()

            val run = RunEntity(
                img = bmp,
                timeStamp = dateTimeStamp,
                avgSpdInKmh = avgSpeed,
                distanceInMeters = distanceInMetres,
                timeInMillis = curTimeInMillis,
                caloriesBurned = caloriesBurned
            )

            mainViewmodel.insertRun(run)

            stopRun()
        }

    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else {
            menu?.getItem(0)?.isVisible = true
            binding.btnToggleRun.text = "Stop"
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun observeFromService() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLatestPolyline()
            moveCamera()
        })

        TrackingService.timeInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedTimeInMillis(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        })
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }


}