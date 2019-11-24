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
    private var note: Note? = null

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

        toolbar = toolbar_note as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = "Add note"
        setSupportActionBar(toolbar)

        noteViewModel = NoteViewModel()

        titleInputLayout = til_note_title
        bodyInputLayout = til_note_body
        fab = fab_note

        // Set color on change
        colorView = v_note_color

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
            val color = savedInstanceState.getInt(Constants.SAVE_STATE_COLOR, 0)

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

            if (color != 0)
                noteViewModel.setColor(color)
        } else {
            // Set default color
            val color = colorView.context.getColor(R.color.colorSecondary)
            noteViewModel.setColor(color)

            // If there is no savedInstanceState -> retrieve intent data (flag and note)
            val bundle = intent.extras

            bundle?.let { data ->
                when (data.getInt(Constants.FLAG_NOTE_KEY)) {
                    Constants.FLAG_ADD_NOTE -> {
                        Log.d(TAG, "Opened NoteActivity with 'add note' flag")
                    }
                    Constants.FLAG_EDIT_NOTE -> {
                        Log.d(TAG, "Opened NoteActivity with 'edit note' flag")
                        note = getNoteFromBundle(data)
                        note?.let {
                            noteViewModel.setNote(it)
                            titleInputLayout.editText?.setText(it.title)
                            bodyInputLayout.editText?.setText(it.body)
                        }
                    }
                    else -> {
                        Log.e(TAG, "Opened NoteActivity with no flag")
                    }
                }
            }
        }

        // Update user input
        titleInputLayout.editText?.addTextChangedListener {
            noteViewModel.setTitle(it.toString())
        }

        bodyInputLayout.editText?.addTextChangedListener {
            noteViewModel.setBody(it.toString())
        }

        // Save on FAB clicked
        fab.setOnClickListener {
            // If title or body is not empty - add note to db
            if (!noteViewModel.getTitle().value.isNullOrEmpty()
                || !noteViewModel.getBody().value.isNullOrEmpty()
            ) {
                noteViewModel.addNoteToDatabase()
                finish()
            } else {
//                Snackbar.make(toolbar, "Empty note", Snackbar.LENGTH_SHORT).show()
                Toast.makeText(this, "Can't add empty note", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                // Set default color
                var color = colorView.context.getColor(R.color.colorSecondary)
                noteViewModel.setColor(color)

                ColorPickerDialogBuilder
                    .with(titleInputLayout.context)
                    .setTitle("Choose color")
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .showBorder(true)
                    .showColorPreview(true)
                    .density(8)
                    .noSliders()
                    .initialColor(color)
                    .setOnColorChangedListener { color = it }
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

    private fun back() {
        currentFocus?.clearFocus()

        if (!noteViewModel.getTitle().value.isNullOrEmpty()
            || !noteViewModel.getBody().value.isNullOrEmpty()
        ) {
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
            finish()
        }
    }

    override fun onBackPressed() {
        back()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val id = noteViewModel.getId().value
        val title = noteViewModel.getTitle().value
        val body = noteViewModel.getBody().value
        val color = noteViewModel.getColor().value

        if (id != null)
            outState.putCharSequence(Constants.SAVE_STATE_ID, id)
        if (title != null)
            outState.putCharSequence(Constants.SAVE_STATE_TITLE, title)
        if (body != null)
            outState.putCharSequence(Constants.SAVE_STATE_BODY, body)
        if (color != null)
            outState.putInt(Constants.SAVE_STATE_COLOR, color)

        super.onSaveInstanceState(outState)
    }
}
