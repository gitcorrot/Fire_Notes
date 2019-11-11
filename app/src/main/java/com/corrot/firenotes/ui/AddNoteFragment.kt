package com.corrot.firenotes.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.MainActivity
import com.corrot.firenotes.R
import com.corrot.firenotes.model.Note
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var bodyInputLayout: TextInputLayout

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
        bodyInputLayout = view.til_add_note_body

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

                // CREATE NOTE
                val title = titleInputLayout.editText?.text.toString()
                val body = bodyInputLayout.editText?.text.toString()
                val note = Note(title)
                note.body = body

                // ADD NOTE TO DB
                val firebaseRepository = FirebaseRepository()
                firebaseRepository.addNoteToDatabase(note)

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
