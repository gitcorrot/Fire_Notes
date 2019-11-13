package com.corrot.firenotes.ui

import android.os.Bundle
import android.view.*
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
import com.google.android.material.textfield.TextInputLayout
import com.mikhaellopez.circleview.CircleView
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
    private lateinit var colorView: CircleView
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var bodyInputLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_note, container, false)

        toolbar = view.toolbar_add_note as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = "Add note"
        (activity as MainActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        addNoteViewModel = AddNoteViewModel()

        titleInputLayout = view.til_add_note_title
        bodyInputLayout = view.til_add_note_body

        // Set color on change
        colorView = view.v_add_note_color
        addNoteViewModel.getColor().observe(this, Observer {
            colorView.apply { circleColor = it }
        })

        // Restore state
        if (savedInstanceState != null) {
            val title = savedInstanceState.getCharSequence(Constants.SAVE_STATE_TITLE).toString()
            val body = savedInstanceState.getCharSequence(Constants.SAVE_STATE_BODY).toString()
            val color = savedInstanceState.getInt(Constants.SAVE_STATE_COLOR)

            addNoteViewModel.setTitle(title)
            titleInputLayout.editText?.setText(title)
            addNoteViewModel.setBody(body)
            bodyInputLayout.editText?.setText(body)
            addNoteViewModel.setColor(color)
        }

        // Update user input
        titleInputLayout.editText?.addTextChangedListener {
            addNoteViewModel.setTitle(it.toString())
        }

        bodyInputLayout.editText?.addTextChangedListener {
            addNoteViewModel.setBody(it.toString())
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
                // If title or body is not empty - add note to db
                if (!addNoteViewModel.getTitle().value.isNullOrEmpty()
                    || !addNoteViewModel.getBody().value.isNullOrEmpty()
                ) {
                    addNoteViewModel.addNoteToDatabase()
                }

                callback.backClicked()
                true
            }
            R.id.action_set_color -> {
                // Set default color
                var color = resources.getColor(R.color.colorAccent)

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
