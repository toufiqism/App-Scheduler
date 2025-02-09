package com.sol.appscheduler.receiver

import com.sol.appscheduler.data.Schedule
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sol.appscheduler.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling alarm triggers
 * Launches the target app and updates schedule status
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Extract schedule details from intent
        val packageName = intent.getStringExtra("package_name") ?: return
        val scheduleId = intent.getIntExtra("schedule_id", -1)

        // Launch target app
        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }

        // Update schedule status in background thread
        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getInstance(context.applicationContext).scheduleDao()
            dao.getById(scheduleId)?.let { existing ->
                dao.update(existing.copy(isCompleted = true))
            }
        }
    }
} 