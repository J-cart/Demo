package com.tutorial.demo.arch

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(val runEntityRepository: RunEntityRepository) :ViewModel() {

    val totalTimeInMillis = runEntityRepository.getTotalTimeInMillis()
    val totalDistance = runEntityRepository.getTotalDistance()
    val totalAvgSpd = runEntityRepository.getTotalAvgSpeed()
    val totalCaloriesBurned = runEntityRepository.getTotalCaloriesBurned()

    val itemsSortedByDate = runEntityRepository.getAllSortedByDate()
}