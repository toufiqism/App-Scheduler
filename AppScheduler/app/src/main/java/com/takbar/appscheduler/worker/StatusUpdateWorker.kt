package com.takbar.appscheduler.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.takbar.appscheduler.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatusUpdateWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val scheduleId = inputData.getInt("schedule_id", -1)
            if (scheduleId == -1) return@withContext Result.failure()
            
            val dao = AppDatabase.getInstance(applicationContext).scheduleDao()
            dao.getById(scheduleId)?.let { schedule ->
                dao.update(schedule.copy(isCompleted = true))
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 