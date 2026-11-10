package com.example.openvoctra

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val maincard = findViewById<LinearLayout>(R.id.maincard)
        val mainedit = findViewById<LinearLayout>(R.id.mainedit)
        val startscreeen = findViewById<LinearLayout>(R.id.startscreen)
        val startbutton = findViewById<Button>(R.id.starttraining)
        val starteditbutton = findViewById<Button>(R.id.startedit)
        val savebutton = findViewById<Button>(R.id.save)
        val cancelbutton = findViewById<Button>(R.id.cancel)
        val exitbutton = findViewById<Button>(R.id.exit)

        starteditbutton.setOnClickListener {
            startscreeen.visibility = View.GONE
            mainedit.visibility = View.VISIBLE
        }
        cancelbutton.setOnClickListener {
            startscreeen.visibility = View.VISIBLE
            mainedit.visibility = View.GONE
        }
        startbutton.setOnClickListener {
            startscreeen.visibility = View.GONE
            maincard.visibility = View.VISIBLE
        }
        exitbutton.setOnClickListener {
            maincard.visibility = View.GONE
            startscreeen.visibility = View.VISIBLE
        }
    }
}