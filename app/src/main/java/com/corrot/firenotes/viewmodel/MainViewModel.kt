package com.corrot.firenotes.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.notifyObserver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    companion object {
        @JvmField
        val TAG: String = MainViewModel::class.java.simpleName
    }

    private val firebaseRepository = FirebaseRepository()
    private var allNotes = MutableLiveData<List<Note>>()

    init {
        loadNotes()
    }

    fun getAllNotes(): LiveData<List<Note>> {
        return allNotes
    }

    // TODO: move it to FragmentMainViewModel
    private fun loadNotes() {
        viewModelScope.launch {
            Log.d(TAG, "Loading Notes...")
            // Coroutine that will be canceled when the ViewModel is cleared.
            firebaseRepository.addNotesListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Detected notes data change")
                    Log.d(TAG, "Notes number: ${snapshot.childrenCount}")
                    val notes: List<Note> = snapshot.children.mapNotNull {
                        it.getValue(Note::class.java)
                    }
                    allNotes.value = notes
                    allNotes.notifyObserver()
                }

                override fun onCancelled(e: DatabaseError) {
                    Log.d(TAG, "Error during loading notes: ${e.message}")
                }
            })
        }
    }
}