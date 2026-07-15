package com.example.freshcontroll.presentation.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.freshcontroll.databinding.ItemEmployeeBinding
import com.example.freshcontroll.domain.model.User

class EmployeeAdapter(
    private val onAccessToggled: (String, Boolean) -> Unit
) : ListAdapter<User, EmployeeAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemEmployeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val binding: ItemEmployeeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User) {
            binding.tvEmployeeName.text = item.fullName
            binding.tvEmployeeRole.text = item.role.name

            // Lógica para mostrar foto o iniciales
            if (!item.photoUrl.isNullOrEmpty()) {
                binding.sivEmployeePhoto.isVisible = true
                binding.cvEmployeeInitialsBg.isVisible = false
                // TODO: Aquí cargarías la imagen (ej. con Coil o Glide):
                // binding.sivEmployeePhoto.load(item.photoUrl)
            } else {
                binding.sivEmployeePhoto.isVisible = false
                binding.cvEmployeeInitialsBg.isVisible = true
                val initials = item.fullName.split(" ").take(2).map { it.first().uppercase() }.joinToString("")
                binding.tvEmployeeInitials.text = initials
            }

            // Corrección: usando switchAccessToggle
            binding.switchAccessToggle.setOnCheckedChangeListener(null)
            binding.switchAccessToggle.isChecked = item.hasAccess
            binding.switchAccessToggle.setOnCheckedChangeListener { _, isChecked ->
                onAccessToggled(item.id, isChecked)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(o: User, n: User) = o.id == n.id
        override fun areContentsTheSame(o: User, n: User) = o == n
    }
}