package com.takbar.appscheduler

import android.app.Application
import com.takbar.appscheduler.worker.ScheduleCheckWorker

class AppSchedulerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScheduleCheckWorker.schedule(this)
    }
} 