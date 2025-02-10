package com.takbar.appscheduler


import com.takbar.appscheduler.ui.ScheduleAdapter
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.takbar.appscheduler.databinding.ActivityMainBinding
import java.util.*
import com.takbar.appscheduler.data.AppDatabase
import com.takbar.appscheduler.viewmodels.ScheduleViewModel
import androidx.lifecycle.ViewModelProvider
import com.takbar.appscheduler.data.Schedule
import com.takbar.appscheduler.ui.AppPickerDialog
import com.takbar.appscheduler.util.AlarmScheduler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import android.view.View

/**
 * Main activity showing scheduled items and handling user interactions
 * Features:
 * - RecyclerView with scheduled items
 * - Floating action button for adding new schedules
 * - Date and time pickers for schedule creation
 * - Permission handling for exact alarms
 * - Animated background with Lottie
 */
class MainActivity : AppCompatActivity() {
    // View binding and ViewModel
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ScheduleViewModel by lazy {
        val dao = AppDatabase.getInstance(this).scheduleDao()
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScheduleViewModel(dao) as T
            }
        })[ScheduleViewModel::class.java]
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup UI components
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Configure Lottie animation background
        binding.animationView.apply {
            setAnimation(R.raw.space_background)
            playAnimation()
        }
        
        // Set up FAB click listener
        binding.fab.setOnClickListener { showScheduleDialog() }
        
        // Initialize RecyclerView and check permissions
        setupRecyclerView()
        checkExactAlarmPermission()
    }
    
    private fun setupRecyclerView() {
        val adapter = ScheduleAdapter(
            onEdit = { schedule ->
                showTimePicker(schedule)
            },
            onDelete = { schedule ->
                viewModel.deleteSchedule(schedule)
                AlarmScheduler.cancel(this, schedule)
            }
        )
        
        binding.rvSchedules.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.schedules.collect { schedules ->
                    adapter.submitList(schedules)
                }
            }
        }
    }

    private fun checkExactAlarmPermission() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs exact alarm permission to schedule apps accurately")
                .setPositiveButton("Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun showTimePicker(schedule: Schedule) {
        val calendar = Calendar.getInstance().apply { timeInMillis = schedule.triggerTime }
        
        // First show Date Picker
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            
            // Then show Time Picker
            TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                val updatedSchedule = schedule.copy(triggerTime = calendar.timeInMillis)
                viewModel.updateSchedule(updatedSchedule)
                AlarmScheduler.cancel(this, schedule)
                AlarmScheduler.schedule(this, updatedSchedule)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showScheduleDialog() {
        // App Picker
        AppPickerDialog(this) { appInfo ->
            val calendar = Calendar.getInstance()
            
            // First show Date Picker
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                
                // Then show Time Picker
                TimePickerDialog(this, { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }

                    val newSchedule = Schedule(
                        packageName = appInfo.packageName,
                        triggerTime = calendar.timeInMillis
                    )
                    
                    viewModel.addSchedule(newSchedule)
                    AlarmScheduler.schedule(this, newSchedule)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }.show()
    }
}