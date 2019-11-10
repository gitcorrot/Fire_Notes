package com.corrot.firenotes.model

data class Note(var title: String) {
    var note: String? = null
    var lastChanged: Long? = null
}
