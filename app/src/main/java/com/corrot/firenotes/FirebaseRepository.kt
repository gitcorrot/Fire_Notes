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
                    true -> Log.d(TAG, "createUser:success")
                    false -> Log.e(TAG, "createUser:failed")
                }
            }
    }

    fun addNoteToDatabase(note: Note) {
        auth.uid?.let { uid ->

            val ref = if (note.id.isEmpty()) {
                database.reference
                    .child(Constants.NOTE_KEY)
                    .child(uid)
                    .push()

            } else {
                database.reference
                    .child(Constants.NOTE_KEY)
                    .child(uid)
                    .child(note.id)
            }

            if (note.id.isEmpty()) note.id = ref.key!!

            ref.setValue(note)
                .addOnCompleteListener { result ->
                    when (result.isSuccessful) {
                        true -> Log.d(TAG, "addNoteToDatabase:success")
                        false -> Log.e(TAG, "addNoteToDatabase:failed")
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

    fun removeNoteWithId(noteId: String) {
        auth.uid?.let { uid ->
            val ref = database
                .reference
                .child(Constants.NOTE_KEY)
                .child(uid)
                .child(noteId)

            ref.removeValue()
                .addOnCompleteListener { result ->
                    when (result.isSuccessful) {
                        true -> Log.d(TAG, "removeNoteWithId:success")
                        false -> Log.e(TAG, "removeNoteWithId:failed")
                    }
                }
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
