package com.example.yazlab4

class ScoreCalculator {
    private val letterScores = mapOf(
        'A' to 1, 'B' to 3, 'C' to 4, 'Ç' to 4, 'D' to 3, 'E' to 1, 'F' to 7, 'G' to 5, 'Ğ' to 8, 'H' to 5,
        'I' to 2, 'İ' to 1, 'J' to 10, 'K' to 1, 'L' to 1, 'M' to 2, 'N' to 1, 'O' to 2, 'Ö' to 7, 'P' to 5,
        'R' to 1, 'S' to 2, 'Ş' to 4, 'T' to 1, 'U' to 2, 'Ü' to 3, 'V' to 7, 'Y' to 3, 'Z' to 4
    )

    fun calculateWordScore(word: String): Int {
        var total = 0
        for (char in word.uppercase()) {
            total += letterScores[char] ?: 0
        }
        return total
    }

    fun calculateComboScore(mainWord: String, dictionaryManager: DictionaryManager): Pair<Int, List<String>> {
        val foundSubWords = mutableSetOf<String>()
        
        // Ana kelimenin içindeki tüm anlamlı alt kelimeleri (3+ harf) bul
        // Alt kelimeler ana kelimenin içindeki ardışık harf dizileridir (substring)
        for (len in 3..mainWord.length) {
            for (start in 0..mainWord.length - len) {
                val sub = mainWord.substring(start, start + len).uppercase()
                if (dictionaryManager.isWordValid(sub)) {
                    foundSubWords.add(sub)
                }
            }
        }

        var totalScore = 0
        for (word in foundSubWords) {
            totalScore += calculateWordScore(word)
        }

        return Pair(totalScore, foundSubWords.toList())
    }
}