package com.tutorial.demo.others

import android.content.Context
import android.view.LayoutInflater
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.tutorial.demo.databinding.MarkerViewBinding
import com.tutorial.demo.db.RunEntity
import com.tutorial.demo.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView (
    val runs : List<RunEntity>,
    c: Context,
    layoutId:Int
        ): MarkerView(c,layoutId){

    private var _binding: MarkerViewBinding? = null
    private val binding get() = _binding!!


    init {
        _binding = MarkerViewBinding.inflate(LayoutInflater.from(c),this,true)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f, -height.toFloat())
    }


    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if (e == null){
            return
        }

        val curRunID = e.x.toInt()
        val data = runs[curRunID]


        val calender = Calendar.getInstance().apply {
            timeInMillis = data.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(calender.time)

        val avgSpeed = "${data.avgSpdInKmh}Km/h"
        binding.tvAverageSpeed.text = avgSpeed

        val distanceInKmh = "${data.distanceInMeters / 1000}Km"
        binding.tvTotalDistanceInfo.text  = distanceInKmh

        binding.tvTotalTimeInfo.text = TrackingUtility.getFormattedTimeInMillis(data.timeInMillis)

        val caloriesBurned = "${data.caloriesBurned}"
        binding.tvTotalCaloriesInfo.text = caloriesBurned
    }
}