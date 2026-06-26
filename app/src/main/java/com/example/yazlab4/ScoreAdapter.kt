package com.example.yazlab4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoreAdapter(private val records: List<GameRecord>) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGameId: TextView = view.findViewById(R.id.tvGameId)
        val tvGameDate: TextView = view.findViewById(R.id.tvGameDate)
        val tvGameGrid: TextView = view.findViewById(R.id.tvGameGrid)
        val tvGameScore: TextView = view.findViewById(R.id.tvGameScore)
        val tvGameWordCount: TextView = view.findViewById(R.id.tvGameWordCount)
        val tvGameLongestWord: TextView = view.findViewById(R.id.tvGameLongestWord)
        val tvGameDuration: TextView = view.findViewById(R.id.tvGameDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_record, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val record = records[position]
        val context = holder.itemView.context
        
        holder.tvGameId.text = context.getString(R.string.game_card_title, record.id)
        holder.tvGameDate.text = context.getString(R.string.game_card_date, record.date)
        holder.tvGameGrid.text = context.getString(R.string.game_card_grid, record.gridLabel)
        holder.tvGameScore.text = context.getString(R.string.game_card_score, record.score)
        holder.tvGameWordCount.text = context.getString(R.string.game_card_word_count, record.wordCount)
        holder.tvGameLongestWord.text = context.getString(R.string.longest_word, record.longestWord)
        holder.tvGameDuration.text = context.getString(R.string.game_card_duration, record.durationFormatted)
    }

    override fun getItemCount(): Int = records.size
}