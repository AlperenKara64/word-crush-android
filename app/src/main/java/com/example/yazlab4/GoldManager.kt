package com.example.yazlab4

import android.content.Context
import android.content.SharedPreferences

class GoldManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("WordCrushGoldPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_GOLD = "current_gold"
        
        const val KEY_JOKER_FISH = "joker_fish"           // Balık
        const val KEY_JOKER_WHEEL = "joker_wheel"         // Tekerlek
        const val KEY_JOKER_LOLLIPOP = "joker_lollipop"   // Lolipop Kırıcı
        const val KEY_JOKER_SWAP = "joker_swap"           // Serbest Değiştirme
        const val KEY_JOKER_SHUFFLE = "joker_shuffle"     // Harf Karıştırma
        const val KEY_JOKER_PARTY = "joker_party"         // Parti Güçlendiricisi
        
        const val INITIAL_GOLD = 50000 
    }

    init {
        if (!prefs.contains(KEY_GOLD)) {
            prefs.edit().putInt(KEY_GOLD, INITIAL_GOLD).apply()
        }
    }

    fun getGold(): Int = prefs.getInt(KEY_GOLD, INITIAL_GOLD)

    fun spendGold(amount: Int): Boolean {
        val current = getGold()
        return if (current >= amount) {
            prefs.edit().putInt(KEY_GOLD, current - amount).apply()
            true
        } else false
    }

    fun getJokerCount(key: String): Int = prefs.getInt(key, 0)

    fun addJoker(key: String) {
        val current = getJokerCount(key)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun useJoker(key: String): Boolean {
        val current = getJokerCount(key)
        return if (current > 0) {
            prefs.edit().putInt(key, current - 1).apply()
            true
        } else false
    }
}