package com.example.android2finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android2finalproject.R
import com.example.android2finalproject.models.Product

class AdminProductAdapter(
    private val list: List<Product>,
    private val onEdit: (Product) -> Unit,    // callback -> edit product
    private val onDelete: (Product) -> Unit   // callback -> delete product
) : RecyclerView.Adapter<AdminProductAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name : TextView  = itemView.findViewById(R.id.item_name)
        val price: TextView  = itemView.findViewById(R.id.item_price)
        val type : TextView  = itemView.findViewById(R.id.item_type)
        val desc : TextView  = itemView.findViewById(R.id.item_description)
        val rating: TextView = itemView.findViewById(R.id.item_rating)
        val img  : ImageView = itemView.findViewById(R.id.item_iv)
        val addToCartIv: ImageView = itemView.findViewById(R.id.addToCartIv)
        val editIv: ImageView = itemView.findViewById(R.id.editIv)
        val deleteIv: ImageView = itemView.findViewById(R.id.deleteIv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = list[position]
        h.name.text  = p.pName
        h.price.text = p.pPrice.toString()
        h.type.text  = p.pType
        h.desc.text  = p.pDescription
        h.rating.text= "★ ${p.pRating}"

        Glide.with(h.img.context).load(p.imageUri).into(h.img)

        // show admin icons / hide cart icon
        h.addToCartIv.visibility = View.GONE
        h.editIv.visibility      = View.VISIBLE
        h.deleteIv.visibility    = View.VISIBLE

        h.editIv.setOnClickListener {
            onEdit(p)
        }
        h.deleteIv.setOnClickListener {
            onDelete(p)
        }
    }

    override fun getItemCount(): Int = list.size
}
