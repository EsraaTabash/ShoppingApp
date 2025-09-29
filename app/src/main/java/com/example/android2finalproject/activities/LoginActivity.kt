package com.example.android2finalproject.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android2finalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var emailEt: EditText
    private lateinit var passEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var goToSignupBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        emailEt = findViewById(R.id.emailEditText)
        passEt  = findViewById(R.id.passwordEditText)
        loginBtn = findViewById(R.id.loginButton)
        goToSignupBtn = findViewById(R.id.signupButton)

        loginBtn.setOnClickListener {
            doLogin()
        }
        goToSignupBtn.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
            finish()
        }
    }

    private fun doLogin() {

        //validate fields from spaces
        val email = emailEt.text.toString().trim()
        val pass  = passEt.text.toString().trim()

        //validate fields from empty
        if (email.isEmpty()) { emailEt.error = "pleas, insert email"; emailEt.requestFocus(); return }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailEt.error = "email format not coreect"; emailEt.requestFocus(); return }
        if (pass.isEmpty())  { passEt.error  = "pleas, insert pass"; passEt.requestFocus(); return }

        loginBtn.isEnabled = false

        //sign with firebase
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { task ->


                val user = auth.currentUser
                if (user == null) {
                    Toast.makeText(this, "login fail", Toast.LENGTH_SHORT).show()
                    loginBtn.isEnabled = true
                    return@addOnSuccessListener
                }
                //get user data from firestore by uid
                db.collection("users").document(user.uid).get()
                    .addOnSuccessListener { doc ->

                        val isAdmin = doc.getBoolean("isAdmin") ?: false
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("isAdmin", isAdmin)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "User data could not be read.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "login fail: ${e.localizedMessage ?: "check your data"}", Toast.LENGTH_SHORT).show()
                loginBtn.isEnabled = true
            }
    }
}
