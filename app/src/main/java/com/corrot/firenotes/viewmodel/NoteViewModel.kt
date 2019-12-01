package com.corrot.firenotes.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.Constants
import com.corrot.firenotes.utils.Constants.Companion.FLAG_NOTE_KEY
import com.corrot.firenotes.utils.Constants.Companion.NOTE_KEY
import com.corrot.firenotes.utils.Constants.Companion.ORIGINAL_NOTE_KEY
import com.corrot.firenotes.utils.notifyObserver
import java.util.*

class NoteViewModel(private val handle: SavedStateHandle) : ViewModel() {

    companion object {
        @JvmField
        val TAG: String = NoteViewModel::class.java.simpleName
    }

    private val firebaseRepository = FirebaseRepository()

    private val _note: MutableLiveData<Note> = handle.getLiveData<Note>(NOTE_KEY, Note(""))
    val note: LiveData<Note> = _note

    private var originalNote: Note?
        get() = handle.get(ORIGINAL_NOTE_KEY)
        set(value) = handle.set(ORIGINAL_NOTE_KEY, value)

    var flag: Int?
        get() = handle.get<Int>(FLAG_NOTE_KEY)
        set(value) = handle.set(FLAG_NOTE_KEY, value)


    private fun setNoteId(id: String?) {
        if (!id.isNullOrEmpty())
            _note.value?.let { it.id = id }
    }

    fun setNoteTitle(title: String) {
        _note.value?.let {
            it.title = title
            _note.notifyObserver()
        }
    }

    fun setNoteBody(body: String) {
        _note.value?.let {
            it.body = body
            _note.notifyObserver()
        }
    }

    fun setNoteColor(color: Int) {
        _note.value?.let {
            it.color = color
            _note.notifyObserver()
        }
    }

    private fun setNoteLastChanged(lastChanged: Long) {
        _note.value?.let { it.lastChanged = lastChanged }
    }

    private fun setNote(n: Note) {
        setNoteId(n.id)
        setNoteTitle(n.title)
        setNoteBody(n.body)
        setNoteColor(n.color)
        setNoteLastChanged(n.lastChanged)
    }

    //----------------------------------------------------------------------------------------------

    /**
     * @return returns true if note added without error, else false
     */
    private fun addNoteToDatabase(): Boolean {
        setNoteLastChanged(Calendar.getInstance().timeInMillis)

        return if (validateInput()) { // if true, note is not null
            firebaseRepository.addNoteToDatabase(note.value!!)
            true
        } else {
            Log.e(TAG, "Can't add empty note")
            false
        }
    }


    /**
     * Function that validates user input. Checks if note's title or body is not empty.
     * @return Returns true if title or body is not null, else returns false.
     */
    private fun validateInput(): Boolean {
        return if (note.value != null)
            note.value!!.title.isNotEmpty() || note.value!!.body.isNotEmpty()
        else
            false
    }


    /**
     * Function that checks for changes
     * @return If changes were made returns true, else returns false
     */
    private fun checkForChanges(): Boolean {
        return if (originalNote != null) {
            // TODO: make comparator
            originalNote!!.title != note.value?.title
                    || originalNote!!.body != note.value?.body
                    || originalNote!!.color != note.value?.color
        } else {
            false
        }
    }


    /**
     * Function that retrieves Note from Bundle and update ViewModel
     * @param b Bundle?
     */
    fun retrieveDataFromBundle(b: Bundle?) {
        b?.let { data ->
            when (data.getInt(FLAG_NOTE_KEY)) {
                Constants.FLAG_ADD_NOTE ->
                    Log.d(TAG, "Opened NoteActivity with 'add note' flag")
                Constants.FLAG_EDIT_NOTE -> {
                    Log.d(TAG, "Opened NoteActivity with 'edit note' flag")
                    getNoteFromBundle(data)
                }
                else -> {
                    Log.e(TAG, "Opened NoteActivity with no flag")
                }
            }
        }
    }


    /**
     * Function that retrieves Note from bundle
     * @return If Note is not null returns Note, else null
     */
    private fun getNoteFromBundle(b: Bundle) {
        b.getParcelable<Note>(NOTE_KEY)?.let {
            originalNote = it
            setNote(it)
        }
    }


    /**
     * Function that is called when user clicks floating action button.
     * @return Returns true if note was added/edited or there is no changes. If error returns false.
     */
    fun onFabClicked(): Boolean {
        return when (flag) {
            Constants.FLAG_ADD_NOTE -> addNoteToDatabase()
            Constants.FLAG_EDIT_NOTE -> if (checkForChanges()) {
                // Changes were made, try to add Note to database and return result
                addNoteToDatabase()
            } else {
                // No changes were made, return true
                true
            }
            else -> {
                Log.e(TAG, "Wrong flag!")
                false
            }
        }
    }


    /**
     * Function that is called when user press back button or back indicator from toolbar.
     * @return true if data is not valid or there was no changes, else returns false
     */
    fun onBackClicked(): Boolean {
        return when (flag) {
            Constants.FLAG_ADD_NOTE -> !validateInput()
            Constants.FLAG_EDIT_NOTE -> !checkForChanges()
            else -> {
                Log.e(TAG, "Wrong flag")
                false
            }
        }
    }
}
