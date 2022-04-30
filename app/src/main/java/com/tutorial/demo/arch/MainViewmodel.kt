package com.tutorial.demo.arch

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorial.demo.db.RunEntity
import com.tutorial.demo.others.EventHandler
import com.tutorial.demo.others.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewmodel @Inject constructor(val runEntityRepository: RunEntityRepository) :
    ViewModel() {

    val processComplete = MutableLiveData<EventHandler<Boolean>>()
    private val itemSortedByDate = runEntityRepository.getAllSortedByDate()
    private val itemSortedByDistance = runEntityRepository.getAllSortedByDistance()
    private val itemSortedByCalories = runEntityRepository.getAllSortedByCalories()
    private val itemSortedByTimeInMillis = runEntityRepository.getAllSortedByTime()
    private val itemSortedByAvgSpd = runEntityRepository.getAllSortedByAvgSpd()


    val runs = MediatorLiveData<List<RunEntity>>()
    var sortType = SortType.DATE

    init {
        runs.addSource(itemSortedByDate) {
            if (sortType == SortType.DATE) {
                it?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(itemSortedByAvgSpd) {
            if (sortType == SortType.AVGSPEED) {
                it?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(itemSortedByDistance) {
            if (sortType == SortType.DISTANCE) {
                it?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(itemSortedByCalories) {
            if (sortType == SortType.CALORIES_BURNED) {
                it?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(itemSortedByTimeInMillis) {
            if (sortType == SortType.RUNNING_TIME) {
                it?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> itemSortedByDate.value?.let { runs.value = it }
        SortType.AVGSPEED -> itemSortedByAvgSpd.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> itemSortedByCalories.value?.let { runs.value = it }
        SortType.DISTANCE -> itemSortedByDistance.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> itemSortedByTimeInMillis.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertRun(runEntity: RunEntity) {
        viewModelScope.launch {
            runEntityRepository.insertRun(runEntity)
            processComplete.postValue(EventHandler(true))
        }
    }
}