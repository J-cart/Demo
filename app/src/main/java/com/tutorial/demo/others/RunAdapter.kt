package com.tutorial.demo.others

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutorial.demo.R
import com.tutorial.demo.databinding.ItemRunBinding
import com.tutorial.demo.TrackingUtility
import com.tutorial.demo.db.RunEntity
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemRunBinding.bind(view)

        fun bind(data: RunEntity) {
            Glide.with(itemView).load(data.img).into(binding.ivRunImage)

            val calender = Calendar.getInstance().apply {
                timeInMillis = data.timeStamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calender.time)

            val avgSpeed = "${data.avgSpdInKmh}Km/h"
            binding.tvAvgSpeed.text = avgSpeed

            val distanceInKmh = "${data.distanceInMeters / 1000}Km"
            binding.tvDistance.text = distanceInKmh

            binding.tvTime.text = TrackingUtility.getFormattedTimeInMillis(data.timeInMillis)

            val caloriesBurned = "${data.caloriesBurned}"
            binding.tvCalories.text = caloriesBurned
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_run, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = differ.currentList[position]
        holder.bind(pos)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val diffCallback = object : DiffUtil.ItemCallback<RunEntity>() {
        override fun areItemsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<RunEntity>) = differ.submitList(list)
}