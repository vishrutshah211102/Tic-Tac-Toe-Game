package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivityGameplayBinding
import kotlin.random.Random
import android.graphics.Color
import android.view.View
import android.widget.TextView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.appcompat.widget.Toolbar

class GameplayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameplayBinding
    private var currentPlayer = "X"
    private val board = Array(3) { arrayOfNulls<String>(3) }
    private var difficultyLevel: String = "Easy"
    private var minimaxCallCount = 0
    private lateinit var dbHelper: DatabaseHelper

    companion object {
        const val REQUEST_CODE_SETTINGS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // default
        dbHelper = DatabaseHelper(this)
        difficultyLevel = "Easy"

        // Add the back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        start()

        //goto settings
        binding.buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("DIFFICULTY_LEVEL", difficultyLevel)
            startActivityForResult(intent, REQUEST_CODE_SETTINGS)
        }
    }
    private fun vsHuman(){
        val buttons = arrayOf(
            arrayOf(binding.button00, binding.button01, binding.button02),
            arrayOf(binding.button10, binding.button11, binding.button12),
            arrayOf(binding.button20, binding.button21, binding.button22)
        )
        for (i in buttons.indices) {
            for (j in buttons[i].indices) {
                buttons[i][j].setOnClickListener {
                    if (board[i][j] == null) {
                        board[i][j] = currentPlayer
                        buttons[i][j].text = currentPlayer
                        buttons[i][j].setTextColor(if (currentPlayer == "X") Color.RED else Color.BLUE)
                        val result = checkWin()
                        if (result != null) {
                            resetboard(buttons, result)
                            logCurrentDetails(result)
                        } else {
                            currentPlayer = if (currentPlayer == "X") "O" else "X"
                            this.vsHuman()
                        }
                    }
                }
            }
        }
    }
    private fun start() {
        val buttons = arrayOf(
            arrayOf(binding.button00, binding.button01, binding.button02),
            arrayOf(binding.button10, binding.button11, binding.button12),
            arrayOf(binding.button20, binding.button21, binding.button22)
        )

        for (i in buttons.indices) {
            for (j in buttons[i].indices) {
                buttons[i][j].setOnClickListener {
                    if (board[i][j] == null) {
                        board[i][j] = currentPlayer
                        buttons[i][j].text = currentPlayer
                        buttons[i][j].setTextColor(if (currentPlayer == "X") Color.RED else Color.BLUE)
                        val result = checkWin()
                        if (result != null) {
                            resetboard(buttons, result)
                            logCurrentDetails(result)
                        } else {
                            currentPlayer = if (currentPlayer == "X") "O" else "X"
                            if (currentPlayer == "O") {
                                computerMove(buttons)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SETTINGS && resultCode == RESULT_OK) {
            data?.let {
                // settings difficult change
                val newDifficulty = it.getStringExtra("NEW_DIFFICULTY_LEVEL")
                if (newDifficulty != null) {
                    difficultyLevel = newDifficulty
                    if(difficultyLevel=="vsHuman"){
                        showSlidingNotification("Difficulty set to $difficultyLevel")
                        vsHuman()
                    }
//                    else if(difficultyLevel=="vsHumanAnotherDevice"){
//                        val intent = Intent(this, AnotherDeviceActivity::class.java)
//                        startActivity(intent)
//                    }
                    else{
                        showSlidingNotification("Difficulty set to $difficultyLevel")
                    }
                }

                // reset done or not
                if (it.getBooleanExtra("RESET_GAME", false)) {
                    resetboard(getButtonGrid(),"RESET")
                    showSlidingNotification("Game Reset")
                }
            }
        }
    }

    private fun showSlidingNotification(message: String) {
        val notificationText: TextView = findViewById(R.id.notificationText)
        notificationText.text = message
        notificationText.visibility = View.VISIBLE

        // Animate sliding down
        notificationText.translationY = -notificationText.height.toFloat() // Start above the screen
        notificationText.animate()
            .translationY(0f) // Move to its original position
            .setDuration(300) // Duration for the slide-down effect
            .withEndAction {
                notificationText.postDelayed({
                    // Animate sliding up out of the screen
                    notificationText.animate()
                        .translationY(-notificationText.height.toFloat())
                        .setDuration(300)
                        .withEndAction {
                            notificationText.visibility = View.GONE // Hide the view after animation
                        }
                }, 2500) // Duration to keep the notification visible
            }
    }

    private fun getButtonGrid(): Array<Array<Button>> {
        return arrayOf(
            arrayOf(binding.button00, binding.button01, binding.button02),
            arrayOf(binding.button10, binding.button11, binding.button12),
            arrayOf(binding.button20, binding.button21, binding.button22)
        )
    }

    private fun computerMove(buttons: Array<Array<Button>>) {
        Log.d("DifficultyLevel;", "Difficulty level: $difficultyLevel")
        val bestMove: Pair<Int, Int>? = when (difficultyLevel) {
            "Easy" -> Random()
            "Medium" -> {
                if (Random.nextFloat() > 0.5) Random() else bestMove()
            }
            "Hard" -> bestMove()
            else -> bestMove()
        }

        if (bestMove != null) {
            board[bestMove.first][bestMove.second] = currentPlayer
            buttons[bestMove.first][bestMove.second].text = currentPlayer
            buttons[bestMove.first][bestMove.second].setTextColor(Color.BLUE)
            val result = checkWin()
            if (result != null) {
                resetboard(buttons, result)
                logCurrentDetails(result)
            } else {
                currentPlayer = "X"
            }
        }
    }

    private fun Random(): Pair<Int, Int>? {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j] == null) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }
        return if (emptyCells.isNotEmpty()) emptyCells[Random.nextInt(emptyCells.size)] else null
    }

    private fun bestMove(): Pair<Int, Int>? {
        var bestValue = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null
        var alpha = Int.MIN_VALUE
        var beta = Int.MAX_VALUE

        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j] == null) {
                    board[i][j] = "O"
                    val moveValue = minimaxalgo(0, false, alpha, beta)
                    board[i][j] = null

                    if (moveValue > bestValue) {
                        bestValue = moveValue
                        bestMove = Pair(i, j)
                    }
                }
            }
        }

        Log.d("MinimaxCount", "Minimax possibilities calculated: $minimaxCallCount")
        minimaxCallCount = 0
        return bestMove
    }

    private fun minimaxalgo(depth: Int, isMaximizing: Boolean, alpha: Int = Int.MIN_VALUE, beta: Int = Int.MAX_VALUE): Int {
        minimaxCallCount++
        val score = check()

        if (score == 10) return score - depth
        if (score == -10) return score + depth
        if (!movesleft()) return 0

        var localAlpha = alpha
        var localBeta = beta

        if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in board.indices) {
                for (j in board[i].indices) {
                    if (board[i][j] == null) {
                        board[i][j] = "O"
                        best = maxOf(best, minimaxalgo(depth + 1, false, localAlpha, localBeta))
                        board[i][j] = null
                        localAlpha = maxOf(localAlpha, best)

                        if (localBeta <= localAlpha) {
                            return best//pruning
                        }
                    }
                }
            }
            return best
        } else {
            var best = Int.MAX_VALUE
            for (i in board.indices) {
                for (j in board[i].indices) {
                    if (board[i][j] == null) {
                        board[i][j] = "X"
                        best = minOf(best, minimaxalgo(depth + 1, true, localAlpha, localBeta))
                        board[i][j] = null
                        localBeta = minOf(localBeta, best)

                        if (localBeta <= localAlpha) {
                            return best //pruning
                        }
                    }
                }
            }
            return best
        }
    }

    private fun check(): Int {
        for (i in 0..2) {
            if (board[i][0] != null && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return if (board[i][0] == "O") 10 else -10
            }
            if (board[0][i] != null && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return if (board[0][i] == "O") 10 else -10
            }
        }
        if (board[0][0] != null && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return if (board[0][0] == "O") 10 else -10
        }
        if (board[0][2] != null && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return if (board[0][2] == "O") 10 else -10
        }
        return 0
    }

    private fun movesleft(): Boolean {
        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j] == null) return true
            }
        }
        return false
    }

    private fun resetboard(buttons: Array<Array<Button>>, result: String?) {
        if (result != null) {
            showSlidingNotification(result)
        }

        for (i in board.indices) {
            for (j in board[i].indices) {
                board[i][j] = null
                buttons[i][j].text = ""
            }
        }
        currentPlayer = "X"
        minimaxCallCount = 0
    }

    private fun checkWin(): String? {
        val winner = if (currentPlayer == "X") "Human" else "Computer"

        for (i in 0..2) {
            if (board[i][0] != null && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return "Row ${i + 1} win by $winner"
            }
        }

        for (i in 0..2) {
            if (board[0][i] != null && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return "Column ${i + 1} win by $winner"
            }
        }

        if (board[0][0] != null && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return "Diagonal win by $winner"
        }
        if (board[0][2] != null && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return "Diagonal win by $winner"
        }

        if (!movesleft()) {
            return "Draw"
        }

        return null
    }
    private fun logCurrentDetails(result: String?) {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val winner = when {
            result == "Draw" -> "Draw"
            result?.contains("Human", ignoreCase = true) == true -> "Human"
            else -> "Computer"
        }

        // Insert in db
        dbHelper.insertGameData(currentDate, winner, difficultyLevel)

        // Verify by logging
        Log.d("DB", "Inserted into database -> Date: $currentDate, Winner: $winner, Difficulty: $difficultyLevel")
    }
}