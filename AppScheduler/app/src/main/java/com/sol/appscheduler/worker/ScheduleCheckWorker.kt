package com.sol.appscheduler.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.sol.appscheduler.data.AppDatabase
import com.sol.appscheduler.util.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ScheduleCheckWorker(context: Context, params: WorkerParameters) : 
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getInstance(applicationContext).scheduleDao()
                val schedules = dao.getAllSync()
                
                schedules.filter { !it.isCompleted }.forEach { schedule ->
                    AlarmScheduler.schedule(applicationContext, schedule)
                }
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ScheduleCheckWorker>(
                15, // Every 15 minutes
                TimeUnit.MINUTES
            ).build()
            
            androidx.work.WorkManager.getInstance(context).enqueue(request)
        }
    }
} 