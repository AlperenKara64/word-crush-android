package com.example.yazlab4

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "WordCrushDB"
        private const val DATABASE_VERSION = 1
        private const val TABLE_GAMES = "games"

        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_GRID_SIDE = "grid_side"
        private const val COLUMN_SCORE = "score"
        private const val COLUMN_WORD_COUNT = "word_count"
        private const val COLUMN_LONGEST_WORD = "longest_word"
        private const val COLUMN_DURATION = "duration"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_GAMES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_GRID_SIDE + " INTEGER,"
                + COLUMN_SCORE + " INTEGER,"
                + COLUMN_WORD_COUNT + " INTEGER,"
                + COLUMN_LONGEST_WORD + " TEXT,"
                + COLUMN_DURATION + " INTEGER" + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GAMES")
        onCreate(db)
    }

    fun addGameRecord(record: GameRecord): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_DATE, record.date)
        values.put(COLUMN_GRID_SIDE, record.gridSide)
        values.put(COLUMN_SCORE, record.score)
        values.put(COLUMN_WORD_COUNT, record.wordCount)
        values.put(COLUMN_LONGEST_WORD, record.longestWord)
        values.put(COLUMN_DURATION, record.durationSeconds)

        val success = db.insert(TABLE_GAMES, null, values)
        db.close()
        return success
    }

    fun getAllRecords(): List<GameRecord> {
        val recordList = mutableListOf<GameRecord>()
        val selectQuery = "SELECT * FROM $TABLE_GAMES ORDER BY $COLUMN_ID DESC"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val record = GameRecord(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    gridSide = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GRID_SIDE)),
                    score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE)),
                    wordCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORD_COUNT)),
                    longestWord = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LONGEST_WORD)),
                    durationSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION))
                )
                recordList.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return recordList
    }
}