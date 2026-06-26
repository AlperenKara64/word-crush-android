package com.example.yazlab4

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView

enum class PowerType {
    NONE, ROW, AREA, COLUMN, MEGA
}

class LetterView(context: Context, val row: Int, var col: Int) : AppCompatTextView(context) {
    
    var powerType: PowerType = PowerType.NONE
        set(value) {
            field = value
            updateAppearance()
        }

    var isSelectedState = false
        set(value) {
            field = value
            updateAppearance()
        }

    init {
        gravity = Gravity.CENTER
        textSize = 22f
        setTextColor(Color.BLACK)
        updateAppearance()
    }

    private fun updateAppearance() {
        if (isSelectedState) {
            setBackgroundResource(R.drawable.tile_selected)
        } else {
            setBackgroundResource(R.drawable.tile_background)
        }

        // Güç simgelerini metne ekleyelim
        val baseChar = text.toString().filter { it.isLetter() }
        if (baseChar.isNotEmpty()) {
            val icon = when (powerType) {
                PowerType.NONE -> ""
                PowerType.ROW -> " ⇆"
                PowerType.AREA -> " ✹"
                PowerType.COLUMN -> " ⇅"
                PowerType.MEGA -> " ✪"
            }
            // Sadece bir kez eklemek için kontrol (init'te veya değişimde)
            // Not: text set edildiğinde bu çağrılacak. Sonsuz döngüden kaçınmak lazım.
            // Bu yüzden text değişimini burada değil, dışarıda yapmak daha sağlıklı.
        }
    }

    fun setLetter(char: String, type: PowerType = PowerType.NONE) {
        powerType = type
        val icon = when (type) {
            PowerType.NONE -> ""
            PowerType.ROW -> " ⇆"
            PowerType.AREA -> " ✹"
            PowerType.COLUMN -> " ⇅"
            PowerType.MEGA -> " ✪"
        }
        text = "$char$icon"
    }
}