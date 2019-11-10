package com.corrot.firenotes

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_add_note.*
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

    private lateinit var mAuth: FirebaseAuth
    private lateinit var callback: AddNoteListener

    private lateinit var toolbar: Toolbar
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var noteInputLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_note, container, false)

        toolbar = view.toolbar_add_note as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = "Add note"
        (activity as MainActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        titleInputLayout = view.til_add_note_title
        noteInputLayout = view.til_add_note_note

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
                callback.backClicked()
                true
            }
            R.id.action_set_color -> {
                // TODO: set note color
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
