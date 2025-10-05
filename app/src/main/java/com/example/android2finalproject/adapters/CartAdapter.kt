package com.example.android2finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android2finalproject.R
import com.example.android2finalproject.models.CartItem

class CartAdapter(
    private val list: MutableList<CartItem>,
    private val onAddOne: (CartItem, Int) -> Unit,     // callback -> +1
    private val onDeleteOne: (CartItem, Int) -> Unit   // callback -> -1
) : RecyclerView.Adapter<CartAdapter.CartVH>() {

    class CartVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img : ImageView = itemView.findViewById(R.id.item_iv)
        val name : TextView  = itemView.findViewById(R.id.item_name)
        val price : TextView  = itemView.findViewById(R.id.item_price)
        val type : TextView  = itemView.findViewById(R.id.item_type)
        val desc : TextView  = itemView.findViewById(R.id.item_description)
        val rating : TextView  = itemView.findViewById(R.id.item_rating)
        val addToCartIv: ImageView = itemView.findViewById(R.id.addToCartIv)
        val editIv : ImageView = itemView.findViewById(R.id.editIv)
        val deleteIv : ImageView = itemView.findViewById(R.id.deleteIv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return CartVH(v)
    }

    override fun onBindViewHolder(holder: CartVH, position: Int) {
        val item = list[position]
        holder.name.text  = item.pName
        holder.price.text = item.pPrice.toString()
        holder.desc.text  = item.pDescription
        Glide.with(holder.img.context)
            .load(item.imageUri)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.img)

        holder.name.visibility  = View.VISIBLE
        holder.price.visibility = View.VISIBLE
        holder.desc.visibility  = View.VISIBLE
        holder.type.visibility  = View.GONE

        holder.rating.visibility = View.VISIBLE
        holder.rating.text = "x${item.qty}"

        holder.addToCartIv.visibility = View.VISIBLE   // +1
        holder.deleteIv.visibility    = View.VISIBLE   // -1 / 0
        holder.editIv.visibility      = View.GONE

        // +1
        holder.addToCartIv.setOnClickListener {
            onAddOne(item, holder.adapterPosition)
        }

        // -1 or delete
        holder.deleteIv.setOnClickListener {
            onDeleteOne(item, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = list.size
}
