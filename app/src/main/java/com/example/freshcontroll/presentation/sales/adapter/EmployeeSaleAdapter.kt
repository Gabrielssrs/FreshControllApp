package com.example.freshcontroll.presentation.sales.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.databinding.ItemEmployeeSaleBinding
import com.example.freshcontroll.domain.model.Sale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmployeeSaleAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<Sale, EmployeeSaleAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemEmployeeSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemEmployeeSaleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sale) {
            binding.tvSaleNumber.text = "Venta #${item.id}"
            binding.tvSaleAmount.text = "S/ ${item.total}"

            // 2. CORRIGE ESTA SECCIÓN PARA EL TIMESTAMP:
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.tvSaleTime.text = timeFormat.format(Date(item.timestamp))

            binding.cvEditedBadge.isVisible = item.isEdited

            binding.root.setOnClickListener { onItemClick(item.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(o: Sale, n: Sale) = o.id == n.id
        override fun areContentsTheSame(o: Sale, n: Sale) = o == n
    }
}