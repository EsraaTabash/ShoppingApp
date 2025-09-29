package com.example.android2finalproject.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android2finalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var nameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passEt: EditText
    private lateinit var signupBtn: Button
    private lateinit var goToLoginBtn: Button
    private lateinit var adminCheck: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        nameEt  = findViewById(R.id.nameEditText)
        emailEt = findViewById(R.id.emailEditText)
        passEt  = findViewById(R.id.passwordEditText)
        signupBtn = findViewById(R.id.signupButton)
        goToLoginBtn = findViewById(R.id.loginButton)
        adminCheck   = findViewById(R.id.isAdminCheckBox)

        signupBtn.setOnClickListener {
            doRegister()
        }

        goToLoginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun doRegister() {
        //validate fields from spaces
        val name  = nameEt.text.toString().trim()
        val email = emailEt.text.toString().trim()
        val pass  = passEt.text.toString().trim()
        val isAdmin = adminCheck.isChecked

        //validate fields from empty
        if (name.isEmpty())  { nameEt.error = "pleas, insert name"; nameEt.requestFocus(); return }
        if (email.isEmpty()) { emailEt.error = "pleas, insert email"; emailEt.requestFocus(); return }
        if (pass.isEmpty())  { passEt.error  = "pleas, insert password"; passEt.requestFocus(); return }
        if (pass.length < 6) { passEt.error = "password less than 6" ; passEt.requestFocus() ; return }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailEt.error = "email format is not right" ; emailEt.requestFocus() ;return }
        signupBtn.isEnabled = false

        //create user in firebase
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener  { result->

                val user = result.user

                if (user == null) {
                    Toast.makeText(this, "creation fail", Toast.LENGTH_SHORT).show()
                    signupBtn.isEnabled = true
                    return@addOnSuccessListener
                }


                //add user as a doc to the firestore
                val userDoc = hashMapOf(
                    "id" to user.uid,
                    "name" to name,
                    "email" to email,
                    "isAdmin" to isAdmin
                )

                db.collection("users").document(user.uid).set(userDoc)
                    .addOnSuccessListener {

                        Toast.makeText(this, "craation sucess", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("isAdmin", isAdmin)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "User data could not be store.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {e ->
                val msg = when (e.message) {
                    null -> "craation fail try again"
                    else -> "craation fail: ${e.localizedMessage}"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                signupBtn.isEnabled = true
            }
    }
}
