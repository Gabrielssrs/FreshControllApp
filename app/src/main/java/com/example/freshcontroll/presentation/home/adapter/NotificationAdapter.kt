package com.example.freshcontroll.presentation.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.R
import com.example.freshcontroll.databinding.ItemNotificationBinding
import com.example.freshcontroll.domain.model.Product
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class AlertType {
    OUT_OF_STOCK,
    EXPIRING,
    LOW_STOCK
}

class NotificationAdapter(
    private val alertType: AlertType,
    private val onItemClick: (String) -> Unit
) : ListAdapter<Product, NotificationAdapter.NotificationViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val context = binding.root.context

            // 1. Nombre del producto
            binding.tvProductName.text = product.name

            // 2. Lógica de colores, íconos y mensajes según el AlertType
            when (alertType) {
                AlertType.OUT_OF_STOCK -> {
                    binding.tvNotificationMessage.text = "¡Sin existencias!"

                    // Colores Rojo
                    binding.tvNotificationMessage.setTextColor(ContextCompat.getColor(context, R.color.rojo_oscuro_icono))
                    binding.cvNotificationItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.rojo_fondo_alerta))
                    binding.cvNotificationItem.strokeColor = ContextCompat.getColor(context, R.color.error)
                    binding.cvNotificationIconBg.setCardBackgroundColor(ContextCompat.getColor(context, R.color.error))

                    // Ícono (Ajusta el nombre de tu drawable si es distinto)
                    binding.ivNotificationIcon.setImageResource(R.drawable.ic_remove_shopping_cart)
                }

                AlertType.EXPIRING -> {
                    // Cálculo de días restantes
                    val expirationMs = product.expirationDate ?: 0L
                    val diffMs = expirationMs - System.currentTimeMillis()
                    val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMs).coerceAtLeast(0)

                    // Formateo de fecha
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val dateStr = if (expirationMs > 0) sdf.format(Date(expirationMs)) else "--/--/----"

                    binding.tvNotificationMessage.text = "Vence en $daysRemaining días ($dateStr)"

                    // Colores Rojo (mismo estilo de alerta crítica)
                    binding.tvNotificationMessage.setTextColor(ContextCompat.getColor(context, R.color.rojo_oscuro_icono))
                    binding.cvNotificationItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.rojo_fondo_alerta))
                    binding.cvNotificationItem.strokeColor = ContextCompat.getColor(context, R.color.error)
                    binding.cvNotificationIconBg.setCardBackgroundColor(ContextCompat.getColor(context, R.color.error))

                    // Ícono (Ajusta el nombre de tu drawable si es distinto)
                    binding.ivNotificationIcon.setImageResource(R.drawable.ic_event_busy)
                }

                AlertType.LOW_STOCK -> {
                    // Quitamos los decimales si es un número entero para mejor presentación
                    val stockFormatted = if (product.currentStock % 1.0 == 0.0) {
                        product.currentStock.toInt().toString()
                    } else {
                        product.currentStock.toString()
                    }

                    binding.tvNotificationMessage.text = "Solo quedan $stockFormatted unidades"

                    // Colores Morado
                    binding.tvNotificationMessage.setTextColor(ContextCompat.getColor(context, R.color.morado_acento))
                    binding.cvNotificationItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.blanco))
                    binding.cvNotificationItem.strokeColor = ContextCompat.getColor(context, R.color.morado_acento)
                    binding.cvNotificationIconBg.setCardBackgroundColor(ContextCompat.getColor(context, R.color.morado_acento))

                    // Ícono (Ajusta el nombre de tu drawable si es distinto)
                    binding.ivNotificationIcon.setImageResource(R.drawable.ic_info)
                }
            }

            // 3. Click Listener
            binding.root.setOnClickListener {
                onItemClick(product.id)
            }
        }
    }
}

// DiffUtil para optimización de listas
class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        // Al ser data class, el == compara todas las propiedades internas
        return oldItem == newItem
    }
}