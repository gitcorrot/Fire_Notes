package com.corrot.firenotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.corrot.firenotes.ui.SignInFragment
import com.corrot.firenotes.ui.SignUpFragment
import com.corrot.firenotes.utils.Constants
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity(),
    SignInFragment.SignInListener,
    SignUpFragment.SignUpListener {
    companion object {
        const val TAG = "AuthActivity"
    }

    private lateinit var fragmentManager: FragmentManager
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_cient_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        if (FirebaseAuth.getInstance().currentUser == null) {
            // Check if google user is logged in
            val sli = mGoogleSignInClient.silentSignIn()

            if (sli.isSuccessful) {
                // User is logged in
                val account = sli.result
                Log.d(TAG, "User logged via google account, id: ${account?.id}")
                startMainActivity(Constants.LOGIN_PROVIDER_GOOGLE_ACCOUNT)
            } else {
                // User is during logging in
                sli.addOnCompleteListener {
                    if (!handleSignInResult(it)) {
                        // If user is not logged in load sign in fragment
                        fragmentManager = supportFragmentManager
                        fragmentContainer = fl_auth_fragment_container

                        val signInFragment = SignInFragment()
                        signInFragment.setSignInListener(this)

                        val fragmentTransaction = fragmentManager.beginTransaction()
                        fragmentTransaction
                            .add(fragmentContainer.id, signInFragment)
                            .commit()
                    }
                }
            }
        } else {
            // If user is logged in (via email and password) proceed to MainActivity
            Log.d(
                TAG,
                "Logged via email and password, UID: ${FirebaseAuth.getInstance().currentUser!!.uid}"
            )
            startMainActivity(Constants.LOGIN_PROVIDER_EMAIL_PASSWORD)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>): Boolean {
        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "Logged via google account, ID: ${account!!.id}")





            val ref = FirebaseDatabase.getInstance().reference
                .child(Constants.NOTE_KEY)
                .child(account.id!!)

            ref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Log.e(TAG, "ValueEventListener cancelled")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        // User is not in database -> add user
                        FirebaseRepository()
                            .createUser(account.id!!, account.email, account.displayName)
                    }
                }
            })





            startMainActivity(Constants.LOGIN_PROVIDER_GOOGLE_ACCOUNT)
            true
        } catch (e: ApiException) {
            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_REQUIRED)
                Log.e(TAG, "Sign in required")
            else
                Log.e(TAG, "handleSignInResult:failed code = ${e.statusCode}")
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.GOOGLE_SIGN_IN_RESULT_CODE -> {
                if (resultCode == RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
                } else {
                    Log.e(TAG, "Result from request:$requestCode:$resultCode")
                }
            }
            else -> {
                Log.e(TAG, "Unknown requestCode ($requestCode)")
            }
        }
    }


    /**
     *  Function that loads SignUpFragment
     */
    override fun signUpClicked() {
        val signUpFragment = SignUpFragment()
        signUpFragment.setSignUpListener(this)

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction
            .replace(fragmentContainer.id, signUpFragment)
            .commit()
    }

    /**
     * If user signed up successfully (with email and password) start main activity.
     */
    override fun signedUp() {
        startMainActivity(Constants.LOGIN_PROVIDER_EMAIL_PASSWORD)
    }

    /**
     * If user signed in successfully (with email and password) start main activity.
     */
    override fun done() {
        startMainActivity(Constants.LOGIN_PROVIDER_EMAIL_PASSWORD)
    }

    /**
     * Function that starts google sign intent for result
     */
    override fun signUpWithGoogleClicked() {
        val intent = mGoogleSignInClient.signInIntent
        startActivityForResult(intent, Constants.GOOGLE_SIGN_IN_RESULT_CODE)
    }

    /**
     * Function that starts main activity with provider flag as extra Int
     * @param flag login provider flag
     */
    private fun startMainActivity(flag: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.FLAG_LOGIN_PROVIDER, flag)
        startActivity(intent)
        finish()
    }
}
