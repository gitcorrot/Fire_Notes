package com.corrot.firenotes

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.Constants
import com.corrot.firenotes.viewmodel.NoteViewModel
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_note.*

class NoteActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = NoteActivity::class.java.simpleName
    }

    private lateinit var noteViewModel: NoteViewModel

    private var flag: Int = 0
    private var color: Int = 0

    private lateinit var toolbar: Toolbar
    private lateinit var colorView: View
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var bodyInputLayout: TextInputLayout
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        // Always read flag
        flag = intent.getIntExtra(Constants.FLAG_NOTE_KEY, 0)

        // Initialize views
        toolbar = toolbar_note as BottomAppBar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = "Add note"
        setSupportActionBar(toolbar)

        titleInputLayout = til_note_title
        bodyInputLayout = til_note_body
        colorView = v_note_color
        fab = fab_note

        // Initialize viewModel
        noteViewModel = NoteViewModel()

        // Set up observers
        noteViewModel.getColor().observe(this, Observer {
            val shape = colorView.background as GradientDrawable
            shape.setColor(it)
        })

        // Restore state
        if (savedInstanceState != null) {
            val id = savedInstanceState.getCharSequence(Constants.SAVE_STATE_ID)
            val title = savedInstanceState.getCharSequence(Constants.SAVE_STATE_TITLE)
            val body = savedInstanceState.getCharSequence(Constants.SAVE_STATE_BODY)
            color = savedInstanceState.getInt(Constants.SAVE_STATE_COLOR, 0)

            if (!id.isNullOrEmpty())
                noteViewModel.setId(id.toString())

            if (!title.isNullOrEmpty()) {
                noteViewModel.setTitle(title.toString())
                titleInputLayout.editText?.setText(title)
            }

            if (!body.isNullOrEmpty()) {
                noteViewModel.setBody(body.toString())
                bodyInputLayout.editText?.setText(body)
            }

            if (color != 0) {
                noteViewModel.setColor(color)
            } else {
                setDefaultColor()
            }
        } else {
            // If there is no savedInstanceState -> try to retrieve intent bundle
            val bundle = intent.extras
            retrieveDataFromBundle(bundle)
        }

        // Update user input in viewModel
        titleInputLayout.editText?.addTextChangedListener {
            noteViewModel.setTitle(it.toString())
        }

        bodyInputLayout.editText?.addTextChangedListener {
            noteViewModel.setBody(it.toString())
        }

        // Save on FAB clicked
        fab.setOnClickListener {
            onFabClicked()
        }
    }

    //--------------------------------------------------------------------------------------------//

    /**
     * Function that retrieves Note from Bundle and update ViewModel
     * @param b Bundle?
     */
    private fun retrieveDataFromBundle(b: Bundle?) {
        b?.let { data ->
            when (data.getInt(Constants.FLAG_NOTE_KEY)) {
                Constants.FLAG_ADD_NOTE -> {
                    Log.d(TAG, "Opened NoteActivity with 'add note' flag")
                    setDefaultColor()
                }
                Constants.FLAG_EDIT_NOTE -> {
                    Log.d(TAG, "Opened NoteActivity with 'edit note' flag")
                    val note = getNoteFromBundle(data)
                    note?.let { n ->
                        noteViewModel.setNote(n)
                        // Set originalNote as n to check for changes later
                        noteViewModel.originalNote = n
                        // Update edit texts manually (can't be updated via observers, because
                        // there are text watchers set
                        titleInputLayout.editText?.setText(n.title)
                        bodyInputLayout.editText?.setText(n.body)
                    }
                }
                else -> {
                    Log.e(TAG, "Opened NoteActivity with no flag")
                }
            }
        }
    }

    /**
     * Function that is called when user clicks floating action button.
     */
    private fun onFabClicked() {
        when (flag) {
            // add note -> only validate
            Constants.FLAG_ADD_NOTE -> {
                if (validateInput()) {
                    // If title or body is not empty -> add note to db
                    noteViewModel.addNoteToDatabase()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Can't add empty note", Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // edit note -> check for changes
            Constants.FLAG_EDIT_NOTE -> {
                if (checkForChanges()) {
                    // If changes were made -> validate input
                    if (validateInput()) {
                        noteViewModel.addNoteToDatabase()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Can't add empty note", Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // No changes were made, finish without saving
                    finish()
                }
            }
            else -> {
                Log.e(TAG, "Wrong flag!")
            }
        }
    }

    /**
     * Function that sets color as default (colorSecondary)
     */
    private fun setDefaultColor() {
        color = colorView.context.getColor(R.color.colorSecondary)
        noteViewModel.setColor(color)
    }

    /**
     * Function that validates user input. Checks if note's title or body is not null.
     * @return Returns true if title or body is not null, else returns false.
     */
    private fun validateInput(): Boolean {
        /*  OR
        1 | 1 = 1
        1 | 0 = 1
        0 | 1 = 1
        0 | 0 = 0
        */
        return (!noteViewModel.getTitle().isNullOrEmpty() ||
                !noteViewModel.getBody().isNullOrEmpty())
    }

    /**
     * Function that checks for changes
     * @return If changes were made returns true, else returns false
     */
    private fun checkForChanges(): Boolean {
        val originalNote = noteViewModel.originalNote
        val note = noteViewModel.getNote()

        Log.d(TAG, "original: ${originalNote?.title}, note: ${note?.title}")
        Log.d(TAG, "original: ${originalNote?.body}, note: ${note?.body}")
        Log.d(TAG, "original: ${originalNote?.color}, note: ${note?.color}")

        return if (originalNote != null && note != null) {
            originalNote.title != note.title
                    || originalNote.body != note.body
                    || originalNote.color != note.color
        } else {
            false
        }
    }

    /**
     * Function that retrieves Note from bundle
     * @return If Note is not null returns Note, else null
     */
    private fun getNoteFromBundle(b: Bundle): Note? {
        val id = b.getString(Constants.NOTE_ID_KEY)

        return if (!id.isNullOrEmpty()) {
            val note = Note(id)
            note.title = b.getString(Constants.NOTE_TITLE_KEY)
            note.body = b.getString(Constants.NOTE_BODY_KEY)
            note.color = b.getInt(Constants.NOTE_COLOR_KEY)
            note.lastChanged = b.getLong(Constants.NOTE_LAST_CHANGED_KEY)

            note
        } else {
            // Should never get there
            Log.e(TAG, "Note id must not be null")
            null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                back()
                true
            }
            R.id.action_set_color -> {
                //
                if (color == 0) color = -1

                ColorPickerDialogBuilder
                    .with(colorView.context)
                    .setTitle("Choose color")
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .showBorder(true)
                    .showColorPreview(false)
                    .density(8)
                    .noSliders()
                    .initialColor(color)
                    .setOnColorChangedListener {
                        Log.d(TAG, "selected color: $it")
                        color = it
                    }
                    .setPositiveButton("Ok") { _, _, _ ->
                        noteViewModel.setColor(color = color)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }
                    .build()
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Function that is called when user press back button or back indicator from toolbar.
     * Validates data and finishes activity if conditions are meet.
     */
    private fun back() {
        currentFocus?.clearFocus()

        when (flag) {
            // add note -> only validate
            Constants.FLAG_ADD_NOTE -> {
                if (validateInput()) {
                    // If title or body is not empty -> show alert dialog
                    val dialogBuilder = MaterialAlertDialogBuilder(this)
                    with(dialogBuilder) {
                        setTitle("Discard note?")
                        setPositiveButton("Discard") { _, _ ->
                            finish()
                        }
                        setNegativeButton("Cancel", null)
                        show()
                    }
                } else {
                    // If title and body is empty
                    finish()
                }
            }
            // edit note -> check for changes
            Constants.FLAG_EDIT_NOTE -> {
                if (checkForChanges()) {
                    // If changes were made -> show alert dialog
                    val dialogBuilder = MaterialAlertDialogBuilder(this)
                    with(dialogBuilder) {
                        setTitle("Discard note?")
                        setPositiveButton("Discard") { _, _ ->
                            finish()
                        }
                        setNegativeButton("Cancel", null)
                        show()
                    }
                } else {
                    // No changes were made, finish
                    finish()
                }
            }
            else -> {
                Log.e(TAG, "Wrong flag!")
            }
        }
    }

    override fun onBackPressed() {
        back()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val n = noteViewModel.getNote()

        if (n != null) {
            outState.putCharSequence(Constants.SAVE_STATE_ID, n.id)
            n.title?.let { outState.putCharSequence(Constants.SAVE_STATE_TITLE, it) }
            n.body?.let { outState.putCharSequence(Constants.SAVE_STATE_BODY, it) }
            n.color?.let { outState.putInt(Constants.SAVE_STATE_COLOR, it) }
        }

        super.onSaveInstanceState(outState)
    }
}
