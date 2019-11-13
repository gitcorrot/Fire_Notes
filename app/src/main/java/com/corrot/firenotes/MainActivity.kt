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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fragments
        fragmentManager = supportFragmentManager
        fragmentContainer = fl_main_fragment_container

        // Firebase
        mAuth = FirebaseAuth.getInstance()

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
                fragment = fragmentManager.findFragmentByTag(Constants.ADD_NOTE_FRAGMENT_KEY)
                if (fragment != null) {
                    (fragment as AddNoteFragment).setAddNoteListener(this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // If user is null (is not logged in) open signUpActivity
        if (mAuth.currentUser == null) {
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } else {
            val user: FirebaseUser = mAuth.currentUser!!
            Log.d(TAG, "LOGGED AS: ${user.email.toString()}")
            Snackbar.make(
                fragmentContainer,
                "Logged in as ${user.email}", Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadMainFragment() {
        var mainFragment = fragmentManager.findFragmentByTag(Constants.MAIN_FRAGMENT_KEY)

        if (mainFragment == null) {
            mainFragment = MainFragment()
            mainFragment.setMainListener(this)
            fragmentManager.beginTransaction()
                .replace(fragmentContainer.id, mainFragment, Constants.MAIN_FRAGMENT_KEY)
                .commit()
        } else {
            (mainFragment as MainFragment).setMainListener(this)
        }
    }

    private fun loadAddNoteFragment() {
        var addNoteFragment = fragmentManager.findFragmentByTag(Constants.ADD_NOTE_FRAGMENT_KEY)

        if (addNoteFragment == null) {
            addNoteFragment = AddNoteFragment()
            addNoteFragment.setAddNoteListener(this)
            fragmentManager.beginTransaction()
                .replace(fragmentContainer.id, addNoteFragment, Constants.ADD_NOTE_FRAGMENT_KEY)
                .commit()
        } else {
            (addNoteFragment as AddNoteFragment).setAddNoteListener(this)
        }
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
}
