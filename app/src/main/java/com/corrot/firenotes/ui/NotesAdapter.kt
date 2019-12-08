package com.corrot.firenotes.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.corrot.firenotes.R
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.inflate
import kotlinx.android.synthetic.main.item_note.view.*
import java.util.Collections.swap


class NotesAdapter(
    private var notes: MutableList<Note>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<NotesAdapter.NoteHolder>() {

    interface OnItemClickListener {
        fun onItemClicked(note: Note)
        fun onPinUpClicked(note: Note)
//        fun onLongItemClicked()
    }

    class NoteHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val view = v
        private val titleView = v.tv_item_note_title
        private val bodyView = v.tv_item_note_body
        private val colorView = v.v_item_note_color
        private val pinUpView = v.ib_item_pin_up

        fun bind(n: Note, clickListener: OnItemClickListener) {

            view.setOnClickListener {
                clickListener.onItemClicked(n)
            }

            pinUpView.setOnClickListener {
                clickListener.onPinUpClicked(n)
            }

            if (n.title.isNotEmpty()) {
                titleView.text = n.title
                titleView.visibility = View.VISIBLE
            } else {
                titleView.visibility = View.GONE
            }

            if (n.body.isNotEmpty()) {
                bodyView.text = n.body
                bodyView.visibility = View.VISIBLE
            } else {
                bodyView.visibility = View.GONE
            }

            if (n.color != 0) {
                colorView.background.setTint(n.color)
            }

            if (n.pinned) {
                Glide.with(pinUpView)
                    .load(R.drawable.ic_pin_filled)
                    .into(pinUpView)
            } else {
                Glide.with(pinUpView)
                    .load(R.drawable.ic_pin_unfilled)
                    .into(pinUpView)
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
        this.notes.clear()
        this.notes.addAll(notes)
        diff.dispatchUpdatesTo(this)
    }

    fun getNotes(): List<Note> {
        return this.notes
    }

    fun addNote(pos: Int, n: Note) {
        notes.add(pos, n)
        notifyItemInserted(pos)
    }

    fun removeNote(pos: Int) {
        notes.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun getNoteOnPosition(position: Int): Note {
        return this.notes[position]
    }

    /**
     * Function called to swap dragged items
     */
    fun swapItems(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                swap(notes, i, i + 1)
                notifyItemMoved(i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                swap(notes, i, i - 1)
                notifyItemMoved(i, i - 1)
            }
        }
//        Collections.swap(notes, fromPosition, toPosition)
//        notifyItemMoved(fromPosition, toPosition)
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
                        oldNotes[oldItemPosition].pinned == newNotes[newItemPosition].pinned &&
                        oldNotes[oldItemPosition].color == newNotes[newItemPosition].color
        })
    }
}
