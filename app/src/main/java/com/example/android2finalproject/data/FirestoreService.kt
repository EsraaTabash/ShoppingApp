package com.example.android2finalproject.data

import com.example.android2finalproject.models.CartItem
import com.example.android2finalproject.models.Category
import com.example.android2finalproject.models.Product
import com.example.android2finalproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreService {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    //everytime we get category we will use 2 func
    fun getCategories(
        onSuccess: (List<Category>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("categories")
            .get()
            .addOnSuccessListener { querySnapshot ->
                //list for categories we will bring from firestore
                val list = ArrayList<Category>()

                //each doc in firestore we will add it in the list above like a model
                for (doc in querySnapshot.documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    //model
                    val category = Category(
                        name = name,
                        id = id
                    )
                    //add model to list
                    list.add(category)
                }

                onSuccess(list)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    //everytime we get products we will use 3 parameters (string+2func)
    fun getProductsOfCategory(
        categoryId: String,
        onSuccess: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        //get products collections for one category
        db.collection("categories")
            .document(categoryId)
            .collection("products")
            .get()
            .addOnSuccessListener { querySnapshot ->

                //list to put products docs inside it after convert them to models
                val list = ArrayList<Product>()

                for (doc in querySnapshot.documents) {
                    val name = doc.getString("pName") ?:""
                    val description = doc.getString("pDescription") ?:""
                    val type= doc.getString("pType") ?:""
                    val imageUri = doc.getString("imageUri") ?:""
                    val price  = doc.getDouble("pPrice") ?: 0.0
                    val rating = doc.getDouble("pRating") ?: 0.0
                    val lat = doc.getDouble("latitude")
                    val lng = doc.getDouble("longitude")

                    //model
                    val product = Product(
                        pName = name ,
                        pDescription = description,
                        pPrice = price,
                        pType = type,
                        pRating = rating,
                        imageUri = imageUri,
                        id = doc.id,
                        latitude = lat,
                        longitude = lng
                    )
                    list.add(product)
                }

                onSuccess(list)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    //everytime we get user data we will use 2 func (success + error)
    fun getUserData(
        onSuccess: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?:return

        //get user document from firestore
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val user = User(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: (FirebaseAuth.getInstance().currentUser?.email ?: ""),
                        isAdmin = doc.getBoolean("isAdmin") ?: false
                    )
                    onSuccess(user)
                } else {
                    onError(IllegalStateException("user document not found"))
                }
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    // ------------------------------------------------------------
    // add product to user cart (create if not exists, else qty++)
    // add product to user cart (create if not exists, else qty++)
    fun addToCart(
        product: Product,
        categoryId: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onError(IllegalStateException("no user logged in"))

        //users->{uid}->cart->{productId}
        val docRef = db.collection("users")
            .document(uid)
            .collection("cart")
            .document(product.id)

        val baseData = hashMapOf<String, Any>(
            "productId" to product.id,
            "pName" to product.pName,
            "imageUri" to product.imageUri,
            "pPrice" to product.pPrice,
            "pDescription" to product.pDescription,
            "categoryId" to categoryId,
            "latitude" to (if (latitude != 0.0) latitude else (product.latitude ?: 0.0)),
            "longitude" to (if (longitude != 0.0) longitude else (product.longitude ?: 0.0))
        )

        docRef.get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    //if exits ++
                    val oldQty = doc.getLong("qty") ?: 0L
                    docRef.update(baseData as Map<String, Any>)
                        .addOnSuccessListener {
                            docRef.update("qty", oldQty + 1)
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener(onError)
                        }
                        .addOnFailureListener(onError)
                } else {
                    //else create with 1 val
                    baseData["qty"] = 1L
                    docRef.set(baseData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener(onError)
                }
            }
            .addOnFailureListener(onError)
    }


    //call all cart items
    fun getCartItems(
        onSuccess: (List<CartItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onError(IllegalStateException("no user ?"))
        db.collection("users").document(uid).collection("cart")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = ArrayList<CartItem>()
                for (doc in querySnapshot.documents) {
                    val item = CartItem(
                        productId = doc.getString("productId") ?: doc.id,
                        pName = doc.getString("pName") ?: "",
                        pDescription = doc.getString("pDescription") ?: "",
                        imageUri = doc.getString("imageUri") ?: "",
                        pPrice = doc.getDouble("pPrice") ?: 0.0,
                        qty = doc.getLong("qty") ?: 1L,
                        categoryId = doc.getString("categoryId") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0
                    )
                    list.add(item)
                }
                onSuccess(list)
            }
            .addOnFailureListener(onError)
    }

    // update prod qty
    fun updateCartQty(
        productId: String,
        newQty: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onError(IllegalStateException("no user?"))
        db.collection("users").document(uid).collection("cart")
            .document(productId)
            .update("qty", newQty)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    // remove prod from cart if qty=0
    fun removeFromCart(
        productId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onError(IllegalStateException("no user?"))
        db.collection("users").document(uid).collection("cart")
            .document(productId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    // sum of qty
    fun getCartCount(
        onSuccess: (Long) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onError(IllegalStateException("no user ?"))
        db.collection("users").document(uid).collection("cart")
            .get()
            .addOnSuccessListener { querySnapshot  ->
                var total = 0L
                for (doc in querySnapshot .documents) {
                    total += doc.getLong("qty") ?: 0L
                }
                onSuccess(total)     //badge = total count
            }
            .addOnFailureListener(onError)
    }



}
