package com.example.yazlab4

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MarketAdapter(
    private val jokers: List<Joker>,
    private val goldManager: GoldManager,
    private val onPurchase: () -> Unit
) : RecyclerView.Adapter<MarketAdapter.MarketViewHolder>() {

    class MarketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivJokerIcon)
        val tvName: TextView = view.findViewById(R.id.tvJokerName)
        val tvDesc: TextView = view.findViewById(R.id.tvJokerDescription)
        val tvCount: TextView = view.findViewById(R.id.tvOwnedCount)
        val btnBuy: Button = view.findViewById(R.id.btnBuyJoker)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_market_joker, parent, false)
        return MarketViewHolder(view)
    }

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        val joker = jokers[position]
        val context = holder.itemView.context

        holder.ivIcon.setImageResource(joker.iconResId)
        holder.tvName.text = joker.name
        holder.tvDesc.text = joker.description
        holder.tvCount.text = "Adet: ${goldManager.getJokerCount(joker.key)}"
        holder.btnBuy.text = "${joker.cost} Altın"

        holder.btnBuy.setOnClickListener {
            if (goldManager.spendGold(joker.cost)) {
                goldManager.addJoker(joker.key)
                Toast.makeText(context, context.getString(R.string.purchase_success), Toast.LENGTH_SHORT).show()
                notifyItemChanged(position)
                onPurchase()
            } else {
                Toast.makeText(context, context.getString(R.string.insufficient_gold), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = jokers.size
}