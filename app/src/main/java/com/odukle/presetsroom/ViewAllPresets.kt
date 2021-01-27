package com.odukle.presetsroom

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_view_all_presets.*

var vap: ViewAllPresets? = null

class ViewAllPresets : AppCompatActivity() {

    private var adapter: RecyclerViewAdapterVAP? = null
    lateinit var adRequest: AdRequest
    lateinit var mInterstitialAd: InterstitialAd
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_presets)
        vap = this
        adRequest = AdRequest.Builder().build()
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = INTERSTITIAL_UNIT_ID
        mInterstitialAd.loadAd(adRequest)
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

        setSupportActionBar(toolbar_vap)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
        val groupName = intent.getStringExtra(GROUP_NAME)
        supportActionBar?.title = groupName

        if (!isOnline(this)) {
            Toast.makeText(this, "No internet, check your connection", Toast.LENGTH_LONG).show()
        }

        val db = FirebaseFirestore.getInstance()
        val groupId = intent.getStringExtra(PRESET_GROUP_ID)
        val presetsRef = groupId?.let { db.collection(PRESET_GROUPS_NEW).document(it).collection(PRESETS) }
        val query = presetsRef?.orderBy("name")
        val options = query?.let { FirestoreRecyclerOptions.Builder<Preset>().setQuery(it, Preset::class.java).build() }
        adapter = options?.let { RecyclerViewAdapterVAP(it) }
        rv_vap.layoutManager = LinearLayoutManager(this)
        rv_vap.adapter = adapter
        adapter?.startListening()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
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
                    if (url != null && name != null) {
                        startDownloading(url!!, name!!, true)
                    }
                } else {
                    Snackbar.make(layout_vap, "Permission denied", Snackbar.LENGTH_LONG)
                        .setAction("Allow permission") {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            })
                        }.show()
                }
            }
        }
    }
}