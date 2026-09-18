package com.pumbanet.client

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pumbanet.client.model.AppInfo
import com.pumbanet.client.utils.PreferencesManager

class SelectAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var prefsManager: PreferencesManager
    private var apps = listOf<AppInfo>()
    private var selectedApps = set<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_apps)

        prefsManager = PreferencesManager(this)
        recyclerView = findViewById(R.id.appsRecyclerView)
        
        selectedApps = prefsManager.getSplitTunnelApps()
        loadInstalledApps()
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AppsAdapter()
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        apps = installedApps
            .filter { !it.packageName.startsWith("com.android") || it.packageName.contains("google") }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = appInfo.loadLabel(pm).toString(),
                    icon = appInfo.loadIcon(pm),
                    isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                    isSelected = selectedApps.contains(appInfo.packageName)
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    override fun onPause() {
        super.onPause()
        // Сохранение выбранных приложений
        prefsManager.setSplitTunnelApps(selectedApps)
    }

    inner class AppsAdapter : RecyclerView.Adapter<AppViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            val app = apps[position]
            holder.bind(app)
        }

        override fun getItemCount(): Int = apps.size
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val appCheckbox: CheckBox = itemView.findViewById(R.id.appCheckbox)

        fun bind(app: AppInfo) {
            appIcon.setImageDrawable(app.icon)
            appName.text = app.appName
            appCheckbox.isChecked = selectedApps.contains(app.packageName)

            itemView.setOnClickListener {
                if (appCheckbox.isChecked) {
                    selectedApps = selectedApps - app.packageName
                } else {
                    selectedApps = selectedApps + app.packageName
                }
                appCheckbox.isChecked = !appCheckbox.isChecked
            }
        }
    }
}
