package com.tutorial.demo.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(runEntity: RunEntity)

    @Delete
    suspend fun deleteRun(runEntity:RunEntity)

    @Query("SELECT * FROM run_table ORDER BY timeStamp DESC")
    fun getAllSortedByDate():LiveData<List<RunEntity>>

    @Query("SELECT * FROM run_table ORDER BY avgSpdInKmh DESC")
    fun getAllSortedByAvgSpd():LiveData<List<RunEntity>>

    @Query("SELECT * FROM run_table ORDER BY distanceInMeters DESC")
    fun getAllSortedByDistance():LiveData<List<RunEntity>>

    @Query("SELECT * FROM run_table ORDER BY timeInMillis DESC")
    fun getAllSortedByTime():LiveData<List<RunEntity>>

    @Query("SELECT * FROM run_table ORDER BY caloriesBurned DESC")
    fun getAllSortedByCalories():LiveData<List<RunEntity>>

    @Query("SELECT SUM(timeInMillis) FROM run_table")
    fun getTotalTimeInMillis():LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) FROM run_table")
    fun getTotalCaloriesBurned():LiveData<Int>

    @Query("SELECT SUM(distanceInMeters) FROM run_table")
    fun getTotalDistance():LiveData<Int>

    @Query("SELECT AVG(avgSpdInKmh) FROM run_table")
    fun getTotalAvgSpeed():LiveData<Long>

}