package com.example.dailymealrecomment

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dailymealrecomment.data.SessionPreferences
import com.example.dailymealrecomment.data.StartDestination
import com.example.dailymealrecomment.data.StartDestinationResolver
import com.example.dailymealrecomment.data.xampp.XamppAuthSession
import com.example.dailymealrecomment.data.xampp.XamppRepository
import com.example.dailymealrecomment.databinding.ActivityLoginBinding
import com.example.dailymealrecomment.ui.profile.ProfileActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionPreferences: SessionPreferences
    private val xamppRepository by lazy { XamppRepository() }
    private var isRegisterMode = false

    private val smokeTestMode: Boolean
        get() = BuildConfig.DEBUG && intent.getBooleanExtra(EXTRA_SMOKE_TEST, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionPreferences = SessionPreferences(this)

        if (!smokeTestMode) {
            when (
                StartDestinationResolver.resolve(
                    hasAuthenticatedSession = sessionPreferences.isLoggedIn,
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

        binding.btnAuthPrimary.setOnClickListener {
            if (smokeTestMode) openProfile() else submitAuthForm()
        }
        binding.btnAuthModeToggle.setOnClickListener {
            isRegisterMode = !isRegisterMode
            updateAuthModeUi()
        }
        updateAuthModeUi()
    }

    private fun submitAuthForm() {
        clearErrors()
        val name = binding.edtName.text?.toString()?.trim().orEmpty()
        val email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        val password = binding.edtPassword.text?.toString().orEmpty()

        if (isRegisterMode && name.length < MIN_NAME_LENGTH) {
            binding.inputName.error = getString(R.string.auth_invalid_name)
            binding.edtName.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputEmail.error = getString(R.string.auth_invalid_email)
            binding.edtEmail.requestFocus()
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            binding.inputPassword.error = getString(R.string.auth_invalid_password)
            binding.edtPassword.requestFocus()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            runCatching {
                if (isRegisterMode) {
                    xamppRepository.register(name = name, email = email, password = password)
                } else {
                    xamppRepository.login(email = email, password = password)
                }
            }.onSuccess { session ->
                saveSession(session)
                routeAfterAuth(session)
            }.onFailure { error ->
                setLoading(false)
                showError(error.localizedMessage ?: getString(R.string.auth_network_error))
            }
        }
    }

    private fun routeAuthenticatedUser() {
        val token = sessionPreferences.authToken
        if (token.isNullOrBlank()) {
            setLoading(false)
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            runCatching { xamppRepository.fetchProfile(token) }
                .onSuccess { profile ->
                    if (profile != null) {
                        sessionPreferences.saveProfile(profile.profile, profile.dailyCalorieTarget)
                        openMain()
                    } else {
                        openProfile()
                    }
                }
                .onFailure {
                    if (sessionPreferences.isProfileCompleted) {
                        openMain()
                    } else {
                        openProfile()
                    }
                }
        }
    }

    private fun saveSession(session: XamppAuthSession) {
        sessionPreferences.saveSession(
            token = session.token,
            userId = session.user.id,
            name = session.user.name,
            email = session.user.email,
            profileCompleted = session.user.profileCompleted,
        )
        session.profile?.let { profile ->
            sessionPreferences.saveProfile(profile.profile, profile.dailyCalorieTarget)
        }
    }

    private fun routeAfterAuth(session: XamppAuthSession) {
        val hasProfile = session.user.profileCompleted || session.profile != null
        if (hasProfile) openMain() else openProfile()
    }

    private fun updateAuthModeUi() {
        binding.inputName.visibility = if (isRegisterMode) View.VISIBLE else View.GONE
        binding.tvAuthTitle.setText(
            if (isRegisterMode) R.string.auth_register_title else R.string.auth_login_title,
        )
        binding.tvAuthMessage.setText(
            if (isRegisterMode) R.string.auth_register_message else R.string.auth_login_message,
        )
        binding.btnAuthPrimary.setText(
            if (isRegisterMode) R.string.auth_register_button else R.string.auth_login_button,
        )
        binding.btnAuthModeToggle.setText(
            if (isRegisterMode) R.string.auth_go_login else R.string.auth_go_register,
        )
        clearErrors()
        binding.tvStatus.text = ""
    }

    private fun clearErrors() {
        binding.inputName.error = null
        binding.inputEmail.error = null
        binding.inputPassword.error = null
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
        binding.btnAuthPrimary.isEnabled = !isLoading
        binding.btnAuthModeToggle.isEnabled = !isLoading
        binding.inputName.isEnabled = !isLoading
        binding.inputEmail.isEnabled = !isLoading
        binding.inputPassword.isEnabled = !isLoading
        binding.tvStatus.text = if (isLoading) getString(R.string.checking_account) else ""
    }

    private fun showError(message: String) {
        binding.tvStatus.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_SMOKE_TEST = "com.example.dailymealrecomment.SMOKE_TEST"
        private const val MIN_NAME_LENGTH = 2
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
