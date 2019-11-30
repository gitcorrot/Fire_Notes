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
import com.corrot.firenotes.utils.Constants.Companion.NOTE_BODY_KEY
import com.corrot.firenotes.utils.Constants.Companion.NOTE_COLOR_KEY
import com.corrot.firenotes.utils.Constants.Companion.NOTE_ID_KEY
import com.corrot.firenotes.utils.Constants.Companion.NOTE_LAST_CHANGED_KEY
import com.corrot.firenotes.utils.Constants.Companion.NOTE_TITLE_KEY
import com.corrot.firenotes.utils.Constants.Companion.ORIGINAL_NOTE_KEY
import java.util.*

class NoteViewModel(private val handle: SavedStateHandle) : ViewModel() {

    companion object {
        @JvmField
        val TAG: String = NoteViewModel::class.java.simpleName
    }

    private val firebaseRepository = FirebaseRepository()

    private val _noteTitle: MutableLiveData<String> = handle.getLiveData<String>(NOTE_TITLE_KEY)
    private val _noteBody: MutableLiveData<String> = handle.getLiveData<String>(NOTE_BODY_KEY)
    private val _noteColor: MutableLiveData<Int> = handle.getLiveData<Int>(NOTE_COLOR_KEY)

    val noteTitle: LiveData<String> = _noteTitle
    val noteBody: LiveData<String> = _noteBody
    val noteColor: LiveData<Int> = _noteColor

    private var noteId: String?
        get() = handle.get<String>(NOTE_ID_KEY)
        set(value) = handle.set(NOTE_ID_KEY, value)

    private var originalNote: Note?
        get() = handle.get(ORIGINAL_NOTE_KEY)
        set(value) = handle.set(ORIGINAL_NOTE_KEY, value)

    var flag: Int?
        get() = handle.get<Int>(FLAG_NOTE_KEY)
        set(value) = handle.set(FLAG_NOTE_KEY, value)


    fun setNoteTitle(title: String) {
        if (title.isNotBlank()) {
            _noteTitle.value = title
        }
    }

    fun setNoteBody(body: String) {
        if (body.isNotBlank())
            _noteBody.value = body
    }

    fun setNoteColor(color: Int) {
        _noteColor.value = color
    }

    private fun setNote(n: Note) {
        noteId = n.id
        n.title?.let { setNoteTitle(it) }
        n.body?.let { setNoteBody(it) }
        n.color?.let { setNoteColor(it) }
    }

    /**
     * @return Note made from VM data
     */
    private fun getNote(): Note {
        val n: Note

        val id = noteId
        n = if (!id.isNullOrEmpty()) {
            Note(id)
        } else {
            Note("")
        }

        with(n) {
            title = noteTitle.value
            body = noteBody.value
            color = noteColor.value
        }

        return n
    }

    /**
     * @return returns true if note added without error, else false
     */
    private fun addNoteToDatabase(): Boolean {
        val note = getNote()
        note.lastChanged = Calendar.getInstance().timeInMillis

        return if (validateInput(note.title, note.body)) {
            if (note.id.isEmpty()) {
                firebaseRepository.addNoteToDatabase(
                    note.title,
                    note.body,
                    note.color,
                    note.lastChanged
                )
            } else {
                firebaseRepository.editNoteFromDatabase(
                    note.id,
                    note.title,
                    note.body,
                    note.color,
                    note.lastChanged
                )
            }
            true
        } else {
            Log.e(TAG, "Can't add empty note")
            false
        }
    }


    /**
     * Function that validates user input. Checks if note's title or body is not null.
     * @param title note title
     * @param body note body
     * @return Returns true if title or body is not null, else returns false.
     */
    private fun validateInput(title: String?, body: String?): Boolean {
        /*  OR
        1 | 1 = 1
        1 | 0 = 1
        0 | 1 = 1
        0 | 0 = 0
        */
        return (!title.isNullOrEmpty() || !body.isNullOrEmpty())
    }

    /**
     * Function that validates user input. Checks if note's title or body is not null.
     * @return Returns true if title or body is not null, else returns false.
     */
    private fun validateInput(): Boolean =
        !noteTitle.value.isNullOrEmpty() || !noteBody.value.isNullOrEmpty()


    /**
     * Function that checks for changes
     * @return If changes were made returns true, else returns false
     */
    private fun checkForChanges(): Boolean {
        val note = getNote()
        return if (originalNote != null) {
            // TODO: make comparator
            originalNote!!.title != note.title
                    || originalNote!!.body != note.body
                    || originalNote!!.color != note.color
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
                Constants.FLAG_ADD_NOTE -> {
                    Log.d(TAG, "Opened NoteActivity with 'add note' flag")
                    setDefaultColor()
                }
                Constants.FLAG_EDIT_NOTE -> {
                    Log.d(TAG, "Opened NoteActivity with 'edit note' flag")
                    val note = getNoteFromBundle(data)
                    note?.let { n ->
                        setNote(n)
                        originalNote = n
                    }
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
    private fun getNoteFromBundle(b: Bundle): Note? {
        val id = b.getString(NOTE_ID_KEY)

        return if (!id.isNullOrEmpty()) {
            val note = Note(id)
            note.title = b.getString(NOTE_TITLE_KEY)
            note.body = b.getString(NOTE_BODY_KEY)
            note.color = b.getInt(NOTE_COLOR_KEY)
            note.lastChanged = b.getLong(NOTE_LAST_CHANGED_KEY)

            note
        } else {
            // Should never get there
            Log.e(TAG, "Note id must not be null")
            null
        }
    }

    /**
     * Function that sets color as default (colorSecondary)
     */
    private fun setDefaultColor() = setNoteColor(Constants.DEFAULT_COLOR)

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
