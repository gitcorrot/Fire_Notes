package com.corrot.firenotes.model

import android.os.Parcelable
import com.corrot.firenotes.utils.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Note(
    var id: String,
    var title: String = "",
    var body: String = "",
    var color: Int = Constants.DEFAULT_COLOR,
    var pinned: Boolean = false,
    var lastChanged: Long = 0
) : Parcelable {
    // empty constructor needed for data deserialization
    constructor() : this(id = "")
}
