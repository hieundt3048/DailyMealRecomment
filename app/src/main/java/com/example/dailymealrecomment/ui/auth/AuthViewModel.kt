package com.example.btl.ui.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> = _logoutResult

    /**
     * Hàm gọi hộp thoại Đăng nhập Google thực tế
     */
    fun signInWithGoogle(context: Context) {
        val credentialManager = CredentialManager.create(context)

        // CHÚ Ý: Thay bằng Web Client ID thực tế từ Google Cloud Console của bạn
        val serverClientId = "577908919305-23p4rm796p0pa237ak2d93ppufl50b6u.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // false: Cho phép chọn mọi tài khoản Google có trên máy
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true) // Tự động đăng nhập nếu chỉ có 1 tài khoản duy nhất
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Chạy bất đồng bộ (Coroutine) để không làm đơ giao diện
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    val idToken = credential.idToken
                    // Đăng nhập thành công! idToken này dùng để gửi lên Backend hoặc Firebase của bạn
                    _loginResult.value = Result.success(credential.displayName ?: "Người dùng Google")
                } else {
                    _loginResult.value = Result.failure(Exception("Loại xác thực không hợp lệ"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    /**
     * Hàm Đăng xuất xóa trạng thái đăng nhập trên thiết bị
     */
    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        viewModelScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                _logoutResult.value = true
            } catch (e: Exception) {
                _logoutResult.value = false
            }
        }
    }
}