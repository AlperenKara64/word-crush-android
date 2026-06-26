package com.example.yazlab4

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScoreActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var rvGameHistory: RecyclerView
    
    private lateinit var tvTotalGames: TextView
    private lateinit var tvHighestScore: TextView
    private lateinit var tvAverageScore: TextView
    private lateinit var tvTotalWords: TextView
    private lateinit var tvLongestWordSummary: TextView
    private lateinit var tvTotalDuration: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        MusicManager.startMusic(this)

        dbHelper = DatabaseHelper(this)
        
        initViews()
        loadScoreData()
    }

    private fun initViews() {
        rvGameHistory = findViewById(R.id.rvGameHistory)
        rvGameHistory.layoutManager = LinearLayoutManager(this)
        
        tvTotalGames = findViewById(R.id.tvTotalGames)
        tvHighestScore = findViewById(R.id.tvHighestScore)
        tvAverageScore = findViewById(R.id.tvAverageScore)
        tvTotalWords = findViewById(R.id.tvTotalWords)
        tvLongestWordSummary = findViewById(R.id.tvLongestWordSummary)
        tvTotalDuration = findViewById(R.id.tvTotalDuration)
    }

    private fun loadScoreData() {
        val records = dbHelper.getAllRecords()
        
        if (records.isNotEmpty()) {
            calculateAndDisplaySummary(records)
            rvGameHistory.adapter = ScoreAdapter(records)
        }
    }

    private fun calculateAndDisplaySummary(records: List<GameRecord>) {
        val totalGames = records.size
        val highestScore = records.maxOf { it.score }
        val averageScore = records.map { it.score }.average().toInt()
        val totalWords = records.sumOf { it.wordCount }
        val longestWord = records.maxByOrNull { it.longestWord.length }?.longestWord ?: "-"
        val totalDurationSeconds = records.sumOf { it.durationSeconds }
        
        val totalHours = totalDurationSeconds / 3600
        val totalMinutes = (totalDurationSeconds % 3600) / 60
        val durationText = if (totalHours > 0) {
            "$totalHours saat $totalMinutes dakika"
        } else {
            "$totalMinutes dakika"
        }

        tvTotalGames.text = getString(R.string.total_games, totalGames)
        tvHighestScore.text = getString(R.string.highest_score, highestScore)
        tvAverageScore.text = getString(R.string.average_score, averageScore)
        tvTotalWords.text = getString(R.string.total_words, totalWords)
        tvLongestWordSummary.text = getString(R.string.longest_word, longestWord)
        tvTotalDuration.text = getString(R.string.total_duration, durationText)
    }
}