package org.c8.research.comming.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import com.pawegio.kandroid.textWatcher
import kotlinx.android.synthetic.main.content_settings.*
import org.c8.research.comming.Constants
import org.c8.research.comming.ImComingApplication
import org.c8.research.comming.LocationBoard
import org.c8.research.comming.R
import org.c8.research.comming.adapters.AvatarChooserAdapter
import org.c8.research.comming.entities.Preferences
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SettingsActivity : AppCompatActivity() {

    val comingApi by lazy { (applicationContext as ImComingApplication).comingApi }
    val locationBoard by lazy { (application as ImComingApplication).locationBoard }

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
        send_logs.setOnClickListener { sendSupport(this) }
    }

    fun onSettingsUpdated() {
        Preferences.Route.id ?: return
        comingApi.updateRouteSettings(
                Preferences.Route.id!!,
                Preferences.Settings.toRouteSettings()
        )
    }

    fun sendSupport(context: Context) {
        val attachment = zipDeviceLogs(context)
        sendEmail(context, "dmitry.stabrovsky@gmail.com", "logs", "logs", attachment)
    }

    fun sendEmail(context: Context,
                  recipient: String,
                  subject: String,
                  text: String?,
                  attachment: File?) {
        val emailIntent = createEmailIntent(recipient, subject, text, attachment)
        context.startActivity(Intent.createChooser(emailIntent, null))
    }

    fun createEmailIntent(recipient: String,
                          subject: String,
                          text: String?,
                          attachment: File?): Intent {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:" + recipient)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        if (text != null) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, text)
        }
        if (attachment != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment))
        }
        return emailIntent
    }

    fun zipDeviceLogs(context: Context): File? {
        val logs = readDeviceLogs(1024 * 1024)
        var attachment: File? = null
        if (logs != null) {
            try {
                attachment = File(context.externalCacheDir, "log.zip")
                zipData(attachment, "log.txt", logs.toByteArray())
            } catch (e: IOException) {
                attachment = null
            }

        }
        return attachment
    }

    @Throws(IOException::class)
    fun zipData(zipFile: File, entryFilename: String, data: ByteArray) {
        var zipOutputStream: ZipOutputStream? = null
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(zipFile))
            val entry = ZipEntry(entryFilename)

            zipOutputStream.putNextEntry(entry)
            zipOutputStream.write(data)
            zipOutputStream.closeEntry()
        } finally {
            if (zipOutputStream != null) {
                zipOutputStream.close()
            }
        }
    }


    fun readDeviceLogs(maxLogSize: Int): String? {
        try {
            val pid = android.os.Process.myPid()
            val process = Runtime.getRuntime().exec("logcat -d | grep " + pid)
            val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream))

            val log = StringBuilder()
            var line = bufferedReader.readLine()
            while (line != null) {
                log.append(line).append("\n")
                line = bufferedReader.readLine()
            }

            if (log.length > maxLogSize) {
                log.delete(0, log.length - maxLogSize)
            }
            return log.toString()
        } catch (e: Exception) {
            return null
        }

    }

}
