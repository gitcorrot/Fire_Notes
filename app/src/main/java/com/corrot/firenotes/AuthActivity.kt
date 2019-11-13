package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.corrot.firenotes.ui.SignInFragment
import com.corrot.firenotes.ui.SignUpFragment
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity(),
    SignInFragment.SignInListener,
    SignUpFragment.SignUpListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        fragmentManager = supportFragmentManager
        fragmentContainer = fl_auth_fragment_container

        val signInFragment = SignInFragment()
        signInFragment.setSignInListener(this)

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction
            .add(fragmentContainer.id, signInFragment)
            .commit()
    }

    // load SignUpFragment
    override fun signUpClicked() {
        val signUpFragment = SignUpFragment()
        signUpFragment.setSignUpListener(this)

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction
            .replace(fragmentContainer.id, signUpFragment)
            .commit()
    }

    // If user signed up successfully close this activity
    override fun signedUp() {
        close()
    }

    // If user signed in successfully close this activity.
    override fun done() {
        close()
    }

    private fun close() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
