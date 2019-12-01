package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.ui.MainFragment
import com.corrot.firenotes.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
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

    private var userName: String? = null
    private var userEmail: String? = null

    private var loginProvider: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase
        mAuth = FirebaseAuth.getInstance()

        // Fragments
        fragmentManager = supportFragmentManager
        fragmentContainer = fl_main_fragment_container

        loginProvider = intent.getIntExtra(Constants.FLAG_LOGIN_PROVIDER, 0)

        if (mAuth.currentUser != null) {
            // User logged in with email and password
            val user = mAuth.currentUser!!
            userName = user.displayName
            userEmail = user.email
            Log.d(TAG, "Logged as: ${user.uid}")
        } else {
            // TODO: ERROR
            Log.e(TAG, "Wrong provider flag")
            startAuthActivity()
        }

        // Setting Toolbar
        toolbar = toolbar_main as BottomAppBar
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        toolbar.title = "Fire notes"
        setSupportActionBar(toolbar)

        // Create drawer
        createDrawer(userName, userEmail)

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
//        }
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

    private fun createDrawer(name: String?, email: String?) {
        val notesItem = PrimaryDrawerItem()
            .withIdentifier(Constants.DRAWER_NOTES_ITEM)
            .withName("Notes")

        val logOutItem = SecondaryDrawerItem()
            .withIdentifier(Constants.DRAWER_LOG_OUT_ITEM)
            .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                override fun onItemClick(
                    view: View?,
                    position: Int,
                    drawerItem: IDrawerItem<*>
                ): Boolean {
                    Log.d(TAG, "Logging out")
                    logOut()
                    return true
                }
            })
            .withName("Log out")

        val header = AccountHeaderBuilder()
            .withActivity(this)
            .addProfiles(ProfileDrawerItem().withName(name).withEmail(email))
            .withProfileImagesVisible(true)
            .withSelectionListEnabled(false)
            .withDividerBelowHeader(true)
            .build()

        drawer = DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(header)
            .addDrawerItems(notesItem, DividerDrawerItem(), logOutItem)
            .build()
    }

    private fun startAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun logOut() {
        mAuth.signOut()
        when (loginProvider) {
            Constants.LOGIN_PROVIDER_GOOGLE_ACCOUNT -> {
                GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            }
        }
        startAuthActivity()
    }

    private fun openNoteActivity(note: Note?, flag: Int) {
        val intent = Intent(this, NoteActivity::class.java)
        val bundle = Bundle()

        bundle.putInt(Constants.FLAG_NOTE_KEY, flag)

        if (note != null)
            bundle.putParcelable(Constants.NOTE_KEY, note)

        intent.putExtras(bundle)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onItemClicked(note: Note) {
        openNoteActivity(note, Constants.FLAG_EDIT_NOTE)
    }

    override fun onItemRemoved(pos: Int, note: Note) {
        val mainFragment =
            fragmentManager.findFragmentByTag(Constants.MAIN_FRAGMENT_KEY) as MainFragment

        Snackbar.make(toolbar, "Note removed", Snackbar.LENGTH_LONG)
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION)
                        mainFragment.removeNoteWithId(note.id)
                }
            })
            .setAnchorView(fab)
            .setAction("Undo") {
                mainFragment.addNoteBack(pos, note)
            }
            .show()
    }
}
