package com.guillem.repeticiollistacompra

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.guillem.repeticiollistacompra.databinding.ItemShoppingListBinding

class ShoppingListAdapter(
    private var lists: List<ShoppingList>,
    private val onClick: (ShoppingList) -> Unit,
    private val onDeleteClick: (ShoppingList) -> Unit,
    private val onEditClick: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemShoppingListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShoppingListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lists[position]
        holder.binding.tvListName.text = item.name
        holder.binding.root.setOnClickListener { onClick(item) }
        holder.binding.btnDeleteList.setOnClickListener { onDeleteClick(item) }
        holder.binding.btnEditList.setOnClickListener { onEditClick(item) }
    }

    override fun getItemCount(): Int = lists.size

    // IMPORTANT per al Drag & Drop
    fun getLists(): List<ShoppingList> = lists

    fun updateData(newLists: List<ShoppingList>) {
        lists = newLists
        notifyDataSetChanged()
    }
}
