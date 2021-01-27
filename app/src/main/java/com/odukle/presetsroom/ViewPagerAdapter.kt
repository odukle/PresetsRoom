package com.odukle.presetsroom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_how_to_import_splash.*

class ViewPagerAdapter(private var instructions: List<String>, private var images: List<Int>) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    inner class ViewPagerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView_ht)
        val imageView: ImageView = view.findViewById(R.id.image_ht)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPagerAdapter.ViewPagerViewHolder {
        return ViewPagerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.view_pager_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.ViewPagerViewHolder, position: Int) {
        holder.textView.text = instructions[position]
        holder.imageView.setImageResource(images[position])

        if (holder.adapterPosition == 5) {
            if (htiS != null) {
                when (holder.adapterPosition) {
                    4 -> {
                        htiS!!.btn_skip.text = "Skip"
                    }

                    5 -> {
                        htiS!!.btn_skip.text = "Got it"
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return instructions.size
    }
}