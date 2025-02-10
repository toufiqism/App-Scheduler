package com.takbar.appscheduler.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takbar.appscheduler.data.Schedule
import com.takbar.appscheduler.data.ScheduleDao
import kotlinx.coroutines.launch

/**
 * ViewModel for managing schedule data
 * @property scheduleDao Data access object for schedule operations
 */
class ScheduleViewModel(private val scheduleDao: ScheduleDao) : ViewModel() {
    // Observable Flow of all schedules
    val schedules = scheduleDao.getAll()

    fun addSchedule(schedule: Schedule) = viewModelScope.launch {
        scheduleDao.insert(schedule)
    }

    fun updateSchedule(schedule: Schedule) = viewModelScope.launch {
        scheduleDao.update(schedule)
    }

    fun deleteSchedule(schedule: Schedule) = viewModelScope.launch {
        scheduleDao.delete(schedule)
    }
} 