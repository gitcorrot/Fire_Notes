package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
    private lateinit var signUpButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()

        emailInputLayout = til_sign_in_email
        passwordInputLayout = til_sign_in_password
        signInButton = btn_sign_in
        signUpButton = btn_open_sign_up

        signInButton.setOnClickListener {
            val email: String = emailInputLayout.editText!!.text.toString()
            val password: String = passwordInputLayout.editText!!.text.toString()

            if (validateEmailAndPassword(email, password)) {
                signIn(email, password)
            }
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        // If user is not null (is logged in) open mainActivity
        mAuth.currentUser?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Returns true if email and password are OK, else returns false.
    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                emailInputLayout.error = "Email empty!"
                emailInputLayout.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInputLayout.error = "Email is not valid!"
                emailInputLayout.requestFocus()
                return false
            }
        }

        when {
            password.isEmpty() -> {
                passwordInputLayout.error = "Password empty!"
                passwordInputLayout.requestFocus()
                return false
            }
            password.length < 6 -> {
                passwordInputLayout.error = "Password should be longer than 6 characters!"
                passwordInputLayout.requestFocus()
                return false
            }
        }

        return true
    }

    private fun signIn(email: String, password: String) {
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
