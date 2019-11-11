package com.corrot.firenotes

import com.corrot.firenotes.model.User
import com.corrot.firenotes.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository {
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun createUser(id: String, email: String?, username: String?) {
        val user = User(id)
        email?.let { user.email = it }
        username?.let { user.username = it }

        database
            .reference
            .child(Constants.USER_KEY)
            .child(id)
            .setValue(user)
    }
}