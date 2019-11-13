package com.corrot.firenotes.model

data class Note(var title: String) {
    var id: String? = null
    var body: String? = null
    var color: Int? = null
    var lastChanged: Long? = null

    // empty constructor needed for data deserialization
    constructor() : this(title = "")
}
