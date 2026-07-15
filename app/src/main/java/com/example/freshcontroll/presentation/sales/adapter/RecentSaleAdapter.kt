package com.example.freshcontroll.presentation.sales.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.databinding.ItemRecentSaleBinding
import com.example.freshcontroll.domain.model.Sale

class RecentSaleAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<Sale, RecentSaleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRecentSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemRecentSaleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sale) {
            // Unificamos la información en el campo único del XML
            binding.tvSaleTimeAndEmployee.text = "${item.timestamp} – ${item.userName}"

            // Usamos el ID correcto para el monto
            binding.tvSaleAmount.text = "S/ ${item.total}"

            // Usamos el ID correcto para el badge
            binding.cvEditedBadge.isVisible = item.isEdited

            binding.root.setOnClickListener { onItemClick(item.id) }
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(o: Sale, n: Sale) = o.id == n.id
        override fun areContentsTheSame(o: Sale, n: Sale) = o == n
    }
}