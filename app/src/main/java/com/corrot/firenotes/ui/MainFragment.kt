package com.corrot.firenotes.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.corrot.firenotes.NoteActivity
import com.corrot.firenotes.R
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.Constants
import com.corrot.firenotes.utils.Constants.Companion.FLAG_EDIT_NOTE
import com.corrot.firenotes.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainFragment : Fragment(), NotesAdapter.OnItemClickListener {
    companion object {
        @JvmField
        val TAG: String = MainFragment::class.java.simpleName
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // Set up recyclerView displaying notes
        val layoutManager = StaggeredGridLayoutManager(2, VERTICAL)

        layoutManager.gapStrategy = GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        notesAdapter = NotesAdapter(arrayListOf(), this)
        view.rv_main.layoutManager = layoutManager
        view.rv_main.adapter = notesAdapter

        // Attach itemTouchHelper to recyclerView
        attachTouchItemHelper(view.rv_main)

        // Creating MainViewModel
        activity?.let {
            val factory = SavedStateViewModelFactory(it.application, this)
            mainViewModel =
                ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        }

        // Passing Fragment's view as LifecycleOwner to avoid memory leaks
        mainViewModel.allNotes.observe(viewLifecycleOwner, Observer<List<Note>> {
            Log.d(TAG, "Updating notes adapter")
            notesAdapter.setNotes(it)
        })

        // Show / Hide loading bar
        mainViewModel.dataLoading.observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> {
                    view.v_main_shadow.visibility = View.VISIBLE
                    view.pb_main.visibility = View.VISIBLE
                }
                false -> {
                    view.v_main_shadow.visibility = View.GONE
                    view.pb_main.visibility = View.GONE
                }
            }
        })

        mainViewModel.snackBarMessage.observe(viewLifecycleOwner, Observer { msg ->
            Snackbar.make(requireActivity().fab_main, msg, Snackbar.LENGTH_SHORT)
                .setAnchorView(requireActivity().fab_main)
                .show()
        })

        return view
    }

    private fun attachTouchItemHelper(recyclerView: RecyclerView) {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT
                .or(ItemTouchHelper.RIGHT)
                .or(ItemTouchHelper.UP)
                .or(ItemTouchHelper.DOWN),
            ItemTouchHelper.LEFT
                .or(ItemTouchHelper.RIGHT)
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                notesAdapter.swapItems(viewHolder.adapterPosition, target.adapterPosition)
                // TODO: swap notes indexes in db
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                Log.d(TAG, "Trying to delete note on position: $position")

                if (position != RecyclerView.NO_POSITION) {
                    onItemRemoved(position, notesAdapter.getNoteOnPosition(position))
                }
            }
        }
        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(recyclerView)
    }

    // Callback from NoteAdapter
    override fun onItemClicked(note: Note) {
        val intent = Intent(context, NoteActivity::class.java)
        val bundle = Bundle()
        bundle.putInt(Constants.FLAG_NOTE_KEY, FLAG_EDIT_NOTE)
        bundle.putParcelable(Constants.NOTE_KEY, note)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onPinUpClicked(note: Note) {
        mainViewModel.pinUpNote(note)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    fun onItemRemoved(pos: Int, note: Note) {

        notesAdapter.removeNote(pos)

        // TODO: UI problem:
        //  1. removing first note from adapter
        //  2. removing second note from adapter
        //  3. snackbar dismissed -> delete first note from db and notify adapter to update
        //  4. adapter adds second deleted note back, because of db changes listener
        //  5. snackbar dismissed -> delete second note from db and notify adapter to update

        Snackbar.make(requireActivity().fab_main, "Note removed", Snackbar.LENGTH_LONG)
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION)
                        mainViewModel.removeNoteWithId(note.id)
                }
            })
            .setAnchorView(requireActivity().fab_main)
            .setAction("Undo") {
                // retrieve note when user delete it and click 'undo' action
                notesAdapter.addNote(pos, note)
            }
            .show()
    }
}
