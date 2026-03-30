package com.timecompany.mingleeapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Splash ekranı layout'u (boş bile olsa bir root view olmalı)
        setContentView(R.layout.activity_splash)

        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Kullanıcı giriş yapmış → Ana ekrana yönlendir
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Kullanıcı giriş yapmamış → Login ekranına yönlendir
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // SplashActivity’yi kapatıyoruz, geri tuşuna basıldığında dönmesin
        finish()
    }
}