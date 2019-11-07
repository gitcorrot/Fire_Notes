package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        fab = fab_main
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
                fab,
                "Logged in as ${user.email}", Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}
