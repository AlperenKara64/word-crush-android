package com.example.yazlab4

import kotlin.random.Random

class LetterGenerator {
    
    // Türkçe harf frekansları (Yönergeye uygun ağırlıklandırma)
    private val highFreq = "AEİLRN".toCharArray()
    private val midFreq = "KMT SYD".replace(" ", "").toCharArray()
    private val lowFreq = "JĞFV".toCharArray()
    private val others = "BCÇGHIİOÖPRŞTUÜVYZ".filterNot { "AEİLRNKMTSYDJĞFV".contains(it) }.toCharArray()

    fun getRandomLetter(): Char {
        val rand = Random.nextInt(100)
        return when {
            rand < 50 -> highFreq.random() // %50 olasılıkla en sık harfler
            rand < 85 -> midFreq.random()  // %35 olasılıkla orta harfler
            rand < 95 -> others.random()   // %10 olasılıkla diğerleri
            else -> lowFreq.random()       // %5 olasılıkla nadir harfler
        }
    }

    fun generateGrid(size: Int): Array<CharArray> {
        return Array(size) { CharArray(size) { getRandomLetter() } }
    }
}