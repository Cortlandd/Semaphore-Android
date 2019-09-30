package xyz.cortland.fittimer.android.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.settings_activity.*
import xyz.cortland.fittimer.android.BuildConfig
import xyz.cortland.fittimer.android.SemaphoreApp
import xyz.cortland.fittimer.android.R
import android.widget.ArrayAdapter
import android.widget.EditText
import xyz.cortland.fittimer.android.helpers.prefs
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var locale: Locale? = null

    var language: String? by Delegates.observable(initialValue = locale?.displayLanguage) { _, _, _ ->
        setSpeechTitle()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(settings_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settings_navigation.setNavigationItemSelectedListener(this)
        // Give nav icons color
        settings_navigation.itemIconTintList = null

        // Initialize language here to trigger setSpeechTitle()
        language = String()

    }

    // Do again what was initially done on Create
    fun setSpeechTitle() {

        locale = Locale(prefs?.speechLanguage)

        val menu = settings_navigation.menu

        val language = locale?.displayLanguage

        val speechLanguage = menu!!.findItem(R.id.nav_speech_language)
        speechLanguage.title = "${getString(R.string.switch_activity_to_speech_audio)} ($language)"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateUpTo(Intent(this, ActivityListActivity::class.java))
        this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_about -> {
                val aboutAlert = AlertDialog.Builder(this)
                aboutAlert.setTitle(R.string.about)
                aboutAlert.setMessage("${getString(R.string.app_name)} ${BuildConfig.VERSION_NAME}\nCopyright © Cortland Walker")
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
                opensourceAlert.setTitle(R.string.open_source_licenses)
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
            R.id.nav_speech_language -> {

                var selectedLanguage: String? = null
                val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                val dialogView = this.layoutInflater.inflate(R.layout.speech_language_selection_dialog, null)
                val listview = dialogView.findViewById<ListView>(R.id.speech_language_list_view)
                val filterText = dialogView.findViewById<EditText>(R.id.speech_language_filter)

                val list = ArrayList<String>()
                val listLanguage = ArrayList<String>()

                for (i in SemaphoreApp.applicationContext().availableLanguages) {
                    if (SemaphoreApp.applicationContext().textToSpeech!!.isLanguageAvailable(i) >= TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                        list.add(i.displayName)
                        listLanguage.add(i.language)
                    }
                }

                listview.choiceMode = ListView.CHOICE_MODE_SINGLE

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, list)
                listview.adapter = adapter

                listview.setOnItemClickListener { parent, view, position, id ->
                    selectedLanguage = listLanguage[position]
                }

                builder.setView(dialogView)
                builder.setPositiveButton(R.string.save) { dialog, which ->
                    if (selectedLanguage != null) {
                        prefs!!.speechLanguage = selectedLanguage!!
                        language = selectedLanguage
                    } else {
                        dialog.dismiss()
                    }
                }
                builder.setNegativeButton(R.string.close) { dialog, which ->
                    dialog.dismiss()
                }

                filterText.addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(s: Editable?) {

                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        adapter.filter.filter(s)
                    }

                })

                builder.create().show()
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

                finish()
                this.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

}