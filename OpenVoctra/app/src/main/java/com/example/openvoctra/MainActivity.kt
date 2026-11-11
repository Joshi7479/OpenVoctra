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
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dao = AppDatabase.get(this).entryDao()
        val voc = findViewById<TextView>(R.id.voc)
        val voctranslation = findViewById<TextView>(R.id.voctranslation)
        val rightbutton = findViewById<Button>(R.id.right)
        val wrongbutton = findViewById<Button>(R.id.wrong)
        val maincard = findViewById<LinearLayout>(R.id.maincard)
        val mainadd = findViewById<LinearLayout>(R.id.mainadd)
        val titleInput = findViewById<EditText>(R.id.vocname)
        val vocInput = findViewById<EditText>(R.id.voctranslationedit)
        val startscreen = findViewById<LinearLayout>(R.id.startscreen)
        val startbutton = findViewById<Button>(R.id.starttraining)
        val startaddbutton = findViewById<Button>(R.id.startadd)
        val starteditbutton = findViewById<Button>(R.id.startedit)
        val savebutton1 = findViewById<Button>(R.id.save1)
        val cancelbutton1 = findViewById<Button>(R.id.cancel1)
        val savebutton2 = findViewById<Button>(R.id.save2)
        val cancelbutton2 = findViewById<Button>(R.id.cancel2)
        val exitbutton = findViewById<Button>(R.id.exit)
        val mainedit = findViewById<LinearLayout>(R.id.mainedit)
        val vocnameedit = findViewById<EditText>(R.id.vocnamedit)
        val voctranslationedit = findViewById<EditText>(R.id.voctranslationedit)
        val vocnumber = 1

        val usedrandom = mutableSetOf<Int>()

        fun newvoc(){
            lifecycleScope.launch {
                val voccount = dao.getCount()

                if(usedrandom.size >= voccount) {
                    usedrandom.clear()
                    maincard.visibility = View.GONE
                    startscreen.visibility = View.VISIBLE
                }
                if(voccount==0) {
                    voc.text = "Nothing saved yet!"
                    voctranslation.text = ""
                    return@launch
                }
                var randomint: Int
                do {
                    randomint = Random.nextInt(1, voccount + 1)
                } while (randomint in usedrandom)

                usedrandom.add(randomint)

                val entry = dao.getById(randomint.toLong())
                voc.text = entry?.title
                voctranslation.text = entry?.content
            }
        }

        starteditbutton.setOnClickListener {
            mainedit.visibility = View.VISIBLE
            lifecycleScope.launch {
                val currentvocedit = dao.getById(vocnumber.toLong())
                voctranslationedit.setText(currentvocedit?.content)
                vocnameedit.setText(currentvocedit?.title)
            }
        }

        savebutton2.setOnClickListener {
            val name = vocnameedit.text.toString().trim()
            val translation = voctranslationedit.text.toString().trim()

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

        cancelbutton2.setOnClickListener {
            startscreen.visibility = View.VISIBLE
            mainedit.visibility = View.GONE
        }


        savebutton1.setOnClickListener {
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

        startaddbutton.setOnClickListener {
            startscreen.visibility = View.GONE
            mainadd.visibility = View.VISIBLE

            newvoc()
        }

        cancelbutton1.setOnClickListener {
            startscreen.visibility = View.VISIBLE
            mainadd.visibility = View.GONE
        }

        startbutton.setOnClickListener {
            usedrandom.clear()
            startscreen.visibility = View.GONE
            maincard.visibility = View.VISIBLE
            newvoc()
        }

        wrongbutton.setOnClickListener {
            newvoc()
        }

        rightbutton.setOnClickListener {
            newvoc()

        }

        exitbutton.setOnClickListener {
            maincard.visibility = View.GONE
            startscreen.visibility = View.VISIBLE
        }
    }
}