package com.sol.appscheduler.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.sol.appscheduler.receiver.AlarmReceiver
import com.sol.appscheduler.data.Schedule

/**
 * Object for managing alarm scheduling with Android's AlarmManager
 * Handles both scheduling and cancellation of exact alarms
 */
object AlarmScheduler {
    /**
     * Schedule an exact alarm for a given schedule
     * @param context Application context
     * @param schedule Schedule to be triggered
     */
    @RequiresApi(23)
    fun schedule(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        
        // Create intent with schedule details for AlarmReceiver
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("package_name", schedule.packageName)
            putExtra("schedule_id", schedule.id)
        }

        // Create pending intent with unique ID
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set exact alarm that works in Doze mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            schedule.triggerTime,
            pendingIntent
        )
    }

    /**
     * Cancel an existing alarm for a given schedule
     * @param context Application context
     * @param schedule Schedule to cancel
     */
    fun cancel(context: Context, schedule: Schedule) {
        val alarmManager = context.getSystemService<AlarmManager>() ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        
        // Find existing pending intent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancel and remove the pending intent
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
} 