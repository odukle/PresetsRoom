package com.odukle.presetsroom

import android.content.pm.PackageManager
import android.os.Build
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
import com.google.android.gms.ads.AdView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


private const val TAG = "RecyclerViewAdapterVAP"
const val BANNER_UNIT_ID = "ca-app-pub-9193191601772541/5434251397"
private const val ITEM_TYPE_PRESET = 0
private const val ITEM_TYPE_BANNER_AD = 1
var url: String? = null
var name: String? = null

class RecyclerViewAdapterVAP(options: FirestoreRecyclerOptions<Preset>) :
    FirestoreRecyclerAdapter<Preset, RecyclerView.ViewHolder>(options) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewBefore: ImageView = view.findViewById(R.id.imageView_before)
        val imageViewAfter: ImageView = view.findViewById(R.id.imageView_after)
        val nameTextView: TextView = view.findViewById(R.id.name_vap)
        val downloadButton: Button = view.findViewById(R.id.btn_download)
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
                    .inflate(R.layout.recycler_view_layout_vap, parent, false)
                ViewHolder(view)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Preset) {
        when (getItemViewType(position)) {
            ITEM_TYPE_PRESET -> {
                val presetHolder = holder as ViewHolder
                val preset = getItem(position)
                Picasso.get()
                    .load(preset.picUrlBefore)
                    .placeholder(R.drawable.loading)
                    .into(presetHolder.imageViewBefore)

                Picasso.get()
                    .load(preset.picUrlAfter)
                    .placeholder(R.drawable.loading)
                    .into(presetHolder.imageViewAfter)
                presetHolder.nameTextView.text = preset.name

                presetHolder.downloadButton.setOnClickListener {
                    if (isOnline(vap!!.applicationContext)) {
                        CoroutineScope(IO).launch {
                            val db = FirebaseFirestore.getInstance()
                            val presetId =
                                getItem(position).id
                            val groupId = vap?.intent?.getStringExtra(PRESET_GROUP_ID)
                            if (groupId != null) {
                                if (presetId != null) {
                                    db.collection(PRESET_GROUPS_NEW)
                                        .document(groupId)
                                        .collection(PRESETS)
                                        .document(presetId).get().addOnCompleteListener {
                                            if (it.result != null) {
                                                url = it.result!!.get("presetUrl") as String?
                                                name = it.result!!.get("name") as String?
                                                if (vap?.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                                    Log.d(TAG, "requesting permission")
                                                    vap?.requestPermissions(
                                                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                                        WRITE_PERMISSION_CODE
                                                    )
                                                } else {
                                                    if (url != null && name != null) {
                                                        startDownloading(url!!, name!!, true)
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            vap!!.applicationContext,
                            "No internet, check your internet connection",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            ITEM_TYPE_BANNER_AD -> {
                val adHolder = holder as AdViewHolder
                adHolder.adView.loadAd(vap?.adRequest)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position % 6 == 0) {
            if (position == 0) {
                return ITEM_TYPE_PRESET
            }
            return ITEM_TYPE_BANNER_AD
        }
        return ITEM_TYPE_PRESET
    }
}
