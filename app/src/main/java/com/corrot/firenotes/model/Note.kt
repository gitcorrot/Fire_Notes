package com.corrot.firenotes.model

data class Note(val id: String) {
    var title: String? = null
    var body: String? = null
    var color: Int? = null
    var lastChanged: Long? = null

    // empty constructor needed for data deserialization
    constructor() : this(id = "")
}
