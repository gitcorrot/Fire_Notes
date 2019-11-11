package com.corrot.firenotes.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.MainActivity
import com.corrot.firenotes.R
import com.corrot.firenotes.model.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainFragment : Fragment() {
    companion object {
        @JvmField
        val TAG: String = MainFragment::class.java.simpleName
    }

    interface MainListener {
        fun fabClicked()
    }

    private lateinit var callback: MainListener
    private lateinit var toolbar: Toolbar
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        toolbar = view.toolbar_main as Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        toolbar.title = "Fire notes"
        (activity as MainActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        fab = view.fab_main
        fab.setOnClickListener {
            callback.fabClicked()
        }

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
