package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var difficultyLevel: String = "Easy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Populate Spinner with difficulty options
        val difficultyOptions = arrayOf("Easy", "Medium", "Hard","vsHuman","vsHumanAnotherDevice")
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_layout, difficultyOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_layout)  // Set the same layout for dropdown
        binding.spinnerDifficulty.adapter = adapter

        // Get difficulty level from Intent or default
        difficultyLevel = intent.getStringExtra("DIFFICULTY_LEVEL") ?: "Easy"
        val spinnerPosition = difficultyOptions.indexOf(difficultyLevel)
        binding.spinnerDifficulty.setSelection(spinnerPosition)

        // Set save button to pass the difficulty setting back
        binding.buttonSave.setOnClickListener {
            val newDifficulty = binding.spinnerDifficulty.selectedItem.toString()
            val resultIntent = Intent().apply {
                putExtra("NEW_DIFFICULTY_LEVEL", newDifficulty)
            }
            setResult(RESULT_OK, resultIntent)
            finish() // Return to GameplayActivity
        }

        // Set reset button to reset the game
        binding.buttonReset.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("RESET_GAME", true)
            }
            setResult(RESULT_OK, resultIntent)
            finish() // Return to GameplayActivity
        }
    }
}
