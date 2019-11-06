package com.corrot.firenotes

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
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*
import kotlin.concurrent.schedule

class SignUpActivity : AppCompatActivity() {
    companion object {
        @JvmField
        val TAG: String = SignUpActivity::class.java.simpleName
    }

    private lateinit var mAuth: FirebaseAuth

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var signUpButton: MaterialButton
    private lateinit var shadow: View
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        emailInputLayout = til_sign_up_email
        passwordInputLayout = til_sign_up_password
        confirmPasswordInputLayout = til_sign_up_confirm_password
        signUpButton = btn_sign_up
        shadow = v_sign_up_shadow
        progressBar = pb_sign_up

        signUpButton.setOnClickListener {
            val email: String = emailInputLayout.editText!!.text.toString()
            val password: String = passwordInputLayout.editText!!.text.toString()
            val confirmedPassword: String = confirmPasswordInputLayout.editText!!.text.toString()

            it.hideKeyboard()

            if (validateEmailAndPassword(email, password, confirmedPassword)) {
                shadow.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
                signUp(email, password)
            }
        }
    }

    // Returns true if email and password are OK, else returns false.
    private fun validateEmailAndPassword(
        email: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        when {
            email.isEmpty() -> {
                emailInputLayout.error = "Email empty"
                emailInputLayout.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInputLayout.error = "Email is not valid"
                emailInputLayout.requestFocus()
                return false
            }
            else -> emailInputLayout.error = null
        }

        when {
            password.isEmpty() -> {
                passwordInputLayout.error = "Password empty"
                passwordInputLayout.requestFocus()
                return false
            }
            password.length < 6 -> {
                passwordInputLayout.error = "Password should be longer than 6 characters"
                passwordInputLayout.requestFocus()
                return false
            }
            password != confirmedPassword -> {
                passwordInputLayout.error = "Passwords are not the same"
                passwordInputLayout.requestFocus()
                return false
            }
            else -> passwordInputLayout.error = null
        }

        return true
    }

    private fun signUp(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                shadow.visibility = View.GONE
                progressBar.visibility = View.GONE
                when (task.isSuccessful) {
                    true -> {
                        Log.d(TAG, "createUserWithEmail:success")
                        Snackbar.make(
                            signUpButton,
                            "Signed up successfully", Snackbar.LENGTH_SHORT
                        ).show()
                        Timer("Finish").schedule(1000) { finish() }
                    }
                    false -> {
                        Log.d(TAG, "createUserWithEmail:false")
                        task.exception?.let { e ->
                            Snackbar.make(
                                signUpButton,
                                "${e.message}", Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
    }
}
