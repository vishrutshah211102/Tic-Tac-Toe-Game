package com.example.tictactoe

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_DATE TEXT," +
                "$COLUMN_WINNER TEXT," +
                "$COLUMN_DIFFICULTY TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun getAllGameData(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }

    fun insertGameData(date: String, winner: String, difficulty: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(COLUMN_DATE, date)
        contentValues.put(COLUMN_WINNER, winner)
        contentValues.put(COLUMN_DIFFICULTY, difficulty)

        db.insert(TABLE_NAME, null, contentValues)
    }

    companion object {
        private const val DATABASE_NAME = "PastGamesData"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "past_games"

        private const val COLUMN_ID = "id"
        const val COLUMN_DATE = "date"
        const val COLUMN_WINNER = "winner"
        const val COLUMN_DIFFICULTY = "difficulty"
    }
}
