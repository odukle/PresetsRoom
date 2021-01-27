package com.odukle.presetsroom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.youtube.player.YouTubeStandalonePlayer
import kotlinx.android.synthetic.main.activity_how_to_import_splash.*

var htiS: HowToImportSplash? = null
var showIns: Boolean? = null
const val SHOW_SPLASH_INS = "splashIns"

class HowToImportSplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_import_splash)

        htiS = this
        btn_skip.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            val sharedPref = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean(SHOW_SPLASH_INS, false).apply()
            finish()
        }

        val instructions = listOf(
            "Open Lightroom, go to presets and click on the three dots as shown below",
            "Click on \"Import presets\"",
            "Go to Downloads folder",
            "Select one or more presets to import",
            "click on the dropdown shown below",
            "Imported presets will appear under their respective folders"
        )

        val images = listOf(
            R.drawable.how_to1,
            R.drawable.how_to2,
            R.drawable.how_to3,
            R.drawable.how_to5,
            R.drawable.how_to_color,
            R.drawable.how_to6
        )

        val adapter = ViewPagerAdapter(instructions, images)
        viewPager.adapter = adapter
        circle_indicator.setViewPager(viewPager)

        btn_watch_video.setOnClickListener {
            val intent = YouTubeStandalonePlayer.createVideoIntent(
                this,
                getString(R.string.GOOGLE_API_KEY),
                VIDEO_ID,
                0,
                true,
                true
            )
            startActivity(intent)
        }
    }

    fun openLightRoom(view: View) {
        //Get url from tag
        val url = view.tag as String
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        //pass the url to intent data
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}
