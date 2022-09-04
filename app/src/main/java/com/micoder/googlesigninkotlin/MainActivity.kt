package com.micoder.googlesigninkotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.micoder.googlesigninkotlin.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityMainBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    // constants
    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // configure the google signIn
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // the error will resolved when build first time
            .requestEmail() // we only need email from google account
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        // Google SignIn Button, Click to begin Google SignIn
        binding.googleSignInBtn.setOnClickListener {
/*            // begin Google SignIn
            Log.d(TAG, "onCreate: begin Google SignIn")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)*/

            // clearing previous signin caches
            googleSignInClient.signOut()
            //getting the google signin intent
            val signInIntent = googleSignInClient.signInIntent
            //starting the activity for result
            startActivityForResult(signInIntent, RC_SIGN_IN)

        }

    }

    private fun checkUser() {

        // check if user is already logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            // user is already logged in
            // start profile activity
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
            finish()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google SignIn success, now auth with firebase
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }
            catch (e: Exception) {
                // failed Google SignIn
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")

        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                // login success
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Logged In")

                // get loggedIn user
                val firebaseUser = firebaseAuth.currentUser
                // get user info
                val uid = firebaseUser!!.uid
                val email = firebaseUser.email

                Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid: $uid")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")

                // check if user is new or existing
                if (authResult.additionalUserInfo!!.isNewUser) {
                    // user is new -Account Created
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account Created... \n$email")
                    Toast.makeText(this@MainActivity, "Account Created... \n$email", Toast.LENGTH_SHORT).show()
                }
                else {
                    // existing user - loggedIn 
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User... \n$email")
                    Toast.makeText(this@MainActivity, "LoggedIn... \n$email", Toast.LENGTH_SHORT).show()
                }

                // start profile activity
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                finish()

            }
            .addOnFailureListener { e ->
                // login failed 
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Loggin failed due to ${e.message}")
                Toast.makeText(this@MainActivity, "Loggin failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}