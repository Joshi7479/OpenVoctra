package com.example.openvoctra

import android.os.Bundle
import android.content.Context
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
import android.view.inputmethod.EditorInfo
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.singleOrNull
import kotlin.random.nextInt
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("is_first_run", true)

        if(isFirstRun) {
            private val requestetmicpermission = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {isGranted: Boolean ->
                if(isGranted) {
                    Toast.makeText(this, "Thank you.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Okay.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val dao = AppDatabase.get(this).entryDao()
        val voc = findViewById<TextView>(R.id.voc)
        val voctranslation = findViewById<TextView>(R.id.voctranslation)
        val rightbutton = findViewById<Button>(R.id.right)
        val wrongbutton = findViewById<Button>(R.id.wrong)
        val maincard = findViewById<LinearLayout>(R.id.maincard)
        val mainadd = findViewById<LinearLayout>(R.id.mainadd)
        val titleInput = findViewById<EditText>(R.id.vocname)
        val vocInput = findViewById<EditText>(R.id.voctranslationadd)
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
        val previousbutton = findViewById<Button>(R.id.last)
        val nextbutton = findViewById<Button>(R.id.next)
        val vocnumberedit = findViewById<EditText>(R.id.voceditnumber)
        val deletbutton = findViewById<Button>(R.id.delete)
        val showtranslation = findViewById<Button>(R.id.showtranslation)
        var vocnumber = 1

        val usedrandom = mutableSetOf<Int>()
        var currentId = 0
        var lastvoc = 0

        fun newvoc(right: Int) {
            voctranslation.visibility = View.GONE
            showtranslation.visibility = View.VISIBLE
            lifecycleScope.launch {
                val voccount = dao.getCount()

                if (voccount == 0) {
                    voc.text = "Nothing saved yet!"
                    voctranslation.text = ""
                    return@launch
                }

                if (right == 1 && currentId != 0) {
                    usedrandom.add(currentId)
                }

                if (usedrandom.size >= voccount) {
                    usedrandom.clear()
                    currentId = 0
                    maincard.visibility = View.GONE
                    startscreen.visibility = View.VISIBLE
                    return@launch
                }

                val remaining = (1..voccount).filter { it !in usedrandom && it != lastvoc }
                    .ifEmpty { (1..voccount).filter { it !in usedrandom } }
                currentId = remaining.random()
                lastvoc = currentId

                val entry = dao.getById(currentId.toLong())
                voc.text = entry?.title
                voctranslation.text = entry?.content
            }
        }

        fun newvocedit() {
            lifecycleScope.launch {
                vocnumberedit.setText(vocnumber.toString())
                val currentvocedit = dao.getById(vocnumber.toLong())
                if(currentvocedit != null) {
                    voctranslationedit.setText(currentvocedit?.content)
                    vocnameedit.setText(currentvocedit?.title)
                } else {
                    vocnameedit.setText("Nothing saved yet!")
                    voctranslationedit.setText("")
                    if(vocnumber > 0) {
                        vocnumber -= 1
                    } else {
                        vocnumber +=1
                    }
                    newvocedit()
                }
            }

        }

        showtranslation.setOnClickListener {
            voctranslation.visibility = View.VISIBLE
            showtranslation.visibility = View.GONE
        }

        deletbutton.setOnClickListener {
            lifecycleScope.launch {
                dao.deleteAndCompact(vocnumber.toLong())
            }
            newvocedit()
        }

        startbutton.setOnClickListener {
            usedrandom.clear()
            currentId = 0
            startscreen.visibility = View.GONE
            maincard.visibility = View.VISIBLE
            newvoc(0)
        }

        starteditbutton.setOnClickListener {
            startscreen.visibility = View.GONE
            mainedit.visibility = View.VISIBLE
        }

        vocnumberedit.setOnEditorActionListener { v, actionid, event ->
            if (actionid == EditorInfo.IME_ACTION_GO) {
                vocnumber = vocnumberedit.text.toString().toInt()
                newvocedit()
                true
            } else {
                false
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
                dao.deleteAndCompact(vocnumber.toLong())
                val id = dao.insertNext(name, translation)
                Toast.makeText(this@MainActivity, "Gespeichert (#$id)", Toast.LENGTH_SHORT).show()
                titleInput.text.clear()
                vocInput.text.clear()
            }
            vocnumber += 1
            newvocedit()
        }

        nextbutton.setOnClickListener {
            vocnumber += 1
            newvocedit()
        }

        previousbutton.setOnClickListener {
            vocnumber -= 1
            newvocedit()
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

            newvoc(0)
        }

        cancelbutton1.setOnClickListener {
            startscreen.visibility = View.VISIBLE
            mainadd.visibility = View.GONE
        }

        startbutton.setOnClickListener {
            usedrandom.clear()
            startscreen.visibility = View.GONE
            maincard.visibility = View.VISIBLE
            newvoc(0)
        }

        wrongbutton.setOnClickListener {
            newvoc(0)
        }

        rightbutton.setOnClickListener {
            newvoc(1)
        }

        exitbutton.setOnClickListener {
            maincard.visibility = View.GONE
            startscreen.visibility = View.VISIBLE
        }
    }
}