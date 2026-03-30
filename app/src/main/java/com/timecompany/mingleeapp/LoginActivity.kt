package com.timecompany.mingleeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.timecompany.mingleeapp.MainActivity
import com.timecompany.mingleeapp.R
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneInput: EditText
    private lateinit var codeInput: EditText
    private lateinit var sendButton: Button
    private lateinit var verifyButton: Button

    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        phoneInput = findViewById(R.id.editTextPhone)
        codeInput = findViewById(R.id.editTextCode)
        sendButton = findViewById(R.id.buttonSendCode)
        verifyButton = findViewById(R.id.buttonVerify)

        sendButton.setOnClickListener {
            val phoneNumber = phoneInput.text.toString()
            sendVerificationCode(phoneNumber)
        }

        verifyButton.setOnClickListener {
            val code = codeInput.text.toString()
            if (verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithCredential(credential)
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Telefon numarası (+90 ile başlamalı)
            .setTimeout(60L, TimeUnit.SECONDS) // Kodun geçerlilik süresi
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Otomatik doğrulama (bazı cihazlarda SMS otomatik okunur)
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@LoginActivity, "Doğrulama başarısız: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@LoginActivity.verificationId = verificationId
                    Toast.makeText(this@LoginActivity, "Kod gönderildi!", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Giriş başarısız!", Toast.LENGTH_SHORT).show()
                }
            }
    }
}