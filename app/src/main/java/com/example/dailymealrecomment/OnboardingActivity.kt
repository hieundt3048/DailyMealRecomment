package com.example.dailymealrecomment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dailymealrecomment.databinding.ActivityOnboardingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnNext.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        val height = binding.etHeight.text.toString().toDoubleOrNull() ?: 0.0
        val weight = binding.etWeight.text.toString().toDoubleOrNull() ?: 0.0

        if (height == 0.0 || weight == 0.0) {
            Toast.makeText(this, "Please enter valid metrics", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        
        val updates = mapOf(
            "height" to height,
            "weight" to weight
        )

        firestore.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Setup Complete", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
