package com.corrot.firenotes

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = SignInActivity::class.java.simpleName
    }

    private lateinit var mAuth: FirebaseAuth

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var signInButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()

        emailInputLayout = til_sing_in_email
        passwordInputLayout = til_sing_in_password
        signInButton = btn_sign_in
    }

    override fun onStart() {
        super.onStart()

        // If user is not null (is logged in) open mainActivity
        mAuth.currentUser?.let {
            finish()
        }
    }

    fun signIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                when (it.isSuccessful) {
                    true -> {
                        Log.d(TAG, "signInWithEmail:success")
                        val user: FirebaseUser = mAuth.currentUser!!
                        Snackbar.make(
                            signInButton,
                            "Logged in as ${user.displayName}", Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    false -> {
                        // TODO: handle it
                        Snackbar.make(
                            signInButton,
                            "Failed to log in", Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }
}
