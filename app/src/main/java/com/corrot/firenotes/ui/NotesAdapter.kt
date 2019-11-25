package com.corrot.firenotes.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.corrot.firenotes.R
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.inflate
import kotlinx.android.synthetic.main.item_note.view.*

class NotesAdapter(
    private var notes: List<Note>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<NotesAdapter.NoteHolder>() {

    interface OnItemClickListener {
        fun onItemClicked(note: Note)
    }

    class NoteHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view = v
        private val titleView = v.tv_item_note_title
        private val bodyView = v.tv_item_note_body
        private val colorView = v.v_item_note_color

        fun bind(n: Note, clickListener: OnItemClickListener) {

            view.setOnClickListener {
                clickListener.onItemClicked(n)
            }

            if (!n.title.isNullOrEmpty()) {
                titleView.text = n.title
                titleView.visibility = View.VISIBLE
            } else {
                titleView.visibility = View.GONE
            }

            if (!n.body.isNullOrEmpty()) {
                bodyView.text = n.body
                bodyView.visibility = View.VISIBLE
            } else {
                bodyView.visibility = View.GONE
            }

            if (n.color != null) {
                colorView.background.setTint(n.color!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val inflatedView = parent.inflate(R.layout.item_note, attachToRoot = false)
        return NoteHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.bind(notes[position], itemClickListener)
    }

    override fun getItemCount(): Int = notes.size

    fun setNotes(notes: List<Note>) {
        val diff = notifyNotesChanged(notes, this.notes)
        this.notes = notes
        diff.dispatchUpdatesTo(this)
    }

    fun getNotes(): List<Note> {
        return this.notes
    }

    private fun notifyNotesChanged(
        newNotes: List<Note>,
        oldNotes: List<Note>
    ): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldNotes.size
            override fun getNewListSize(): Int = newNotes.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldNotes[oldItemPosition].id == newNotes[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldNotes[oldItemPosition].title == newNotes[newItemPosition].title &&
                        oldNotes[oldItemPosition].body == newNotes[newItemPosition].body &&
                        oldNotes[oldItemPosition].color == newNotes[newItemPosition].color
        })
    }
}
