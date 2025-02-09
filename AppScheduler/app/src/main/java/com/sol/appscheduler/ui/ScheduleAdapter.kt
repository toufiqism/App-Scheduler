package com.sol.appscheduler.ui

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sol.appscheduler.data.Schedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.sol.appscheduler.R

class ScheduleAdapter(
    private val onEdit: (Schedule) -> Unit,
    private val onDelete: (Schedule) -> Unit
) : ListAdapter<Schedule, ScheduleAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.tvAppName)
        val time: TextView = view.findViewById(R.id.tvTime)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = getItem(position)
        val pm = holder.itemView.context.packageManager
        
        try {
            holder.appName.text = pm.getApplicationInfo(schedule.packageName, 0).loadLabel(pm)
        } catch (e: PackageManager.NameNotFoundException) {
            holder.appName.text = "Unknown App"
        }
        
        holder.time.text = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            .format(Date(schedule.triggerTime))
            
        holder.status.text = if (schedule.isCompleted) "Completed" else "Pending"
        holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context,
            if (schedule.isCompleted) android.R.color.holo_green_dark else android.R.color.holo_red_dark
        ))

        holder.btnEdit.setOnClickListener { onEdit(schedule) }
        holder.btnDelete.setOnClickListener { onDelete(schedule) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule) = oldItem == newItem
    }
} 