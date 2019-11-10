package com.corrot.firenotes.ui

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.corrot.firenotes.R
import com.corrot.firenotes.utils.hideKeyboard
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_sign_up.view.*
import java.util.*
import kotlin.concurrent.schedule

class SignUpFragment : Fragment() {
    companion object {
        @JvmField
        val TAG: String = SignUpFragment::class.java.simpleName
    }

    interface SignUpListener {
        fun signedUp()
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var callback: SignUpListener

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var signUpButton: MaterialButton
    private lateinit var shadow: View
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        emailInputLayout = view.til_sign_up_email
        passwordInputLayout = view.til_sign_up_password
        confirmPasswordInputLayout = view.til_sign_up_confirm_password
        signUpButton = view.btn_sign_up
        shadow = view.v_sign_up_shadow
        progressBar = view.pb_sign_up

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

        return view
    }

    fun setSignUpListener(callback: SignUpListener) {
        this.callback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
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
                        Timer("Finish").schedule(1000) {
                            callback.signedUp()
                        }
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
