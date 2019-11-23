package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.ui.MainFragment
import com.corrot.firenotes.utils.Constants
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    MainFragment.MainListener {

    companion object {
        @JvmField
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var drawer: Drawer
    private lateinit var toolbar: BottomAppBar
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase
        mAuth = FirebaseAuth.getInstance()

        // Fragments
        fragmentManager = supportFragmentManager
        fragmentContainer = fl_main_fragment_container

        // If user is null (is not logged in) open signUpActivity
        if (mAuth.currentUser == null) {
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            this.overridePendingTransition(0, 0)
            finish()
        } else {
            val user: FirebaseUser = mAuth.currentUser!!
            Log.d(TAG, "LOGGED AS: ${user.email.toString()}")

            // Setting Toolbar
            toolbar = toolbar_main as BottomAppBar
            toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
            toolbar.title = "Fire notes"
            setSupportActionBar(toolbar)

            // Create drawer
            createDrawer(user)

            // Floating Action Button
            fab = fab_main
            fab.setOnClickListener {
                openNoteActivity(null, Constants.FLAG_ADD_NOTE)
            }

            // On first activity creation load mainFragment
            if (savedInstanceState == null) {
                val mainFragment = MainFragment()
                mainFragment.setMainListener(this)
                fragmentManager.beginTransaction()
                    .add(fragmentContainer.id, mainFragment, Constants.MAIN_FRAGMENT_KEY)
                    .commit()
            } else {
                // If activity is recreated check for opened fragments
                // Checking mainFragment
                var fragment = fragmentManager.findFragmentByTag(Constants.MAIN_FRAGMENT_KEY)
                if (fragment != null) {
                    (fragment as MainFragment).setMainListener(this)
                } else {
                    // Checking addNoteFragment
                    // TODO: check for remaining fragments
                }
            }
        }
    }

    // TODO: It will be useful later when there will be more fragments
//    private fun loadMainFragment() {
//        var mainFragment = fragmentManager.findFragmentByTag(Constants.MAIN_FRAGMENT_KEY)
//
//        if (mainFragment == null) {
//            mainFragment = MainFragment(drawer)
//            mainFragment.setMainListener(this)
//            fragmentManager.beginTransaction()
//                .replace(fragmentContainer.id, mainFragment, Constants.MAIN_FRAGMENT_KEY)
//                .commit()
//        } else {
//            (mainFragment as MainFragment).setMainListener(this)
//        }
//    }

    private fun createDrawer(user: FirebaseUser) {
        val notesItem =
            PrimaryDrawerItem().withIdentifier(Constants.DRAWER_NOTES_ITEM).withName("Notes")

        val header = AccountHeaderBuilder()
            .withActivity(this)
            .addProfiles(ProfileDrawerItem().withName(user.displayName).withEmail(user.email))
            .withProfileImagesVisible(true)
            .withSelectionListEnabled(false)
            .withDividerBelowHeader(true)
            .build()

        drawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(header)
            .addDrawerItems(notesItem, DividerDrawerItem())
            .build()
    }

    private fun openNoteActivity(note: Note?, flag: Int) {
        val intent = Intent(this, NoteActivity::class.java)
        val bundle = Bundle()

        bundle.putInt(Constants.NOTE_KEY, flag)

        if (note != null) {
            bundle.putString(Constants.NOTE_ID_KEY, note.id)
            bundle.putString(Constants.NOTE_TITLE_KEY, note.title)
            bundle.putString(Constants.NOTE_BODY_KEY, note.body)
            note.color?.let { bundle.putInt(Constants.NOTE_COLOR_KEY, it) }
            note.lastChanged?.let { bundle.putLong(Constants.NOTE_LAST_CHANGED_KEY, it) }
        }

        startActivity(intent, bundle)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onItemClicked(note: Note) {
        // TODO: start noteActivity to preview/edit note
    }
}
