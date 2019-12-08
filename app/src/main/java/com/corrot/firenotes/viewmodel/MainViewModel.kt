package com.corrot.firenotes.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.Constants.Companion.ALL_NOTES_KEY
import com.corrot.firenotes.utils.Constants.Companion.IS_LOADING_KEY
import com.corrot.firenotes.utils.Constants.Companion.SNACKBAR_MESSAGE_KEY
import com.corrot.firenotes.utils.Event
import com.corrot.firenotes.utils.notifyObserver
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainViewModel(handle: SavedStateHandle) : ViewModel() {

    companion object {
        @JvmField
        val TAG: String = MainViewModel::class.java.simpleName
    }

    private val firebaseRepository = FirebaseRepository()

    private val _allNotes = handle.getLiveData<List<Note>>(ALL_NOTES_KEY)
    private val _dataLoading = handle.getLiveData<Boolean>(IS_LOADING_KEY)
    private val _snackBarMessage = handle.getLiveData<String>(SNACKBAR_MESSAGE_KEY)

    val allNotes: LiveData<List<Note>> = _allNotes
    val dataLoading: LiveData<Boolean> = _dataLoading
    val snackBarMessage: LiveData<String> = _snackBarMessage

    val snackbarTextEvent =
        MutableLiveData<Event<String>>() // https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150


    init {
        loadNotes()
    }

    private fun setLoading(b: Boolean) {
        _dataLoading.value = b
        _dataLoading.notifyObserver()
    }

    private fun loadNotes() {
        setLoading(true)
        Log.d(TAG, "Loading Notes...")

        firebaseRepository.addNotesListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                setLoading(false)
                val notes: List<Note> = snapshot.children.mapNotNull {
                    it.getValue(Note::class.java)
                }
                _allNotes.value = notes.reversed() // reverse to properly order pinned up notes
                _allNotes.notifyObserver()
            }

            override fun onCancelled(e: DatabaseError) {
                setLoading(false)
                snackbarTextEvent.value = Event("Error during loading notes: ${e.message}")
                Log.e(TAG, "Error during loading notes: ${e.message}")
            }
        })
    }

    fun removeNoteWithId(id: String) {
        firebaseRepository.removeNoteWithId(id, OnCompleteListener { result ->
            when (result.isSuccessful) {
                true -> Log.d(TAG, "removeNoteWithId:success")
                false -> {
                    Log.e(TAG, "removeNoteWithId:failed")
                    _snackBarMessage.value = result.exception.toString()
                }
            }
        })
    }

    fun pinUpNote(note: Note) {
        firebaseRepository.pinUpNote(note, OnCompleteListener { result ->
            when (result.isSuccessful) {
                true -> Log.d(TAG, "pinUpNote:success")
                false -> {
                    Log.e(TAG, "pinUpNote:failed")
                    _snackBarMessage.value = result.exception.toString()
                }
            }
        })
    }
}
