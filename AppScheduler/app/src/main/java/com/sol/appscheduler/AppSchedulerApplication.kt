package com.sol.appscheduler

import android.app.Application
import com.sol.appscheduler.worker.ScheduleCheckWorker

class AppSchedulerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScheduleCheckWorker.schedule(this)
    }
} 