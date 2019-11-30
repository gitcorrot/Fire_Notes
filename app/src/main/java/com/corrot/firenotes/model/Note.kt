package com.corrot.firenotes.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note(
    val id: String,
    var title: String? = "",
    var body: String? = "",
    var color: Int? = -1,
    var lastChanged: Long? = 0
) : Parcelable {
    // empty constructor needed for data deserialization
    constructor() : this(id = "")
}
