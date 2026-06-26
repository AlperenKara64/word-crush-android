package com.example.yazlab4

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MarketActivity : AppCompatActivity() {

    private lateinit var goldManager: GoldManager
    private lateinit var tvGoldAmount: TextView
    private lateinit var rvMarketItems: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)

        MusicManager.startMusic(this)

        goldManager = GoldManager(this)
        
        tvGoldAmount = findViewById(R.id.tvGoldAmount)
        rvMarketItems = findViewById(R.id.rvMarketItems)
        
        updateGoldDisplay()
        setupRecyclerView()
    }

    private fun updateGoldDisplay() {
        tvGoldAmount.text = goldManager.getGold().toString()
    }

    private fun setupRecyclerView() {
        val jokers = listOf(
            Joker(
                "fish",
                getString(R.string.joker_fish_name),
                getString(R.string.joker_fish_desc),
                100,
                GoldManager.KEY_JOKER_FISH,
                R.drawable.ic_joker_fish
            ),
            Joker(
                "wheel",
                getString(R.string.joker_wheel_name),
                getString(R.string.joker_wheel_desc),
                200,
                GoldManager.KEY_JOKER_WHEEL,
                R.drawable.ic_joker_wheel
            ),
            Joker(
                "lollipop",
                getString(R.string.joker_lollipop_name),
                getString(R.string.joker_lollipop_desc),
                75,
                GoldManager.KEY_JOKER_LOLLIPOP,
                R.drawable.ic_joker_lollipop
            ),
            Joker(
                "swap_free",
                getString(R.string.joker_swap_free_name),
                getString(R.string.joker_swap_free_desc),
                125,
                GoldManager.KEY_JOKER_SWAP,
                R.drawable.ic_joker_swap
            ),
            Joker(
                "shuffle",
                getString(R.string.joker_shuffle_name),
                getString(R.string.joker_shuffle_desc),
                300,
                GoldManager.KEY_JOKER_SHUFFLE,
                R.drawable.ic_joker_shuffle
            ),
            Joker(
                "party",
                getString(R.string.joker_party_name),
                getString(R.string.joker_party_desc),
                400,
                GoldManager.KEY_JOKER_PARTY,
                R.drawable.ic_joker_party
            )
        )

        rvMarketItems.layoutManager = LinearLayoutManager(this)
        rvMarketItems.adapter = MarketAdapter(jokers, goldManager) {
            updateGoldDisplay()
        }
    }
}