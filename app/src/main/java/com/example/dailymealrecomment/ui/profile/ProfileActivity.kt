package com.example.btl.ui.profile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.btl.R
import com.example.btl.data.model.DietType
import com.example.btl.data.model.Goal
import com.google.android.material.chip.ChipGroup
import android.content.Intent
import androidx.activity.viewModels
import com.example.btl.ui.auth.AuthViewModel
import com.example.btl.ui.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {
    private val viewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. Ánh xạ các thành phần giao diện từ file XML
        val edtHeight = findViewById<EditText>(R.id.edtHeight)
        val edtWeight = findViewById<EditText>(R.id.edtWeight)
        val edtAge = findViewById<EditText>(R.id.edtAge) ?: EditText(this).apply { setText("25") } // Phòng trường hợp XML cũ chưa có edtAge
        val rgGender = findViewById<RadioGroup>(R.id.rgGender) ?: RadioGroup(this) // Phòng hờ lỗi layout

        val chipGroupGoal = findViewById<ChipGroup>(R.id.chipGroupGoal)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        // 2. Lắng nghe dữ liệu (Calo) từ ViewModel trả về
        viewModel.targetCalories.observe(this) { calories ->
            // Khi ViewModel tính toán xong, cập nhật ngay lên giao diện
            tvResult.text = "$calories Kcal"
            Toast.makeText(this, "AI đã tối ưu thực đơn cho bạn, xem ngay!", Toast.LENGTH_SHORT).show()
        }

        // 3. Xử lý sự kiện khi người dùng nhấn nút "TÍNH TOÁN CALO BẰNG AI"
        val chipGroupDiet = findViewById<ChipGroup>(R.id.chipGroupDiet)

        btnCalculate.setOnClickListener {
            val heightStr = edtHeight.text.toString()
            val weightStr = edtWeight.text.toString()
            val ageStr = edtAge.text.toString()

            if (heightStr.isEmpty() || weightStr.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val height = heightStr.toDouble()
            val weight = weightStr.toDouble()
            val age = ageStr.toInt()

            // Lấy giới tính
            val isMale = rgGender.checkedRadioButtonId == R.id.rbMale

            // Lấy mục tiêu cơ thể từ ChipGroup
            val selectedGoal = when (chipGroupGoal.checkedChipId) {
                R.id.chipLose -> Goal.LOSE_WEIGHT
                R.id.chipGain -> Goal.GAIN_WEIGHT
                else -> Goal.MAINTAIN_WEIGHT
            }

            val selectedDiet = when (chipGroupDiet.checkedChipId) {
                R.id.chipVegan -> DietType.VEGAN     // Chọn Thuần chay
                else -> DietType.NORMAL             // Chọn Bình thường
            }

            // Đẩy toàn bộ dữ liệu thực tế sang ViewModel xử lý
            viewModel.saveUserProfileAndCalculate(height, weight, age, isMale, selectedGoal, selectedDiet)
        }

        val btnSignOut = findViewById<Button>(R.id.btnSignOut)
        btnSignOut.setOnClickListener {
            authViewModel.signOut(this)
        }

        authViewModel.logoutResult.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show()
                // Quay về màn hình đăng nhập
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}