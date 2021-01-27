package com.odukle.presetsroom

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

const val PRESET_URL = "presetUrl"
const val PRESET_GROUP_ID = "groupId"
const val GROUP_NAME = "groupName"
private const val TAG = "RecyclerViewAdapter"
const val WRITE_PERMISSION_CODE = 1111
private const val ITEM_TYPE_PRESET_GROUP = 0
private const val ITEM_TYPE_BANNER_AD = 1
var pgId: String? = null

class RecyclerViewAdapter(options: FirestoreRecyclerOptions<PresetGroup>) :
    FirestoreRecyclerAdapter<PresetGroup, RecyclerView.ViewHolder>(options) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewBefore: ImageView = view.findViewById(R.id.imageView_before_rv)
        val imageViewAfter: ImageView = view.findViewById(R.id.imageView_after_rv)
        val viewAllButton: Button = view.findViewById(R.id.btn_viewAll)
        val downloadAllButton: Button = view.findViewById(R.id.btn_downloadAll)
        val nameTextView: TextView = view.findViewById(R.id.name_rv)
    }

    internal class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adView: AdView = itemView.findViewById(R.id.ad_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_BANNER_AD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.banner_ad_layout, parent, false)
                AdViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_layout, parent, false)
                ViewHolder(view)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        model: PresetGroup
    ) {
        when (getItemViewType(position)) {
            ITEM_TYPE_PRESET_GROUP -> {
                val pgHolder = holder as ViewHolder
                val presetGroup = getItem(position)

                pgHolder.nameTextView.text = presetGroup.name
                Picasso.get()
                    .load(presetGroup.picUrlBefore)
                    .placeholder(R.drawable.loading)
                    .into(pgHolder.imageViewBefore)

                Picasso.get()
                    .load(presetGroup.picUrlAfter)
                    .placeholder(R.drawable.loading)
                    .into(pgHolder.imageViewAfter)

                pgHolder.downloadAllButton.setOnClickListener {
                    Log.d(TAG, "downloadAllButton clicked")
                    val ma = ma
                    if (ma != null) {
                        if (ma.mInterstitialAd.isLoaded) {
                            ma.mInterstitialAd.show()
                        } else {
                            Log.d(TAG, "The interstitial wasn't loaded yet.")
                        }

                        val pg = this@RecyclerViewAdapter.getItem(pgHolder.adapterPosition)
                        pgId = pg.id

                        if (isOnline(ma.applicationContext)) {
                            if (ma.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                Log.d(TAG, "requesting permission")
                                ma.requestPermissions(
                                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    WRITE_PERMISSION_CODE
                                )
                            } else {
                                CoroutineScope(IO).launch {
                                    val db = FirebaseFirestore.getInstance()
                                    Log.d(TAG, "passing id: $pgId")
                                    if (pgId != null) {
                                        db.collection(PRESET_GROUPS_NEW).document(pgId!!)
                                            .collection(PRESETS)
                                            .get()
                                            .addOnCompleteListener {
                                                if (it.result != null) {
                                                    Toast.makeText(
                                                        ma,
                                                        "Downloading ${it.result.count()} presets",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    for (preset in it.result!!) {
                                                        val url = preset[PRESET_URL] as String?
                                                        val name = preset[NAME] as String?
                                                        Log.d(
                                                            TAG,
                                                            "passing url: $url and name: $name"
                                                        )
                                                        if (ma.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                                            Log.d(TAG, "requesting permission")
                                                            ma.requestPermissions(
                                                                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                                                WRITE_PERMISSION_CODE
                                                            )
                                                        } else {
                                                            if (url != null && name != null) {
                                                                startDownloading(url, name, false)
                                                            }
                                                        }
                                                    }

                                                    Toast.makeText(
                                                        ma,
                                                        "Downloaded ${it.result.count()} presets to Downloads",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }


                                                if (it.isSuccessful) {
                                                    Log.d(TAG, "query successful")
                                                }
                                            }

                                    } else {
                                        Toast.makeText(ma, "Group id not found", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }

                        } else {
                            Toast.makeText(
                                ma.applicationContext,
                                "No internet, check your internet connection",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }

                pgHolder.viewAllButton.setOnClickListener {
                    val intent = Intent(ma, ViewAllPresets::class.java)
                    val groupName = getItem(position).name
                    val pg = getItem(position)
                    intent.putExtra(PRESET_GROUP_ID, pg.id)
                    intent.putExtra(GROUP_NAME, groupName)
                    ma?.startActivity(intent)
                }

                val listener = View.OnClickListener {
                    val intent = Intent(ma, ViewAllPresets::class.java)
                    val groupName = getItem(position).name
                    val pg = getItem(position)
                    intent.putExtra(PRESET_GROUP_ID, pg.id)
                    intent.putExtra(GROUP_NAME, groupName)
                    ma?.startActivity(intent)
                }
                pgHolder.imageViewBefore.setOnClickListener(listener)
                pgHolder.imageViewAfter.setOnClickListener(listener)
                ma?.findViewById<RecyclerView>(R.id.rv_main)?.visibility = View.VISIBLE
                ma?.findViewById<ImageView>(R.id.loading_icon)?.visibility = View.GONE
                Log.d(TAG, "bind ends")
            }

            ITEM_TYPE_BANNER_AD -> {
                val adHolder = holder as AdViewHolder
                val adRequest = AdRequest.Builder().build()
                adHolder.adView.loadAd(adRequest)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position % 4 == 0) {
            if (position == 0) {
                return ITEM_TYPE_PRESET_GROUP
            }
            return ITEM_TYPE_BANNER_AD
        }
        return ITEM_TYPE_PRESET_GROUP
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun startDownloading(url: String, name: String, toast: Boolean): Long {
    val vap = vap
    if (vap != null) {
        val count = vap.count
        vap.count++
        if (count >= 5) {
            if (vap.mInterstitialAd.isLoaded) {
                vap.mInterstitialAd.show()
                vap.count = 0
            } else {
                Log.d(TAG, "The interstitial wasn't loaded yet.")
            }
        }

        if (toast) {
            Toast.makeText(
                vap,
                "Downloading $name",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    val request = DownloadManager.Request(Uri.parse(url))
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        .setDescription("The file is downloading...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/$name.xmp")
    val manager = (ma ?: vap)!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val id = manager.enqueue(request)
    if (vap != null) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val mId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == mId) {
                    Toast.makeText(
                        vap,
                        "Downloaded $name to Downloads",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        vap.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    return id
}

@RequiresApi(Build.VERSION_CODES.M)
fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager != null) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
    }
    return false
}

