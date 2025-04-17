package com.nithra.invoicegenerator

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nithra.invoice_generator_tool.activity.InvoiceHomeScreen

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextSearch = findViewById(R.id.editTextSearch)
        animatedHint = findViewById(R.id.animatedHint)
        searchIcon = findViewById(R.id.searchIcon)

        val intent = Intent(this@MainActivity, InvoiceHomeScreen::class.java)
        intent.putExtra("AppLogin", 0)
        intent.putExtra("AppLoginFrom", "")
        intent.putExtra("OpenFromHome", 0)
        startActivity(intent)

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