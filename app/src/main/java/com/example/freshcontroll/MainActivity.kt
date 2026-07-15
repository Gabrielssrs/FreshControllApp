package com.example.freshcontroll

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.freshcontroll.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint // Asegúrate de tener este import

@AndroidEntryPoint // <--- ESTA ANOTACIÓN ES LA CLAVE 🔑
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}