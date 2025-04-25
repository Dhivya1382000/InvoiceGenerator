package com.nithra.invoicegenerator

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen
import com.nithra.invoice_generator_tool.support.InvoiceUtils

class MainActivity : AppCompatActivity() {

    private lateinit var editTextSearch: EditText
    private lateinit var animatedHint: TextView
    private lateinit var searchIcon: ImageView
    private val hintTexts = listOf(
        "Search Products...",
        "Find Services...",
        "Explore Categories...",
        "Search for Deals..."
    )
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationRunning = true // Track animation state
    var preference = InvioceSharedPreference()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextSearch = findViewById(R.id.editTextSearch)
        animatedHint = findViewById(R.id.animatedHint)
        searchIcon = findViewById(R.id.searchIcon)

        val editText = EditText(this)
        editText.hint = "Enter your text here"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

// Create EditTexts
        val nameEditText = EditText(this)
        nameEditText.hint = "User Id"

        val emailEditText = EditText(this)
        emailEditText.hint = "Name"

        val phoneEditText = EditText(this)
        phoneEditText.hint = "Phone"

// Add them to layout
        layout.addView(nameEditText)
        layout.addView(emailEditText)
        layout.addView(phoneEditText)

        if (preference.getString(this@MainActivity,"INVOICE_USER_MOBILE").isEmpty()){
            val dialog = AlertDialog.Builder(this)
                .setTitle("Enter Details")
                .setView(layout)
                .setPositiveButton("Submit") { dialogInterface, _ ->
                    val userId = nameEditText.text.toString()
                    val Name = emailEditText.text.toString()
                    val phone = phoneEditText.text.toString()

                    preference.putString(
                        this@MainActivity,
                        "INVOICE_USER_NAME",
                        ""+Name
                    )
                    preference.putString(
                        this@MainActivity,
                        "INVOICE_USER_ID",
                        userId
                    )
                    preference.putString(
                        this@MainActivity,
                        "INVOICE_USER_MOBILE",
                        ""+phone
                    )
                    val intent = Intent(this@MainActivity, InvoiceHomeScreen::class.java)
                    intent.putExtra("AppLogin", 0)
                    intent.putExtra("AppLoginFrom", "")
                    intent.putExtra("OpenFromHome", 0)
                    startActivity(intent)
                    //Toast.makeText(this, "Name: $name\nEmail: $email\nPhone: $phone", Toast.LENGTH_LONG).show()
                    dialogInterface.dismiss()
                }
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()

            dialog.show()
        }else{
            val intent = Intent(this@MainActivity, InvoiceHomeScreen::class.java)
            intent.putExtra("AppLogin", 0)
            intent.putExtra("AppLoginFrom", "")
            intent.putExtra("OpenFromHome", 0)
            startActivity(intent)
        }



        changeHintWithAnimation()
        // Hide hint when EditText is focused
        editTextSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                stopHintAnimation() // Stop animation when user types
                animatedHint.text = "" // Hide hint
            } else {
                resumeHintAnimation() // Resume animation when focus is lost
            }
        }
    }

    private fun changeHintWithAnimation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isAnimationRunning) { // Only animate if allowed
                    animateHintChange(hintTexts[currentIndex])
                    currentIndex = (currentIndex + 1) % hintTexts.size // Loop through hints
                }
                handler.postDelayed(this, 3000) // Change every 3 seconds
            }
        }, 3000)
    }

    private fun animateHintChange(newHint: String) {
        // Move the current hint up and fade out
        ObjectAnimator.ofFloat(animatedHint, "translationY", 0f, -50f).apply {
            duration = 300
            start()
        }.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                if (isAnimationRunning) {
                    animatedHint.text = newHint
                    // Reset position from below and fade in
                    ObjectAnimator.ofFloat(animatedHint, "translationY", 50f, 0f).apply {
                        duration = 300
                        start()
                    }
                }
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    private fun stopHintAnimation() {
        isAnimationRunning = false
        handler.removeCallbacksAndMessages(null) // Stop scheduled animations
    }

    private fun resumeHintAnimation() {
        if (!isAnimationRunning) {
            isAnimationRunning = true
            changeHintWithAnimation() // Restart animation loop
        }
    }
}