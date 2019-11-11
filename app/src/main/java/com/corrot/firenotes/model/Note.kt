package com.corrot.firenotes.model

data class Note(var title: String) {
    var body: String? = null
    var lastChanged: Long? = null

    // empty constructor needed for data deserialization
    constructor() : this(title = "")
}
