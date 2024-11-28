package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class PastGames : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout  // Use lateinit to initialize it in onCreate
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_games)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initialize views after setting the layout
        tableLayout = findViewById(R.id.pastgamestable)
        dbHelper = DatabaseHelper(this)

        populateTableWithGameData()
    }

    private fun populateTableWithGameData() {
        val cursor = dbHelper.getAllGameData()

        while (cursor.moveToNext()) {
            val tableRow = TableRow(this)

            val date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE))
            val winner = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WINNER))
            val difficulty = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIFFICULTY))

            val dateTextView = TextView(this).apply {
                text = date
                setPadding(8, 8, 8, 8)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(ContextCompat.getColor(context, R.color.blue_background))  // Use color from colors.xml
                setTextColor(ContextCompat.getColor(context, R.color.white))

            }

            val winnerTextView = TextView(this).apply {
                text = winner
                setPadding(8, 8, 8, 8)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(ContextCompat.getColor(context, R.color.blue_background))  // Use color from colors.xml
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            val difficultyTextView = TextView(this).apply {
                text = difficulty
                setPadding(8, 8, 8, 8)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                gravity = android.view.Gravity.CENTER
                setBackgroundColor(ContextCompat.getColor(context, R.color.blue_background))  // Use color from colors.xml
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            tableRow.addView(dateTextView)
            tableRow.addView(winnerTextView)
            tableRow.addView(difficultyTextView)

            tableLayout.addView(tableRow)
        }

        cursor.close()
    }

}