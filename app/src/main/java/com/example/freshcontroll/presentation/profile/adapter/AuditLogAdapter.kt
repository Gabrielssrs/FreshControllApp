package com.example.freshcontroll.presentation.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.ItemAuditLogBinding
import com.example.freshcontroll.domain.model.AuditEventType
import com.example.freshcontroll.domain.model.AuditLog
//import com.example.freshcontroll.domain.model.EventType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditLogAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<AuditLog, AuditLogAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemAuditLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemAuditLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AuditLog) {
            // Asignación corregida según los IDs reales del XML
            binding.tvAuditLogTitle.text = item.description

            // Fix 1: Formateo de timestamp (Long a String legible)
            val timeFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            binding.tvAuditTimestamp.text = timeFormat.format(Date(item.timestamp))

            binding.tvAuditResponsible.text = "Hecho por: ${item.userName}"

            // Fix 2: Uso de AuditEventType en lugar de EventType
            val icon = when(item.eventType) {
                AuditEventType.VENTA_EDITADA -> R.drawable.ic_edit
                AuditEventType.AJUSTE_STOCK -> R.drawable.ic_edit
                AuditEventType.PRODUCTO_ELIMINADO -> R.drawable.ic_delete
            }
            binding.ivAuditIcon.setImageResource(icon)

            binding.btnViewDetail.setOnClickListener { onItemClick(item.id) }
            binding.root.setOnClickListener { onItemClick(item.id) }
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<AuditLog>() {
        override fun areItemsTheSame(o: AuditLog, n: AuditLog) = o.id == n.id
        override fun areContentsTheSame(o: AuditLog, n: AuditLog) = o == n
    }
}