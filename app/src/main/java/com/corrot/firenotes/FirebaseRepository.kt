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
                        Log.e(TAG, "createUser:failed")
                    }
                }
            }
    }

    fun editNoteFromDatabase(
        id: String,
        title: String?,
        body: String?,
        color: Int?,
        lastChanged: Long?
    ) {
        auth.uid?.let { uid ->
            val ref = database
                .reference
                .child(Constants.NOTE_KEY)
                .child(uid)
                .child(id)

            val note = Note(id)
            note.title = title
            note.body = body
            note.color = color
            note.lastChanged = lastChanged

            ref.setValue(note)
                .addOnCompleteListener { result ->
                    when (result.isSuccessful) {
                        true -> {
                            // TODO: result successful
                            Log.d(TAG, "editNoteFromDatabase:success")
                        }
                        false -> {
                            // TODO: result failed
                            Log.e(TAG, "editNoteFromDatabase:failed")
                        }
                    }
                }
        }
    }

    fun addNoteToDatabase(
        title: String?,
        body: String?,
        color: Int?,
        lastChanged: Long?
    ) {
        auth.uid?.let { uid ->
            val ref = database
                .reference
                .child(Constants.NOTE_KEY)
                .child(uid)
                .push()

            val note = Note(ref.key!!)
            note.title = title
            note.body = body
            note.color = color
            note.lastChanged = lastChanged

            ref.setValue(note)
                .addOnCompleteListener { result ->
                    when (result.isSuccessful) {
                        true -> {
                            // TODO: result successful
                            Log.d(TAG, "addNoteToDatabase:success")
                        }
                        false -> {
                            // TODO: result failed
                            Log.e(TAG, "addNoteToDatabase:failed")
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
