package com.tutorial.demo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.tutorial.demo.databinding.FragmentStatisticsBinding
import com.tutorial.demo.others.CustomMarkerView
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : BaseFragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFromViewModel()
        setUpBArChart()
    }

    private fun setUpBArChart(){
        binding.barChart?.xAxis?.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)

        }
        binding.barChart?.axisRight?.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)

        }
        binding.barChart?.axisLeft?.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)

        }

        binding.barChart?.apply {
            description.text = "Average Speed over Time"
            legend.isEnabled = false

        }
    }

    private fun observeFromViewModel() {
        statsViewmodel.totalTimeInMillis.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedTimeInMillis(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        })

        statsViewmodel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceText = "${totalDistance}Km"

                binding.tvTotalDistance.text = totalDistanceText
            }
        })
        statsViewmodel.totalAvgSpd.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpd = round(it * 10f) / 10f
                val avgSpdString = "${avgSpd}Km/h"

                binding.tvAverageSpeed.text = avgSpdString
            }
        })
        statsViewmodel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}KCal"
                binding.tvTotalCalories.text = totalCalories
            }
        })

        statsViewmodel.itemsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = it.indices.map { i-> BarEntry(i.toFloat(), it[i].avgSpdInKmh) }
                val barDataSet = BarDataSet(avgSpeed,"Average Speed Over Time").apply {
                    valueTextColor =  Color.WHITE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }

                binding.barChart?.data = BarData(barDataSet)
                binding.barChart?.marker = CustomMarkerView(it.reversed(), requireContext(),R.layout.marker_view)
                binding.barChart?.invalidate()
            }



        })

    }


}