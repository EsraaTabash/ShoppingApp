package com.example.android2finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android2finalproject.R
import com.example.android2finalproject.adapters.CartAdapter
import com.example.android2finalproject.data.FirestoreService
import com.example.android2finalproject.models.CartItem

/**
 * A simple [Fragment] subclass.
 * Use the [CartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CartFragment : Fragment() {
    private lateinit var rvCart: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var cartAdapter: RecyclerView.Adapter<*>
    private val cartList = arrayListOf<CartItem>()
    private val fs = FirestoreService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvCart  = view.findViewById(R.id.rvCart)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        rvCart.layoutManager = LinearLayoutManager(requireContext())
        cartAdapter = CartAdapter(
            list = cartList,
            onAddOne = { item, pos -> addOneToCart(item, pos) },       // +1
            onDeleteOne = { item, pos -> deleteOneFromCart(item, pos) } // -1 / delete
        )
        rvCart.adapter = cartAdapter

        // load data
        loadCartFromFirestore()
    }

    // get cart items for current user
    private fun loadCartFromFirestore() {
        fs.getCartItems(
            onSuccess = { list ->
                cartList.clear()
                cartList.addAll(list)
                cartAdapter.notifyDataSetChanged()
                toggleEmpty(list.isEmpty())
                refreshCartBadge() //update badge
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail load cart: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                toggleEmpty(true)
            }
        )
    }

    //update qty on fs then locally on badge
    private fun addOneToCart(item: CartItem, position: Int) {
        val newQty = item.qty + 1
        fs.updateCartQty(
            productId = item.productId,
            newQty = newQty,
            onSuccess = {
                //copy newqty to qty parameter fot the item
                val updated = item.copy(qty = newQty)
                cartList[position] = updated
                cartAdapter.notifyItemChanged(position)
                refreshCartBadge()
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail add qty: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    //-1 or 0
    private fun deleteOneFromCart(item: CartItem, position: Int) {
        val newQty = (item.qty - 1).coerceAtLeast(0)
        if (newQty > 0) {
            fs.updateCartQty(
                productId = item.productId,
                newQty = newQty,
                onSuccess = {
                    val updated = item.copy(qty = newQty)
                    cartList[position] = updated
                    cartAdapter.notifyItemChanged(position)
                    refreshCartBadge()
                },
                onError = { e ->
                    Toast.makeText(requireContext(), "fail update qty: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            fs.removeFromCart(
                productId = item.productId,
                onSuccess = {
                    cartList.removeAt(position)
                    cartAdapter.notifyItemRemoved(position)
                    toggleEmpty(cartList.isEmpty())
                    refreshCartBadge()
                },
                onError = { e ->
                    Toast.makeText(requireContext(), "fail remove item: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // show togle that empty
    private fun toggleEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            tvEmpty.visibility = View.VISIBLE
            rvCart.visibility  = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvCart.visibility  = View.VISIBLE
        }
    }

    //update badge
    private fun refreshCartBadge() {
        fs.getCartCount(
            onSuccess = { total ->
                (requireActivity() as? CartBadgeHost)?.setCartBadge(total)
            },
            onError = {

            }
        )
    }
}
