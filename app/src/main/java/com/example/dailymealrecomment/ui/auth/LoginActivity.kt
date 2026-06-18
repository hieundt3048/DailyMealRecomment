package com.example.btl.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.btl.R
import com.example.btl.ui.profile.ProfileActivity

class LoginActivity : AppCompatActivity() {
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        btnGoogleLogin.setOnClickListener {
            // Gọi hàm mở hộp thoại chọn tài khoản Google thực tế của máy
            viewModel.signInWithGoogle(this)
        }

        // Lắng nghe kết quả trả về
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { userName ->
                Toast.makeText(this, "Xin chào, $userName!", Toast.LENGTH_SHORT).show()
                // Đăng nhập thực tế thành công -> Chuyển sang màn hình nhập chỉ số
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }.onFailure { exception ->
                Toast.makeText(this, "Lỗi đăng nhập: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}