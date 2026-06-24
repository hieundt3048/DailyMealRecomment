package com.example.dailymealrecomment

import android.content.Intent
<<<<<<< HEAD
import android.net.Uri
=======
>>>>>>> master
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
<<<<<<< HEAD
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailymealrecomment.databinding.ActivityMainBinding
import com.example.dailymealrecomment.model.FoodItem
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val loggedItems = mutableListOf<FoodItem>()
    private lateinit var adapter: FoodLogAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, FoodAnalysisActivity::class.java).apply {
                putExtra("image_uri", it.toString())
            }
            startActivity(intent)
        }
    }
=======
import com.example.dailymealrecomment.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
>>>>>>> master

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
<<<<<<< HEAD

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
=======
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
>>>>>>> master
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

<<<<<<< HEAD
        setupToolbar()
        setupRecyclerView()
        setupDashboardMock()

        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        binding.btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
=======
        binding.btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
>>>>>>> master
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = FoodLogAdapter(loggedItems)
        binding.rvTodayLog.layoutManager = LinearLayoutManager(this)
        binding.rvTodayLog.adapter = adapter

        // Mock items
        loggedItems.add(FoodItem("Greek Yogurt", 150, 150, 15.0, 10.0, 2.0))
        loggedItems.add(FoodItem("Almonds", 30, 170, 6.0, 6.0, 14.0))
        adapter.notifyDataSetChanged()
    }

    private fun setupDashboardMock() {
        // Mock values for the circular indicator
        binding.progressCalories.progress = 40
        binding.tvCaloriesRemaining.text = "1420"
        
        binding.tvProteinValue.text = "21/120g"
        binding.tvCarbsValue.text = "16/250g"
        binding.tvFatValue.text = "16/60g"
    }
}
