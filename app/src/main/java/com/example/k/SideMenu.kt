package com.example.k

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.k.databinding.ActivitySideMenuBinding

class SideMenu : AppCompatActivity() {
    private lateinit var binding : ActivitySideMenuBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySideMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.PrefChange.setOnClickListener{
            val intent = Intent(this,PrefChanging::class.java)
            startActivity(intent)
        }
        binding.CalendarV.setOnClickListener{
            val intent = Intent(this,CalendarView::class.java)
            startActivity(intent)
        }
    }
}