package com.example.android2finalproject.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.android2finalproject.R
import com.example.android2finalproject.data.AdminFirestoreService
import com.example.android2finalproject.models.Product
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass.
 * Use the [StatisticFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatisticFragment : Fragment() {

    private lateinit var moContainer: View
    private lateinit var moImg: ImageView
    private lateinit var moName: TextView
    private lateinit var moPrice: TextView
    private lateinit var moType: TextView
    private lateinit var moDesc: TextView
    private lateinit var moRating: TextView
    private lateinit var moQty: TextView
    private lateinit var moEmpty: TextView
    private lateinit var trContainer: View
    private lateinit var trImg: ImageView
    private lateinit var trName: TextView
    private lateinit var trPrice: TextView
    private lateinit var trType: TextView
    private lateinit var trDesc: TextView
    private lateinit var trRating: TextView
    private lateinit var trEmpty: TextView

    private val adminFs = AdminFirestoreService()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        moContainer = view.findViewById(R.id.cardMostOrdered)
        moImg = view.findViewById(R.id.mo_item_iv)
        moName = view.findViewById(R.id.mo_item_name)
        moPrice = view.findViewById(R.id.mo_item_price)
        moType = view.findViewById(R.id.mo_item_type)
        moDesc = view.findViewById(R.id.mo_item_description)
        moRating = view.findViewById(R.id.mo_item_rating)
        moQty = view.findViewById(R.id.mo_item_qty)
        moEmpty  = view.findViewById(R.id.tvMostOrderedEmpty)
        trContainer = view.findViewById(R.id.cardTopRated)
        trImg = view.findViewById(R.id.tr_item_iv)
        trName = view.findViewById(R.id.tr_item_name)
        trPrice = view.findViewById(R.id.tr_item_price)
        trType = view.findViewById(R.id.tr_item_type)
        trDesc = view.findViewById(R.id.tr_item_description)
        trRating = view.findViewById(R.id.tr_item_rating)
        trEmpty = view.findViewById(R.id.tvTopRatedEmpty)

        loadMostOrderedProduct()
        loadTopRatedFive()
    }

    // 1) get most ordered product and show it
    private fun loadMostOrderedProduct() {
        adminFs.getMostOrderedProduct(
            onSuccess = { pair ->
                if (pair == null) {
                    // no data -> show empty text
                    moContainer.visibility = View.GONE
                    moEmpty.visibility = View.VISIBLE
                } else {
                    val product = pair.first
                    val totalQty = pair.second
                    showMostOrdered(product, totalQty)
                }
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail stats: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                moContainer.visibility = View.GONE
                moEmpty.visibility = View.VISIBLE
            }
        )
    }

    private fun showMostOrdered(p: Product, qty: Long) {
        moContainer.visibility = View.VISIBLE
        moEmpty.visibility = View.GONE
        moName.text = p.pName
        moPrice.text = p.pPrice.toString()
        moType.text = p.pType
        moDesc.text = p.pDescription
        moRating.text = "★ ${p.pRating}"
        moQty.text = "x$qty"
        Glide.with(moImg.context).load(p.imageUri).into(moImg)
    }

    // 2) get top rated product
    private fun loadTopRatedFive() {
        db.collectionGroup("products")
            .whereEqualTo("pRating", 5.0)
            .limit(1)
            .get()
            .addOnSuccessListener { qs ->
                val d = qs.documents.firstOrNull()
                if (d == null) {
                    trContainer.visibility = View.GONE
                    trEmpty.visibility = View.VISIBLE
                } else {
                    val p = Product(
                        pName = d.getString("pName") ?: "",
                        pDescription = d.getString("pDescription") ?: "",
                        pPrice = d.getDouble("pPrice") ?: 0.0,
                        pType = d.getString("pType") ?: "",
                        pRating = d.getDouble("pRating") ?: 0.0,
                        imageUri  = d.getString("imageUri") ?: "",
                        id = d.id
                    )
                    showTopRated(p)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "fail top rated: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                adminFs.getTopRatedProduct(
                    onSuccess = { p ->
                        if (p == null || p.pRating < 5.0) {
                            trContainer.visibility = View.GONE
                            trEmpty.visibility = View.VISIBLE
                        } else {
                            showTopRated(p)
                        }
                    },
                    onError = { err ->
                        Log.e("STAT_DEBUG", "fallback top rated failed: ${err.localizedMessage}", err)
                        trContainer.visibility = View.GONE
                        trEmpty.visibility = View.VISIBLE
                    }
                )
            }
    }

    private fun showTopRated(p: Product) {
        trContainer.visibility = View.VISIBLE
        trEmpty.visibility = View.GONE
        trName.text  = p.pName
        trPrice.text = p.pPrice.toString()
        trType.text  = p.pType
        trDesc.text  = p.pDescription
        trRating.text = "★ ${p.pRating}"

        Glide.with(trImg.context).load(p.imageUri).into(trImg)
    }


}
