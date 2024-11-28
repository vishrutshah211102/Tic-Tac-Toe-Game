package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startGameButton = findViewById<Button>(R.id.button_start_game)
        val viewPastGamesButton=findViewById<Button>(R.id.button_past_game)
        val createRoomButton: Button = findViewById(R.id.createRoomButton)
        val joinRoomButton: Button = findViewById(R.id.joinRoomButton)

        createRoomButton.setOnClickListener {
            val intent = Intent(this, CreateRoomActivity::class.java)
            startActivity(intent)
        }

        joinRoomButton.setOnClickListener {
            val intent = Intent(this, JoinRoomActivity::class.java)
            startActivity(intent)
        }
        startGameButton.setOnClickListener {
            startGame()
        }
        viewPastGamesButton.setOnClickListener {
            viewPastGames()
        }
    }

    private fun startGame() {
        val intent = Intent(this, GameplayActivity::class.java)
        startActivity(intent)
    }
    private fun viewPastGames() {
        val intent = Intent(this, PastGames::class.java)
        startActivity(intent)
    }
}
