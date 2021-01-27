package com.odukle.presetsroom
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PresetGroup(
    var name: String = "",
    var picUrlBefore: String = "",
    var picUrlAfter: String = "",
    var id: String = "0",
) : Parcelable