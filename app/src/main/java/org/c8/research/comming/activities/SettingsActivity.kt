package org.c8.research.comming.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.pawegio.kandroid.textWatcher
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.content_settings.*
import org.c8.research.comming.CommingService
import org.c8.research.comming.Constants
import org.c8.research.comming.R
import org.c8.research.comming.adapters.AvatarChooserAdapter
import org.c8.research.comming.entities.Preferences
import org.c8.research.comming.services.LocationTrackingService
import org.c8.research.comming.utils.connectGoogleApi

class SettingsActivity : AppCompatActivity() {
    val commingService: CommingService by lazy {
        CommingService.create(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        enabled_switch.isChecked = LocationTrackingService.isTrackingLocation(this)
        enabled_switch.setOnCheckedChangeListener { view, checked ->
            if (checked) {
                // TODO: Implement sharing here
                return@setOnCheckedChangeListener
            }
            connectGoogleApi().subscribe {
                LocationTrackingService.stopLocationTracking(this, it)
            }
        }

        title_edit.setText(Preferences.Settings.title)
        title_edit.textWatcher {
            afterTextChanged {
                Preferences.Settings.title = title_edit.text.toString()
                onSettingsUpdated()
            }
        }

        avatar_chooser_view.setHasFixedSize(true)
        avatar_chooser_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val selectedAvatarPosition = Constants.Avatars.indexOfFirst { it.id === Preferences.Settings.avatar }
        selected_avatar_view.setImageResource(Constants.AvatarsMap[Preferences.Settings.avatar]!!)
        avatar_chooser_view.adapter = AvatarChooserAdapter(Constants.Avatars, selectedAvatarPosition) {
            selected_avatar_view.setImageResource(it.drawableResource)
            Preferences.Settings.avatar = it.id
            onSettingsUpdated()
        }
    }

    fun onSettingsUpdated() {
        Preferences.Route.id ?: return
        commingService.updateRouteSettings(
                Preferences.Route.id!!,
                Preferences.Settings.toRouteSettings()
        )
    }

}
