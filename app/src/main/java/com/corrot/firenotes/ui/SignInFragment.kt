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
import com.google.android.gms.common.SignInButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_sign_in.view.*
import java.util.*
import kotlin.concurrent.schedule


class SignInFragment : Fragment() {
    companion object {
        @JvmField
        val TAG: String = SignInFragment::class.java.simpleName
    }

    interface SignInListener {
        fun done()
        fun signUpWithGoogleClicked()
        fun signUpClicked()
    }

    private lateinit var callback: SignInListener

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var signInButton: MaterialButton
    private lateinit var googleSignInButton: SignInButton
    private lateinit var signUpButton: MaterialButton
    private lateinit var shadow: View
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        emailInputLayout = view.til_sign_in_email
        passwordInputLayout = view.til_sign_in_password
        signInButton = view.btn_sign_in
        googleSignInButton = view.btn_google_sign_in
        signUpButton = view.btn_open_sign_up
        shadow = view.v_sign_in_shadow
        progressBar = view.pb_sign_in

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


        signUpButton.setOnClickListener {
            callback.signUpClicked()
        }

        googleSignInButton.setOnClickListener {
            callback.signUpWithGoogleClicked()
        }

        return view
    }

    fun setSignInListener(callback: SignInListener) {
        this.callback = callback
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
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
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

                        // Wait for user to read snackbar and finish.
                        Timer("Finish").schedule(1000) { callback.done() }
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

}
