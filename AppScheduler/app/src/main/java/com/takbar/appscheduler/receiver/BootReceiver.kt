package com.takbar.appscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.takbar.appscheduler.data.AppDatabase
import com.takbar.appscheduler.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = AppDatabase.getInstance(context).scheduleDao()
                val schedules = dao.getAllSync()
                
                schedules.filter { !it.isCompleted }.forEach { schedule ->
                    AlarmScheduler.schedule(context, schedule)
                }
            }
        }
    }
} 