package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.corrot.firenotes.ui.AddNoteFragment
import com.corrot.firenotes.ui.MainFragment
import com.corrot.firenotes.utils.Constants
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
    AddNoteFragment.AddNoteListener,
    MainFragment.MainListener {

    companion object {
        @JvmField
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var drawer: Drawer

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

            createDriver(user)

            // On first activity creation load mainFragment
            if (savedInstanceState == null) {
                val mainFragment = MainFragment(drawer)
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
                    fragment = fragmentManager.findFragmentByTag(Constants.ADD_NOTE_FRAGMENT_KEY)
                    if (fragment != null) {
                        (fragment as AddNoteFragment).setAddNoteListener(this)
                    }
                }
            }
        }
    }

    private fun loadMainFragment() {
        var mainFragment = fragmentManager.findFragmentByTag(Constants.MAIN_FRAGMENT_KEY)

        if (mainFragment == null) {
            mainFragment = MainFragment(drawer)
            mainFragment.setMainListener(this)
            fragmentManager.beginTransaction()
                .replace(fragmentContainer.id, mainFragment, Constants.MAIN_FRAGMENT_KEY)
                .commit()
        } else {
            fragmentManager.popBackStack(
                Constants.ADD_NOTE_FRAGMENT_KEY,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            (mainFragment as MainFragment).setMainListener(this)
        }
    }

    private fun loadAddNoteFragment() {
        var addNoteFragment = fragmentManager.findFragmentByTag(Constants.ADD_NOTE_FRAGMENT_KEY)

        if (addNoteFragment == null) {
            addNoteFragment = AddNoteFragment(drawer.drawerLayout)
            addNoteFragment.setAddNoteListener(this)
            fragmentManager.beginTransaction()
                .replace(fragmentContainer.id, addNoteFragment, Constants.ADD_NOTE_FRAGMENT_KEY)
                .addToBackStack(Constants.ADD_NOTE_FRAGMENT_KEY)
                .commit()
        } else {
            (addNoteFragment as AddNoteFragment).setAddNoteListener(this)
        }
    }

    private fun createDriver(user: FirebaseUser) {
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
            .withAccountHeader(header)
            .addDrawerItems(notesItem, DividerDrawerItem())
            .build()
    }

    override fun fabClicked() {
        loadAddNoteFragment()
    }

    override fun noteAdded() {
        loadMainFragment()
    }

    override fun backClicked() {
        loadMainFragment()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else if (fragmentManager.backStackEntryCount > 0) {
            val addNoteFragment = fragmentManager.findFragmentByTag(Constants.ADD_NOTE_FRAGMENT_KEY)
            if (addNoteFragment != null) {
                (addNoteFragment as AddNoteFragment).back()
            }
        } else {
            super.onBackPressed()
        }
    }
}
