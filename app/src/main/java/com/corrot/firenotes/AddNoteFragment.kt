package com.corrot.firenotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var callback: AddNoteListener

    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var noteInputLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_note, container, false)

        titleInputLayout = view.til_add_note_title
        noteInputLayout = view.til_add_note_note

        return view
    }
}