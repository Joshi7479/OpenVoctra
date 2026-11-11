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
import android.widget.Toast
import kotlin.random.Random
import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.singleOrNull

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dao = AppDatabase.get(this).entryDao()
        val voc = findViewById<TextView>(R.id.voc)
        val voctranslation = findViewById<TextView>(R.id.voctranslation)
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

            if (name.isEmpty() || translation.isEmpty()) {
                Toast.makeText(this, "Bitte beide Felder ausfüllen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val id = dao.insertNext(name, translation)
                Toast.makeText(this@MainActivity, "Gespeichert (#$id)", Toast.LENGTH_SHORT).show()
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
                val voccount = dao.getCount()
                if(voccount==0) {
                    voc.text = "Nothing saved yet!"
                    voctranslation.text = ""
                    return@launch
                }
                val randomint = Random.nextInt(1, voccount + 1)
                val entry = dao.getById(randomint.toLong())
                voc.text = entry?.title
                voctranslation.text = entry?.content
            }
        }

        exitbutton.setOnClickListener {
            maincard.visibility = View.GONE
            startscreen.visibility = View.VISIBLE
        }
    }
}