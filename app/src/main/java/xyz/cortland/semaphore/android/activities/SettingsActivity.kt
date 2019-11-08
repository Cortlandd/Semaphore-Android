package xyz.cortland.semaphore.android.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.settings_activity.*
import xyz.cortland.semaphore.android.BuildConfig
import xyz.cortland.semaphore.android.R
import xyz.cortland.semaphore.android.SemaphoreApp
import xyz.cortland.semaphore.android.helpers.prefs
import java.util.*
import kotlin.properties.Delegates

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

        app_version.text = "v${BuildConfig.VERSION_NAME}"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateUpTo(Intent(this, ActivityListActivity::class.java))
        this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            intializePreferences()
        }

        fun intializePreferences() {


            preferenceScreen.findPreference<Preference>("send_feedback").also {
                it?.setOnPreferenceClickListener {
                    sendFeedback()
                    true
                }
            }

            preferenceScreen.findPreference<Preference>("open_source_licenses").also {
                it?.setOnPreferenceClickListener {
                    Intent(context!!, OpenSourceLicensesActivity::class.java).also { i ->
                        startActivity(i)
                    }
                    true
                }
            }

            preferenceScreen.findPreference<Preference>("faq").also {
                it?.setOnPreferenceClickListener {
                    Intent(context!!, FAQActivity::class.java).also { i ->
                        startActivity(i)
                    }
                    true
                }
            }

            preferenceScreen.findPreference<Preference>("rate_app").also {
                it?.setOnPreferenceClickListener {
                    rateApp()
                    true
                }
            }

        }

        fun sendFeedback() {
            val deviceModel = Build.MODEL
            val appVersion = BuildConfig.VERSION_NAME
            val body =
                """
                        -------------------- <br/>
                        App: Semaphore <br/>
                        Version: $appVersion <br/>
                        Device: $deviceModel
                    """.trimIndent()

            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.type = "message/rfc822"
            emailIntent.data = Uri.parse("mailto:cortland1568@gmail.com")
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback - Android")
            emailIntent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
        }

        /**
         *
         * Start with rating the app
         * Determine if the Play Store is installed on the device
         *
         * */
        fun rateApp() {
            val uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
            var intent = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

            if (intent.resolveActivity(context!!.packageManager) != null) {
                startActivity(intent)
            }
            else {
                intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))
                if (intent.resolveActivity(context!!.packageManager) != null) {
                    startActivity(intent)
                }
                else {
                    Toast.makeText(context, "The app currently isn't in the Play Store.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}