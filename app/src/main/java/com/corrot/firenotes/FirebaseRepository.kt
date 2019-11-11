package com.corrot.firenotes

import android.util.Log
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.model.User
import com.corrot.firenotes.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseRepository {
    companion object {
        @JvmField
        val TAG: String = FirebaseRepository::class.java.simpleName
    }

    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var listener: ValueEventListener? = null

    fun createUser(id: String, email: String?, username: String?) {
        val user = User(id)
        email?.let { user.email = it }
        username?.let { user.username = it }

        database
            .reference
            .child(Constants.USER_KEY)
            .child(id)
            .setValue(user)
            .addOnCompleteListener { result ->
                when (result.isSuccessful) {
                    true -> {
                        // TODO: result successful
                        Log.d(TAG, "createUser:success")
                    }
                    false -> {
                        // TODO: result failed
                        Log.d(TAG, "createUser:failed")
                    }
                }
            }
    }

    fun addNoteToDatabase(note: Note) {
        auth.uid?.let { uid ->
            database
                .reference
                .child(Constants.NOTE_KEY)
                .child(uid)
                .push()
                .setValue(note)
                .addOnCompleteListener { result ->
                    when (result.isSuccessful) {
                        true -> {
                            // TODO: result successful
                            Log.d(TAG, "addNoteToDatabase:success")
                        }
                        false -> {
                            // TODO: result failed
                            Log.d(TAG, "addNoteToDatabase:failed")
                        }
                    }
                }
        }
    }

    fun addNotesListener(listener: ValueEventListener) {
        this.listener = listener
        auth.uid?.let { uid ->
            database
                .reference
                .child(Constants.NOTE_KEY)
                .child(uid)
                .addValueEventListener(listener)
        }
    }

    fun removeNotesListener() {
        // TODO: remove listener (?)
//        database
//            .reference
//            .child(Constants.NOTE_KEY)
//            .child(auth.uid!!)
//            .removeEventListener(listener)
    }
}
