package com.odukle.presetsroom

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val PRESET_GROUPS_NEW = "presetGroupsNew"
const val PRESETS = "presets"
const val NAME = "name"
const val PIC_URL_BEFORE = "picUrlBefore"
const val PIC_URL_AFTER = "picUrlAfter"
var ma: MainActivity? = null
private const val TAG = "MainActivity"
const val MY_PREFS = "myPrefs"
private const val SHOW_DIALOG = "showD"
const val INTERSTITIAL_UNIT_ID = "ca-app-pub-9193191601772541/8133083844"

class MainActivity : AppCompatActivity() {

    private var adapter: RecyclerViewAdapter? = null
    lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ma = this
        setSupportActionBar(toolbar_main)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = INTERSTITIAL_UNIT_ID
        mInterstitialAd.loadAd(adRequest)
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        val db = FirebaseFirestore.getInstance()
        val sharedPref = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val showD = sharedPref.getBoolean(SHOW_DIALOG, true)
        if (showD) {
            val dialog = MaterialAlertDialogBuilder(this)
            val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
            dialog.setCancelable(false)
                .setView(dialogLayout)
                .setPositiveButton(
                    "Got it!"
                ) { dialog, _ ->
                    dialog?.dismiss()
                    if (dialogLayout.checkbox.isChecked) {
                        val editor = sharedPref.edit()
                        editor.putBoolean(SHOW_DIALOG, false)
                        editor.apply()
                    }
                }.show()
        }

        if (!isOnline(this)) {
            Toast.makeText(this, "No internet, check your internet connection", Toast.LENGTH_LONG)
                .show()
        }
        val presetsRef = db.collection(PRESET_GROUPS_NEW)
        val query = presetsRef.orderBy(NAME)
        val options =
            FirestoreRecyclerOptions.Builder<PresetGroup>().setQuery(query, PresetGroup::class.java)
                .build()
        adapter = RecyclerViewAdapter(options)
        rv_main.layoutManager = LinearLayoutManager(this)
        rv_main.adapter = adapter
        adapter?.startListening()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = FirebaseFirestore.getInstance()
                        Log.d(TAG, "passing id: $pgId")
                        if (pgId != null) {
                            db.collection(PRESET_GROUPS_NEW).document(pgId!!).collection(PRESETS)
                                .get()
                                .addOnCompleteListener {
                                    if (it.result != null) {
                                        for (preset in it.result!!) {
                                            val url = preset[PRESET_URL] as String?
                                            val name = preset[NAME] as String?
                                            Log.d(TAG, "passing url: $url and name: $name")
                                            if (url != null && name != null) {
                                                startDownloading(url, name, false)
                                            }
                                        }
                                    }
                                }
                        } else {
                            Toast.makeText(ma, "Group id not found", Toast.LENGTH_SHORT).show()
                        }

                    }
                } else {
                    Snackbar.make(layout_main, "Permission denied", Snackbar.LENGTH_LONG)
                        .setAction("Allow permission") {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            })
                        }.show()
                }
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_import_preset -> {
                startActivity(Intent(this, HowToImport::class.java))
            }

            R.id.menu_upload -> {
                startActivity(Intent(this, Upload::class.java))
            }

            R.id.menu_about_app -> {
                startActivity(Intent(this, AboutApp::class.java))
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}

