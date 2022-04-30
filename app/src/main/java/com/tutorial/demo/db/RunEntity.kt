package com.tutorial.demo.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class RunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val img: Bitmap? = null,
    val timeStamp: Long = 0L,
    val avgSpdInKmh: Float = 0f,
    val distanceInMeters: Int = 0,
    val timeInMillis: Long = 0L,
    val caloriesBurned: Int = 0
) {

}