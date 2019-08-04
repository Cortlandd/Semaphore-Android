package xyz.cortland.fittimer.android.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.settings_activity.*
import xyz.cortland.fittimer.android.BuildConfig
import xyz.cortland.fittimer.android.R

class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(settings_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settings_navigation.setNavigationItemSelectedListener(this)
        // Give nav icons color
        //settings_navigation.itemIconTintList = null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateUpTo(Intent(this, WorkoutListActivity::class.java))
        this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_about -> {
                val aboutAlert = AlertDialog.Builder(this)
                aboutAlert.setTitle("About")
                aboutAlert.setMessage("Fit Timer v1.0\nCopyright © Cortland Walker")
                // TODO: Add contributors
                // TODO: aboutAlert.setIcon = App Icon
                aboutAlert.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }.create().show()
            }
            R.id.nav_faq -> {
                Toast.makeText(this, "FAQ", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_licenses -> {
                val opensourceAlert = AlertDialog.Builder(this)
                val opensourceView = this.layoutInflater.inflate(R.layout.open_source_licenses, null)
                opensourceAlert.setTitle("Open Source Licenses")
                opensourceAlert.setView(opensourceView)
                opensourceAlert.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }.create().show()
            }
            R.id.nav_sendfeedback -> {
                val deviceModel = Build.MODEL
                val appVersion = BuildConfig.VERSION_NAME
                val body =
                    """
                        -------------------- <br/>
                        App: Fit Timer <br/>
                        Version: $appVersion <br/>
                        Device: $deviceModel
                    """.trimIndent()

                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.type = "message/rfc822"
                emailIntent.data = Uri.parse("mailto:cortlandwalker@gmail.com")
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback - Android")
                emailIntent.putExtra(Intent.EXTRA_TEXT, body)
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                navigateUpTo(Intent(this, WorkoutListActivity::class.java))
                this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

}