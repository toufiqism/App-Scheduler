package com.takbar.appscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.takbar.appscheduler.data.AppDatabase
import com.takbar.appscheduler.data.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling alarm triggers
 * Launches the target app and updates schedule status
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra("package_name") ?: return
        val scheduleId = intent.getIntExtra("schedule_id", -1)

        // Check schedule status before launching
        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getInstance(context.applicationContext).scheduleDao()
            val schedule = dao.getById(scheduleId)

            if (schedule == null || schedule.isCompleted) {
                return@launch
            }

            // Launch target app in main thread
            CoroutineScope(Dispatchers.Main).launch {
                context.packageManager.getLaunchIntentForPackage(packageName)?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }

            // Update schedule status
            dao.update(schedule.copy(isCompleted = true))
        }
    }
} 