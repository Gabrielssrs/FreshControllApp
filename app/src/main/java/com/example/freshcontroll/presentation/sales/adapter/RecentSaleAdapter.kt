package com.example.freshcontroll.presentation.sales.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.databinding.ItemRecentSaleBinding
import com.example.freshcontroll.domain.model.Sale

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentSaleAdapter(
    private val onItemClick: (String) -> Unit,
    private val onEditClick: (String) -> Unit
) : ListAdapter<Sale, RecentSaleAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("HH:mm – dd/MM", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemRecentSaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemRecentSaleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sale) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            binding.tvSaleTimeAndEmployee.text = "Venta #${item.ticketNumber} – $dateStr"

            binding.tvSaleAmount.text = String.format("S/ %.2f", item.total)
            
            // Usamos el campo de pago para el nombre del empleado si es dueño
            binding.tvSalePaymentMethod.text = "Atendido por: ${item.userName}"
            binding.tvSalePaymentMethod.isVisible = true

            binding.cvEditedBadge.isVisible = item.isEdited

            binding.root.setOnClickListener { onItemClick(item.id) }
            binding.ivEditSale.setOnClickListener { onEditClick(item.id) }
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(o: Sale, n: Sale) = o.id == n.id
        override fun areContentsTheSame(o: Sale, n: Sale) = o == n
    }
}