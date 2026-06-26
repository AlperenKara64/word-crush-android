package com.example.yazlab4

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class DictionaryManager(context: Context) {
    private val wordSet = HashSet<String>()
    private val prefixSet = HashSet<String>()

    init {
        try {
            val inputStream = context.assets.open("words_tr.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                val word = line.trim().uppercase(Locale("tr", "TR"))
                if (word.length >= 3) {
                    wordSet.add(word)
                    // Prefiksleri ekle (Optimizasyon için)
                    for (i in 1..word.length) {
                        prefixSet.add(word.substring(0, i))
                    }
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isWordValid(word: String): Boolean {
        return wordSet.contains(word.uppercase(Locale("tr", "TR")))
    }

    fun isPrefixValid(prefix: String): Boolean {
        return prefixSet.contains(prefix.uppercase(Locale("tr", "TR")))
    }
}