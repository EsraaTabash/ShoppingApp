package com.example.android2finalproject.data

import com.esraa.shoppingapp.data.model.Category
import com.esraa.shoppingapp.data.model.Product
import com.example.android2finalproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
                    val name = doc.getString("pName") ?: ""
                    val description = doc.getString("pDescription") ?: ""
                    val type = doc.getString("pType") ?: ""
                    val imageUri = doc.getString("imageUri") ?: ""
                    val price = doc.getDouble("pPrice") ?: 0.0
                    val rating = doc.getDouble("pRating") ?: 0.0
                    //model
                    val product = Product(
                        pName = name,
                        pDescription = description,
                        pPrice = price,
                        pType = type,
                        pRating = rating,
                        imageUri = imageUri,
                        id = doc.id
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        //get user document from firestore
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val user = User(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email")
                            ?: (FirebaseAuth.getInstance().currentUser?.email ?: ""),
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


    //add product to current user cart
//we use 5 parameters (product + categoryId + lat/long  + 2 func)
    fun addToCart(
        product: Product,
        categoryId: String,
        latitude: Double? = null,
        longitude: Double? = null,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        //we save cart items under carts->{uid}->items->{productId}
        val itemRef = db.collection("carts")
            .document(uid)
            .collection("items")
            .document(product.id) //use product id as cart item id

        //1-read doc
        itemRef.get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    //2-if exists -> increment qty by 1
                    val currentQty = doc.getLong("qty") ?: 0L
                    itemRef.update("qty", currentQty + 1)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e) }
                } else {
                    //3-if doc not exists -> create doc with qty=1 by defealt
                    val data = hashMapOf<String, Any>(
                        "productId" to product.id,
                        "pName" to product.pName,
                        "imageUri" to product.imageUri,
                        "pPrice" to product.pPrice,
                        "categoryId" to categoryId,
                        "qty" to 1L
                    )
                    if (latitude != null && longitude != null) {
                        data["latitude"] = latitude
                        data["longitude"] = longitude
                    }

                    itemRef.set(data)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError(e) }
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }

}


