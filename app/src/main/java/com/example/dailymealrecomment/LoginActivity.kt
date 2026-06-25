package com.example.dailymealrecomment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.data.StartDestination
import com.example.dailymealrecomment.data.StartDestinationResolver
import com.example.dailymealrecomment.data.model.DietType
import com.example.dailymealrecomment.data.model.Goal
import com.example.dailymealrecomment.data.model.UserProfile
import com.example.dailymealrecomment.databinding.ActivityLoginBinding
import com.example.dailymealrecomment.ui.profile.ProfileActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var sessionPreferences: SessionPreferences
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val smokeTestMode: Boolean
        get() = BuildConfig.DEBUG && intent.getBooleanExtra(EXTRA_SMOKE_TEST, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        credentialManager = CredentialManager.create(this)
        sessionPreferences = SessionPreferences(this)

        if (!smokeTestMode) {
            when (
                StartDestinationResolver.resolve(
                    hasFirebaseUser = firebaseAuth.currentUser != null,
                    isProfileCompleted = sessionPreferences.isProfileCompleted,
                )
            ) {
                StartDestination.MAIN -> {
                    openMain()
                    return
                }
                StartDestination.PROFILE_LOOKUP -> {
                    routeAuthenticatedUser()
                    return
                }
                StartDestination.LOGIN -> Unit
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            if (smokeTestMode) openProfile() else signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        setLoading(true)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(googleServerClientId())
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            runCatching {
                credentialManager.getCredential(this@LoginActivity, request).credential
            }.onSuccess { credential ->
                if (
                    credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    authenticateWithFirebase(googleCredential.idToken)
                } else {
                    setLoading(false)
                    showError(getString(R.string.invalid_google_credential))
                }
            }.onFailure { error ->
                setLoading(false)
                showError(describeGoogleSignInError(error))
            }
        }
    }

    private fun googleServerClientId(): String {
        val generatedClientIdRes = resources.getIdentifier(
            "default_web_client_id",
            "string",
            packageName,
        )
        return if (generatedClientIdRes != 0) {
            getString(generatedClientIdRes)
        } else {
            getString(R.string.google_web_client_id)
        }
    }

    private fun describeGoogleSignInError(error: Throwable): String {
        val rawMessage = error.localizedMessage.orEmpty()
        return when {
            rawMessage.contains("Developer console is not set up correctly", ignoreCase = true) ||
                rawMessage.contains("28444", ignoreCase = true) -> {
                getString(R.string.google_console_setup_error)
            }
            else -> rawMessage.ifBlank { getString(R.string.google_sign_in_failed) }
        }
    }

    private fun authenticateWithFirebase(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    routeAuthenticatedUser()
                } else {
                    setLoading(false)
                    showError(task.exception?.localizedMessage ?: getString(R.string.firebase_sign_in_failed))
                }
            }
    }

    private fun routeAuthenticatedUser() {
        if (sessionPreferences.isProfileCompleted) {
            openMain()
            return
        }
        val user = firebaseAuth.currentUser
        if (user == null) {
            setLoading(false)
            return
        }

        setLoading(true)
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.getBoolean("profileCompleted") == true) {
                    cacheRemoteProfile(document.data.orEmpty())
                    openMain()
                } else {
                    openProfile()
                }
            }
            .addOnFailureListener {
                if (sessionPreferences.isProfileCompleted) openMain() else openProfile()
            }
    }

    private fun cacheRemoteProfile(data: Map<String, Any>) {
        val goal = runCatching {
            Goal.valueOf(data["goal"] as? String ?: Goal.MAINTAIN_WEIGHT.name)
        }.getOrDefault(Goal.MAINTAIN_WEIGHT)
        val diet = runCatching {
            DietType.valueOf(data["dietType"] as? String ?: DietType.NORMAL.name)
        }.getOrDefault(DietType.NORMAL)
        val profile = UserProfile(
            heightCm = (data["heightCm"] as? Number)?.toDouble() ?: 0.0,
            weightKg = (data["weightKg"] as? Number)?.toDouble() ?: 0.0,
            age = (data["age"] as? Number)?.toInt() ?: 0,
            isMale = data["isMale"] as? Boolean ?: true,
            goal = goal,
            dietType = diet,
            activityLevel = (data["activityLevel"] as? Number)?.toDouble() ?: 1.2,
        )
        val target = (data["dailyCalorieTarget"] as? Number)?.toInt()
            ?: SessionPreferences.DEFAULT_CALORIE_TARGET
        sessionPreferences.saveProfile(profile, target)
    }

    private fun openProfile() {
        startActivity(Intent(this, ProfileActivity::class.java).apply {
            putExtra(EXTRA_SMOKE_TEST, smokeTestMode)
        })
        finish()
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnGoogleSignIn.isEnabled = !isLoading
        binding.tvStatus.text = if (isLoading) getString(R.string.checking_account) else ""
    }

    private fun showError(message: String) {
        binding.tvStatus.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_SMOKE_TEST = "com.example.dailymealrecomment.SMOKE_TEST"
    }
}
