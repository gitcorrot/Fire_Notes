package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.corrot.firenotes.ui.MainFragment
import com.corrot.firenotes.utils.Constants.Companion.DRAWER_LOG_OUT_ITEM
import com.corrot.firenotes.utils.Constants.Companion.DRAWER_NOTES_ITEM
import com.corrot.firenotes.utils.Constants.Companion.FLAG_ADD_NOTE
import com.corrot.firenotes.utils.Constants.Companion.FLAG_LOGIN_PROVIDER
import com.corrot.firenotes.utils.Constants.Companion.FLAG_NOTE_KEY
import com.corrot.firenotes.utils.Constants.Companion.LOGIN_PROVIDER_GOOGLE_ACCOUNT
import com.corrot.firenotes.utils.Constants.Companion.MAIN_FRAGMENT_KEY
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomappbar.BottomAppBar
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

class MainActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var drawer: Drawer
    private lateinit var toolbar: BottomAppBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase
        mAuth = FirebaseAuth.getInstance()

        // Setting Toolbar
        toolbar = toolbar_main as BottomAppBar
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        toolbar.title = "Fire notes"
        setSupportActionBar(toolbar)

        // Setting Drawer
        createDrawer()

        // Floating Action Button
        fab_main.setOnClickListener {
            val intent = Intent(this, NoteActivity::class.java)
            val bundle = Bundle()
            bundle.putInt(FLAG_NOTE_KEY, FLAG_ADD_NOTE)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        // On first activity creation load mainFragment
        if (savedInstanceState == null) {
            val mainFragment = MainFragment()
            supportFragmentManager.beginTransaction()
                .add(fl_main_fragment_container.id, mainFragment, MAIN_FRAGMENT_KEY)
                .commit()
        }
    }

    private fun createDrawer() {
        val user = mAuth.currentUser!!
        val userName = user.displayName ?: ""
        val userEmail = user.displayName ?: ""

        val notesItem = PrimaryDrawerItem()
            .withIdentifier(DRAWER_NOTES_ITEM)
            .withName("Notes")

        val logOutItem = SecondaryDrawerItem()
            .withIdentifier(DRAWER_LOG_OUT_ITEM)
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
            .addProfiles(ProfileDrawerItem().withName(userName).withEmail(userEmail))
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
        when (intent.getIntExtra(FLAG_LOGIN_PROVIDER, 0)) {
            LOGIN_PROVIDER_GOOGLE_ACCOUNT ->
                GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
        }
        startAuthActivity()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }
}
