package com.tutorial.demo.arch

import com.tutorial.demo.db.RunEntity
import com.tutorial.demo.db.RunEntityDao
import javax.inject.Inject

class RunEntityRepository @Inject constructor(val runDao: RunEntityDao) {


    suspend fun insertRun(runEntity: RunEntity) = runDao.insertRun(runEntity)


    suspend fun deleteRun(runEntity: RunEntity) = runDao.deleteRun(runEntity)


    fun getAllSortedByDate() = runDao.getAllSortedByDate()


    fun getAllSortedByAvgSpd() = runDao.getAllSortedByAvgSpd()


    fun getAllSortedByDistance() = runDao.getAllSortedByDistance()


    fun getAllSortedByTime() = runDao.getAllSortedByTime()


    fun getAllSortedByCalories() = runDao.getAllSortedByCalories()


    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()


    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()


    fun getTotalDistance() = runDao.getTotalDistance()


    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()


}