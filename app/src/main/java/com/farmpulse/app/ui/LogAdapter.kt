package com.farmpulse.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.farmpulse.app.R
import com.farmpulse.app.data.LogEntry
import com.farmpulse.app.data.LogKind
import com.farmpulse.app.databinding.ItemLogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogAdapter(private var items: List<LogEntry> = emptyList()) :
    RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    fun submitList(newItems: List<LogEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class LogViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: LogEntry) {
            binding.tvLogMessage.text = entry.message
            binding.tvLogTime.text =
                SimpleDateFormat("d MMM HH:mm", Locale("th")).format(Date(entry.timestampMillis))

            val colorRes = when (entry.kind) {
                LogKind.WATER -> R.color.water
                LogKind.FERTILIZER -> R.color.leaf
                LogKind.WARNING -> R.color.amber
                LogKind.CONNECTION -> R.color.ink_soft
                LogKind.INFO -> R.color.leaf_deep
            }
            binding.dotIndicator.setBackgroundColor(
                binding.root.context.resources.getColor(colorRes, null)
            )
        }
    }
}
