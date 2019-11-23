package com.corrot.firenotes.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.corrot.firenotes.R
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainFragment : Fragment() {
    companion object {
        @JvmField
        val TAG: String = MainFragment::class.java.simpleName
    }

    interface MainListener {
        fun onItemClicked(note: Note)
    }

    private lateinit var callback: MainListener
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: StaggeredGridLayoutManager
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var shadow: View
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // RecyclerView displaying notes
        recyclerView = view.rv_main
        layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        recyclerView.layoutManager = layoutManager
        notesAdapter = NotesAdapter(emptyList())
        recyclerView.adapter = notesAdapter

        // Creating MainViewModel
        val mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // Passing Fragment's view as LifecycleOwner to avoid memory leaks
        mainViewModel.getAllNotes().observe(viewLifecycleOwner, Observer<List<Note>> {
            Log.d(TAG, "Updating notes adapter")

                notesAdapter.setNotes(it)

        })

        // Show / Hide loading bar
        shadow = view.v_main_shadow
        progressBar = view.pb_main
        // Passing Fragment's view as LifecycleOwner to avoid memory leaks
        mainViewModel.isLoading().observe(viewLifecycleOwner, Observer {
            when (it) {
                true -> {
                    shadow.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                }
                false -> {
                    shadow.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            }
        })

        return view
    }

    fun setMainListener(callback: MainListener) {
        this.callback = callback
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // TODO: show menu
                true
            }
            R.id.action_options -> {
                // TODO: show options
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
