package com.sol.appscheduler.ui

import android.app.Dialog
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sol.appscheduler.R

class AppPickerDialog(
    private val context: Context,
    private val onAppSelected: (ApplicationInfo) -> Unit
) : Dialog(context) {

    private val packageManager = context.packageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_app_picker)
        
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
        
        val adapter = AppListAdapter(apps) {
            onAppSelected(it)
            dismiss()
        }
        
        findViewById<RecyclerView>(R.id.rvApps).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }
}

class AppListAdapter(
    private val apps: List<ApplicationInfo>,
    private val onClick: (ApplicationInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivIcon)
        val name: TextView = view.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.loadIcon(holder.itemView.context.packageManager))
        holder.name.text = app.loadLabel(holder.itemView.context.packageManager)
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount() = apps.size
} 