package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.corrot.firenotes.utils.hideKeyboard
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import java.util.*
import kotlin.concurrent.schedule

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
    private lateinit var shadow: View
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()

        emailInputLayout = til_sign_in_email
        passwordInputLayout = til_sign_in_password
        signInButton = btn_sign_in
        signUpButton = btn_open_sign_up
        shadow = v_sign_in_shadow
        progressBar = pb_sign_in

        signInButton.setOnClickListener {
            val email: String = emailInputLayout.editText!!.text.toString()
            val password: String = passwordInputLayout.editText!!.text.toString()

            it.hideKeyboard()

            if (validateEmailAndPassword(email, password)) {
                shadow.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                signIn(email, password)
            }
        }

        signUpButton.setOnClickListener { openSignUpActivity() }
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
            else -> emailInputLayout.error = null
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
            else -> passwordInputLayout.error = null
        }

        return true
    }

    private fun signIn(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                shadow.visibility = View.GONE
                progressBar.visibility = View.GONE
                when (task.isSuccessful) {
                    true -> {
                        Log.d(TAG, "signInWithEmail:success")
//                        val user: FirebaseUser = mAuth.currentUser!!
                        Snackbar.make(
                            signInButton,
                            "Logged in successfully", Snackbar.LENGTH_SHORT
                        ).show()
                        Timer("Finish").schedule(1000) { finish() }
                    }
                    false -> {
                        Log.d(TAG, "signInWithEmail:false")
                        task.exception?.let { e ->
                            Snackbar.make(
                                signUpButton,
                                "Failed. ${e.message}", Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
    }


    private fun openSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}
