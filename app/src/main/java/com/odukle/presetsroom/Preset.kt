package com.odukle.presetsroom

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Preset(
    var name: String? = "",
    var picUrlBefore: String? = "",
    var picUrlAfter: String? = "",
    var presetUrl: String? = "",
    var id: String? = "0"
) : Parcelable