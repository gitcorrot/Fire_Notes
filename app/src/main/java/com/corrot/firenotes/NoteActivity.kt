package com.corrot.firenotes

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProviders
import com.corrot.firenotes.utils.Constants
import com.corrot.firenotes.viewmodel.NoteViewModel
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_note.*

class NoteActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = NoteActivity::class.java.simpleName
    }

    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        // Initialize viewModel
        val factory = SavedStateViewModelFactory(application, this, savedInstanceState)
        noteViewModel = ViewModelProviders.of(this, factory).get(NoteViewModel::class.java)

        // Set flag
        noteViewModel.flag = intent.getIntExtra(Constants.FLAG_NOTE_KEY, 0)

        // Initialize views
        val toolbar = toolbar_note as BottomAppBar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(toolbar)

        // Set up observers
        noteViewModel.noteColor.observe(this, Observer {
            val shape = v_note_color.background as GradientDrawable
            shape.setColor(it)
        })

        noteViewModel.noteTitle.observe(this, Observer {
            if (til_note_title.editText?.text.toString() != it)
                til_note_title.editText?.setText(it)
        })

        noteViewModel.noteBody.observe(this, Observer {
            if (til_note_body.editText?.text.toString() != it)
                til_note_body.editText?.setText(it)
        })

        // Try to retrieve intent bundle
        if (savedInstanceState == null) {
            val bundle = intent.extras
            noteViewModel.retrieveDataFromBundle(bundle)
        }

        // Update user input in viewModel
        til_note_title.editText?.addTextChangedListener {
            noteViewModel.setNoteTitle(it.toString())
        }

        til_note_body.editText?.addTextChangedListener {
            noteViewModel.setNoteBody(it.toString())
        }


        fab_note.setOnClickListener {
            if (noteViewModel.onFabClicked()) {
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Can't add empty note", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //--------------------------------------------------------------------------------------------//

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
                var color = -1
                noteViewModel.noteColor.value?.let { color = it }
                if (color == 0) color = -1

                ColorPickerDialogBuilder
                    .with(v_note_color.context)
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
                        noteViewModel.setNoteColor(color)
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

    override fun onBackPressed() {
        back()
    }

    private fun back() {
        currentFocus?.clearFocus()

        if (noteViewModel.onBackClicked()) {
            finish()
        } else {
            val dialogBuilder = MaterialAlertDialogBuilder(this)
            with(dialogBuilder) {
                setTitle("Discard note?")
                setPositiveButton("Discard") { _, _ ->
                    finish()
                }
                setNegativeButton("Cancel", null)
                show()
            }
        }
    }
}
