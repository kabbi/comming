package org.c8.research.comming.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.pawegio.kandroid.textWatcher
import kotlinx.android.synthetic.main.content_settings.*
import org.c8.research.comming.CommingApi
import org.c8.research.comming.Constants
import org.c8.research.comming.LocationBoard
import org.c8.research.comming.R
import org.c8.research.comming.adapters.AvatarChooserAdapter
import org.c8.research.comming.entities.Preferences

class SettingsActivity : AppCompatActivity() {
    val mCommingApi: CommingApi by lazy {
        CommingApi.create(applicationContext)
    }

    val locationBoard by lazy {
        LocationBoard(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar?)

        locationBoard.locationStatus
                .first()
                .subscribe( { enabled_switch.isChecked = (it == LocationBoard.Status.RUNNING || it == LocationBoard.Status.PREPARING) } )

        enabled_switch.setOnCheckedChangeListener { view, checked ->
            if (checked) {
                locationBoard.startLocationService(this)
            } else {
                locationBoard.stopLocationService(this)
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
        mCommingApi.updateRouteSettings(
                Preferences.Route.id!!,
                Preferences.Settings.toRouteSettings()
        )
    }

}
