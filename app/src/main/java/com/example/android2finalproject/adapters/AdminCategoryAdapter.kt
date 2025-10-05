package com.example.android2finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android2finalproject.R
import com.example.android2finalproject.models.Category

class AdminCategoryAdapter(
    private val categoryList: List<Category>,
    private val onOpenCategory: (Category) -> Unit,   // a call back -> open category products (admin)
    private val onEditCategory: (Category) -> Unit,   // a call back -> rename category
    private val onDeleteCategory: (Category) -> Unit  // a call back -> delete category
) : RecyclerView.Adapter<AdminCategoryAdapter.AdminCategoryViewHolder>() {

    class AdminCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView   = itemView.findViewById(R.id.cat_name)
        val ivEdit: ImageView  = itemView.findViewById(R.id.catEditIv)
        val ivDelete: ImageView = itemView.findViewById(R.id.catDeleteIv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return AdminCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminCategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.tvName.text = category.name

        // open products list for category
        holder.itemView.setOnClickListener {
            onOpenCategory(category)  // tell fragment to open this category (admin)
        }

        //rename category
        holder.ivEdit.visibility = View.VISIBLE
        holder.ivEdit.setOnClickListener {
            onEditCategory(category)  // tell fragment to edit this category
        }

        //remove category
        holder.ivDelete.visibility = View.VISIBLE
        holder.ivDelete.setOnClickListener {
            onDeleteCategory(category)  // tell fragment to delete this category
        }
    }

    override fun getItemCount(): Int = categoryList.size
}
