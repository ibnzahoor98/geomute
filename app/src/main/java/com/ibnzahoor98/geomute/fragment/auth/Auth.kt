package com.ibnzahoor98.geomute.fragment.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.annotations.Nullable
import com.ibnzahoor98.geomute.Main
import com.ibnzahoor98.geomute.R
import com.ibnzahoor98.geomute.Splash
import com.ibnzahoor98.geomute.databinding.FragmentAuthBinding
import com.ibnzahoor98.geomute.helper.TransferGuestToUser


class Auth : Fragment(), OnClickListener {
    private lateinit var binding: FragmentAuthBinding
    private lateinit var auth: FirebaseAuth

    private val TAG = "AuthActivity"

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAuthBinding.inflate(layoutInflater)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAuthBinding.inflate(inflater, container, false)
        binding.connect.setOnClickListener(this)
        configureGoogleSignIn()
        auth = FirebaseAuth.getInstance()


        return binding.root
    }

    private fun configureGoogleSignIn() {

        // Initialize sign in options the client-id is copied form google-services.json file

        // Initialize sign in options the client-id is copied form google-services.json file
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("551430103235-68nsspb51r8o5co43o25bck35jnfjelc.apps.googleusercontent.com")
            .requestEmail()
            .build()

        // Initialize sign in client
        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check condition
        if (requestCode == 100) {
            // When request code is equal to 100 initialize task
            val signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            // check condition
            if (signInAccountTask.isSuccessful) {
                // When google sign in successful initialize string
                val s = "Google sign in successful"

                // Display Toast
                displayToast(s)
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    val googleSignInAccount = signInAccountTask.getResult(
                        ApiException::class.java
                    )
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        val authCredential =
                            GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
                        // Check credential
                        auth.signInWithCredential(authCredential)
                            .addOnCompleteListener(requireActivity(),
                                OnCompleteListener<AuthResult?> { task ->
                                    // Check condition
                                    if (task.isSuccessful) {

                                        displayToast("Firebase authentication successful")
                                        TransferGuestToUser.begin(requireContext())
                                        val intent = Intent(context, Splash::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent)
                                    } else {
                                        // When task is unsuccessful display Toast
                                        displayToast("Authentication Failed :" + task.exception!!.message)
                                    }
                                })
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }else{

                Log.e("eeee", signInAccountTask.exception!!.localizedMessage)

            }
        }
    }

    private fun displayToast(s: String) {

    }


    override fun onClick(p0: View?) {

        if (p0?.id == R.id.connect)
        {
            // Initialize sign in intent
            val intent: Intent = googleSignInClient.getSignInIntent()
            // Start activity for result
            startActivityForResult(intent, 100)
        }
    }
}