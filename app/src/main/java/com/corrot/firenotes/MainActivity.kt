package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
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

        // On create activity load main fragment
        val mainFragment = MainFragment()
        mainFragment.setMainListener(this)
        fragmentManager.beginTransaction()
            .add(fragmentContainer.id, mainFragment)
            .commit()
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
        val mainFragment = MainFragment()
        mainFragment.setMainListener(this)
        fragmentManager.beginTransaction()
            .replace(fragmentContainer.id, mainFragment)
            .commit()
    }

    override fun fabClicked() {
        Toast.makeText(this, "aaa", Toast.LENGTH_SHORT).show()
        val addNoteFragment = AddNoteFragment()
        addNoteFragment.setAddNoteListener(this)
        fragmentManager.beginTransaction()
            .replace(fragmentContainer.id, addNoteFragment)
            .commit()
    }

    override fun noteAdded() {
        loadMainFragment()
    }

    override fun backClicked() {
        loadMainFragment()
    }
}
