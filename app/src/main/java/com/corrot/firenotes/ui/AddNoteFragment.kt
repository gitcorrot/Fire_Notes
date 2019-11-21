package com.corrot.firenotes.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.corrot.firenotes.MainActivity
import com.corrot.firenotes.R
import com.corrot.firenotes.utils.Constants
import com.corrot.firenotes.viewmodel.AddNoteViewModel
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_add_note.view.*


class AddNoteFragment : Fragment() {
    companion object {
        @JvmField
        val TAG: String = AddNoteFragment::class.java.simpleName
    }

    interface AddNoteListener {
        fun noteAdded()
        fun backClicked()
    }

    private lateinit var callback: AddNoteListener
    private lateinit var addNoteViewModel: AddNoteViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var colorView: View
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var bodyInputLayout: TextInputLayout
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_add_note, container, false)

        toolbar = view.toolbar_add_note as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = "Add note"
        (activity as MainActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        addNoteViewModel = AddNoteViewModel()

        titleInputLayout = view.til_add_note_title
        bodyInputLayout = view.til_add_note_body
        fab = view.fab_add_note

        // Set color on change
        colorView = view.v_note_color

        // Passing Fragment's view as LifecycleOwner to avoid memory leaks
        addNoteViewModel.getColor().observe(viewLifecycleOwner, Observer {
            val shape = colorView.background as GradientDrawable
            shape.setColor(it)
        })

        // Restore state
        if (savedInstanceState != null) {
            val title = savedInstanceState.getCharSequence(Constants.SAVE_STATE_TITLE)
            val body = savedInstanceState.getCharSequence(Constants.SAVE_STATE_BODY)
            val color = savedInstanceState.getInt(Constants.SAVE_STATE_COLOR)

            if (!title.isNullOrEmpty()) {
                addNoteViewModel.setTitle(title.toString())
                titleInputLayout.editText?.setText(title)
            }
            if (!body.isNullOrEmpty()) {
                addNoteViewModel.setBody(body.toString())
                bodyInputLayout.editText?.setText(body)
            }
            addNoteViewModel.setColor(color)
        } else {
            // Set default color
            val color = colorView.context.getColor(R.color.colorSecondary)
            addNoteViewModel.setColor(color)
        }

        // Update user input
        titleInputLayout.editText?.addTextChangedListener {
            addNoteViewModel.setTitle(it.toString())
        }

        bodyInputLayout.editText?.addTextChangedListener {
            addNoteViewModel.setBody(it.toString())
        }

        // Save on FAB clicked
        fab.setOnClickListener {
            // If title or body is not empty - add note to db
            if (!addNoteViewModel.getTitle().value.isNullOrEmpty()
                || !addNoteViewModel.getBody().value.isNullOrEmpty()
            ) {
                addNoteViewModel.addNoteToDatabase()
                callback.backClicked()
            } else {
//                Snackbar.make(toolbar, "Empty note", Snackbar.LENGTH_SHORT).show()
                Toast.makeText(context, "Can't add empty note", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    fun setAddNoteListener(callback: AddNoteListener) {
        this.callback = callback
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_add_note, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
                addNoteViewModel.setColor(color)

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
                        addNoteViewModel.setColor(color = color)
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

    fun back() {
        if (!addNoteViewModel.getTitle().value.isNullOrEmpty()
            || !addNoteViewModel.getBody().value.isNullOrEmpty()
        ) {
            val dialogBuilder = MaterialAlertDialogBuilder(toolbar.context)
            with(dialogBuilder) {
                setTitle("Discard note?")
                setPositiveButton("Discard") { _, _ ->
                    callback.backClicked()
                }
                setNegativeButton("Cancel", null)
                show()
            }
        } else {
            callback.backClicked()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val title = addNoteViewModel.getTitle().value
        val body = addNoteViewModel.getBody().value
        val color = addNoteViewModel.getColor().value

        if (title != null)
            outState.putCharSequence(Constants.SAVE_STATE_TITLE, title)
        if (body != null)
            outState.putCharSequence(Constants.SAVE_STATE_BODY, body)
        if (color != null)
            outState.putInt(Constants.SAVE_STATE_COLOR, color)

        super.onSaveInstanceState(outState)
    }
}
