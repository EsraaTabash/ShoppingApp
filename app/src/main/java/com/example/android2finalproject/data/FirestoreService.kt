package com.example.android2finalproject.data

import com.esraa.shoppingapp.data.model.Category
import com.esraa.shoppingapp.data.model.Product
import com.google.firebase.firestore.DocumentSnapshot
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
                    val name = doc.getString("pName") ?:""
                    val description = doc.getString("pDescription") ?:""
                    val type= doc.getString("pType") ?:""
                    val imageUri = doc.getString("imageUri") ?:""
                    val price  = doc.getDouble("pPrice") ?: 0.0
                    val rating = doc.getDouble("pRating") ?: 0.0
                    //model
                    val product = Product(
                        pName = name ,
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


}
