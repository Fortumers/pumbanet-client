package com.pumbanet.client

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pumbanet.client.model.VpnProfile
import com.pumbanet.client.utils.PreferencesManager
import com.pumbanet.client.utils.PingTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addProfileButton: Button
    private lateinit var prefsManager: PreferencesManager
    private var profiles = listOf<VpnProfile>()
    private var activeProfileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)

        prefsManager = PreferencesManager(this)
        recyclerView = findViewById(R.id.profilesRecyclerView)
        addProfileButton = findViewById(R.id.addProfileButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        loadProfiles()

        addProfileButton.setOnClickListener {
            val intent = Intent(this, ConfigImportActivity::class.java)
            startActivityForResult(intent, IMPORT_CONFIG_REQUEST)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfiles()
    }

    private fun loadProfiles() {
        profiles = prefsManager.getProfiles()
        activeProfileId = prefsManager.getActiveProfileId()
        recyclerView.adapter = ProfilesAdapter(profiles, activeProfileId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == IMPORT_CONFIG_REQUEST && resultCode == RESULT_OK) {
            val configJson = data?.getStringExtra("CONFIG_JSON")
            val vlessLink = data?.getStringExtra("CONFIG_VLESS")
            
            if (configJson != null || vlessLink != null) {
                // Парсинг конфига и создание профиля
                val profile = parseProfileFromConfig(configJson, vlessLink)
                if (profile != null) {
                    prefsManager.saveProfile(profile)
                    loadProfiles()
                    Toast.makeText(this, "Профиль добавлен", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseProfileFromConfig(configJson: String?, vlessLink: String?): VpnProfile? {
        return try {
            if (vlessLink != null && vlessLink.startsWith("vless://")) {
                val uri = android.net.Uri.parse(vlessLink)
                VpnProfile(
                    name = uri.fragment ?: "",
                    configJson = configJson ?: "",
                    vlessLink = vlessLink,
                    serverAddress = uri.host ?: "",
                    serverPort = uri.port ?: 443
                )
            } else if (configJson != null) {
                val json = org.json.JSONObject(configJson)
                val outbounds = json.getJSONArray("outbounds")
                if (outbounds.length() > 0) {
                    val outbound = outbounds.getJSONObject(0)
                    val settings = outbound.getJSONObject("settings")
                    val vnext = settings.getJSONArray("vnext")
                    if (vnext.length() > 0) {
                        val v = vnext.getJSONObject(0)
                        VpnProfile(
                            name = "Profile",
                            configJson = configJson,
                            serverAddress = v.getString("address"),
                            serverPort = v.getInt("port")
                        )
                    } else null
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    inner class ProfilesAdapter(
        private val profiles: List<VpnProfile>,
        private val activeProfileId: String?
    ) : RecyclerView.Adapter<ProfilesAdapter.ProfileViewHolder>() {

        inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val nameText: TextView = itemView.findViewById(R.id.profileName)
            private val serverText: TextView = itemView.findViewById(R.id.profileServer)
            private val pingText: TextView = itemView.findViewById(R.id.profilePing)
            private val favoriteButton: ImageButton = itemView.findViewById(R.id.favoriteButton)
            private val selectButton: Button = itemView.findViewById(R.id.selectProfileButton)
            private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

            fun bind(profile: VpnProfile, isActive: Boolean) {
                nameText.text = profile.displayName()
                serverText.text = "${profile.serverAddress}:${profile.serverPort}"
                
                profile.pingMs?.let { ping ->
                    pingText.text = "${ping}ms"
                    pingText.visibility = View.VISIBLE
                } ?: run {
                    pingText.visibility = View.GONE
                }

                favoriteButton.setImageResource(
                    if (profile.isFavorite) R.drawable.ic_star_filled
                    else R.drawable.ic_star_outline
                )

                selectButton.text = if (isActive) "Активен" else "Выбрать"
                selectButton.isEnabled = !isActive

                favoriteButton.setOnClickListener {
                    val updated = profile.copy(isFavorite = !profile.isFavorite)
                    prefsManager.saveProfile(updated)
                    loadProfiles()
                }

                selectButton.setOnClickListener {
                    prefsManager.setActiveProfile(profile.id)
                    loadProfiles()
                    Toast.makeText(this@ProfilesActivity, "Профиль выбран", Toast.LENGTH_SHORT).show()
                }

                itemView.setOnLongClickListener {
                    measurePing(profile)
                    true
                }

                deleteButton.setOnClickListener {
                    showDeleteConfirmation(profile)
                }
            }

            private fun measurePing(profile: VpnProfile) {
                CoroutineScope(Dispatchers.Main).launch {
                    pingText.text = "..."
                    pingText.visibility = View.VISIBLE
                    
                    val ping = withContext(Dispatchers.IO) {
                        PingTest.pingWithRetries(profile.serverAddress, profile.serverPort)
                    }
                    
                    if (ping != null) {
                        val updated = profile.copy(pingMs = ping)
                        prefsManager.saveProfile(updated)
                        loadProfiles()
                    } else {
                        pingText.text = "-"
                    }
                }
            }

            private fun showDeleteConfirmation(profile: VpnProfile) {
                AlertDialog.Builder(this@ProfilesActivity)
                    .setTitle("Удалить профиль?")
                    .setMessage("Вы уверены, что хотите удалить профиль \"${profile.displayName()}\"?")
                    .setPositiveButton("Удалить") { _, _ ->
                        prefsManager.deleteProfile(profile.id)
                        loadProfiles()
                        Toast.makeText(this@ProfilesActivity, "Профиль удалён", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        }

        inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_profile, parent, false)
            return ProfileViewHolder(view)
        }

        override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
            val profile = profiles[position]
            val isActive = profile.id == activeProfileId
            holder.bind(profile, isActive)
        }

        override fun getItemCount(): Int = profiles.size
    }

    companion object {
        private const val IMPORT_CONFIG_REQUEST = 1
    }
}
