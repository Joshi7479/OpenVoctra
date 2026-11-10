package com.example.openvoctra

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dao = AppDatabase.get(this).entryDao()
        val voctitle = findViewById<TextView>(R.id.voctitle)
        val maincard = findViewById<LinearLayout>(R.id.maincard)
        val mainedit = findViewById<LinearLayout>(R.id.mainedit)
        val titleInput = findViewById<EditText>(R.id.vocname)
        val vocInput = findViewById<EditText>(R.id.voctranslationedit)
        val startscreen = findViewById<LinearLayout>(R.id.startscreen)
        val startbutton = findViewById<Button>(R.id.starttraining)
        val starteditbutton = findViewById<Button>(R.id.startedit)
        val savebutton = findViewById<Button>(R.id.save)
        val cancelbutton = findViewById<Button>(R.id.cancel)
        val exitbutton = findViewById<Button>(R.id.exit)

        savebutton.setOnClickListener {
            val name = titleInput.text.toString().trim()
            val translation = vocInput.text.toString().trim()
            if (name.isEmpty() || translation.isEmpty()) return@setOnClickListener

            lifecycleScope.launch {
                dao.insert(Entry(title = name, content = translation))
                titleInput.text.clear()
                vocInput.text.clear()
            }
        }

        starteditbutton.setOnClickListener {
            startscreen.visibility = View.GONE
            mainedit.visibility = View.VISIBLE
        }

        cancelbutton.setOnClickListener {
            startscreen.visibility = View.VISIBLE
            mainedit.visibility = View.GONE
        }

        startbutton.setOnClickListener {
            startscreen.visibility = View.GONE
            maincard.visibility = View.VISIBLE

            lifecycleScope.launch {
                val list = dao.getAllOnce()
                voctitle.text = list.firstOrNull()?.title ?: "Nothing saved yet."
            }
        }

        exitbutton.setOnClickListener {
            maincard.visibility = View.GONE
            startscreen.visibility = View.VISIBLE
        }
    }
}