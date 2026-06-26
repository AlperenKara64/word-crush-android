package com.example.yazlab4

data class GameRecord(
    val id: Int = 0,
    val date: String,
    val gridSide: Int,
    val score: Int,
    val wordCount: Int,
    val longestWord: String,
    val durationSeconds: Int
) {
    val durationFormatted: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return if (minutes > 0) "${minutes} dk ${seconds} sn" else "${seconds} sn"
        }
        
    val gridLabel: String
        get() = "${gridSide}x${gridSide}"
}