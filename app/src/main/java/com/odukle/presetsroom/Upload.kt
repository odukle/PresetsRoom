package com.odukle.presetsroom

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_upload.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


private const val TAG = "Upload"

class Upload : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        refresh_progress_bar.visibility = View.GONE
        updating.visibility = View.GONE

        btn_refresh.setOnClickListener {

            CoroutineScope(IO).launch {
                this@Upload.runOnUiThread {
                    refresh_progress_bar.visibility = View.VISIBLE
                    updating.visibility = View.VISIBLE
                }
                val storageRef = FirebaseStorage.getInstance().reference
                val db = FirebaseFirestore.getInstance()
                val groupList = listOf(
                    "Curated",
                    "Custom",
                    "Dark",
                    "Extreme",
                    "Gloom",
                    "Gloomy",
                    "Mono",
                    "Mood",
                    "Normal",
                    "NormalDay",
                    "OneUp",
                    "Portrait"
                )

                for (f in groupList) {
                    val groupRef = storageRef.child(f)
                    val groupRefNew = storageRef.child("Presets").child(f)

                    groupRef.child("GroupPicAfter.jpg").downloadUrl.addOnSuccessListener { uri ->
                        val picUrlAfter = uri.toString()
                        val data = hashMapOf(
                            "id" to f,
                            NAME to f,
                            PIC_URL_AFTER to picUrlAfter
                        )
                        db.collection(PRESET_GROUPS_NEW).document(f).set(data, SetOptions.merge())
                    }

                    groupRef.child("GroupPicBefore.jpg").downloadUrl.addOnSuccessListener { uri ->
                        val picUrlBefore = uri.toString()
                        val data = hashMapOf(
                            PIC_URL_BEFORE to picUrlBefore
                        )
                        db.collection(PRESET_GROUPS_NEW).document(f).set(data, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(this@Upload, "Success", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "success")

                            }.addOnFailureListener { e ->
                                Toast.makeText(this@Upload, "Failed", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "failed: ${e.message}")
                            }
                    }

                    groupRefNew.listAll().addOnSuccessListener { presets ->
                        for (p in presets.items) {
                            var picUrlBefore: String? = null
                            var picUrlAfter: String?
                            var presetUrl: String?
                            val pName = p.name
                            Log.d(TAG, "name: $pName")
                            if (pName.endsWith(".jpg")) {
                                if (pName.endsWith("og.jpg")) {
                                    val nameId = pName.substring(0, pName.indexOf('.') - 2)
                                    p.downloadUrl.addOnSuccessListener {
                                        picUrlBefore = it.toString()
                                    }.addOnSuccessListener {
                                        val data = hashMapOf(
                                            "id" to nameId,
                                            NAME to nameId,
                                            PIC_URL_BEFORE to picUrlBefore
                                        )

                                        db.collection(PRESET_GROUPS_NEW).document(f).collection(PRESETS)
                                            .document(nameId).set(data, SetOptions.merge())
                                    }

                                } else {
                                    val nameId = pName.substring(0, pName.indexOf('.'))
                                    p.downloadUrl.addOnSuccessListener { uri ->
                                        picUrlAfter = uri.toString()
                                        Log.d(TAG, "url: $picUrlAfter")
                                        val data = hashMapOf(
                                            PIC_URL_AFTER to picUrlAfter
                                        )

                                        db.collection(PRESET_GROUPS_NEW).document(f).collection(PRESETS)
                                            .document(nameId).set(data, SetOptions.merge())
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this@Upload,
                                                    "Success",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }.addOnFailureListener { e ->
                                                Toast.makeText(
                                                    this@Upload,
                                                    "Failed",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                                Log.e(TAG, "failed: ${e.message}")
                                            }
                                    }
                                }

                            }

                            if (pName.endsWith(".xmp")) {
                                val nameId = pName.substring(0, pName.indexOf('.'))
                                p.downloadUrl.addOnSuccessListener { uri ->
                                    presetUrl = uri.toString()
                                    val data = hashMapOf(
                                        PRESET_URL to presetUrl
                                    )

                                    db.collection(PRESET_GROUPS_NEW).document(f).collection(PRESETS)
                                        .document(nameId + "og").delete()

                                    db.collection(PRESET_GROUPS_NEW).document(f).collection(PRESETS)
                                        .document(nameId).set(data, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@Upload,
                                                "Success",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }.addOnFailureListener { e ->
                                            Toast.makeText(
                                                this@Upload,
                                                "Failed",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                            Log.e(TAG, "failed: ${e.message}")
                                        }
                                }
                            }
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "failed: ${e.message}")
                    }
                }

                this@Upload.runOnUiThread {
                    refresh_progress_bar.visibility = View.GONE
                    updating.visibility = View.GONE
                }

            }

        }

    }

}