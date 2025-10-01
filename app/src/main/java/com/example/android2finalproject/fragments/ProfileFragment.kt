package com.example.android2finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.android2finalproject.R
import com.example.android2finalproject.data.FirestoreService
import com.example.android2finalproject.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass for user profile.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView

    //service
    private val fs = FirestoreService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //bind views
        tvName  = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvRole  = view.findViewById(R.id.tvRole)

        //load data from firebase instead of temp data
        loadUserDataFromFirestore()
    }

    //get user data and show on screen (name // email // role)
    private fun loadUserDataFromFirestore() {
        fs.getUserData(
            onSuccess = { user ->
                showUser(user)
            },
            onError = { e ->
                Toast.makeText(requireContext(), "fail load user: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    //fill screen with user data
    private fun showUser(user: User) {
        tvName.text  = user.name
        tvEmail.text = user.email

        if (user.isAdmin) {
            tvRole.text = "Admin"
            tvRole.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        } else {
            tvRole.text = "User"
            tvRole.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        }
    }

}