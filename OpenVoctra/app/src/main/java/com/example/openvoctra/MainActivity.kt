package com.example.openvoctra

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import android.widget.Toast
import android.view.inputmethod.EditorInfo
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import androidx.lifecycle.repeatOnLifecycle

class MainActivity : AppCompatActivity() {

    private lateinit var ttsManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = findViewById<RecyclerView>(R.id.voclistrecyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        ttsManager = TextToSpeechManager(this)
        val adapter = ObjektAdapter(ttsManager)

        recyclerView.adapter = adapter

        var currentvoceditcontent: String = ""
        var currentvoceditname: String = ""
        var isfirstvoclistrun: Boolean = true

        val preferences = getSharedPreferences("learnlanguage", Context.MODE_PRIVATE)
        val writer = preferences.edit()

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
        val playbutton = findViewById<Button>(R.id.play)
        val setttingsdone = findViewById<Button>(R.id.settingsdone)
        val settingslayout = findViewById<LinearLayout>(R.id.settingslayout)
        val settingsbutton = findViewById<Button>(R.id.settings)
        val startlanguagepickerbutton = findViewById<Button>(R.id.startlanguagepicker)
        val currentlanguagedisplay = findViewById<TextView>(R.id.currentlanguage)
        val voclistbutton = findViewById<Button>(R.id.voclist)
        val voclistlayout = findViewById<LinearLayout>(R.id.voclistlayout)
        val starttrainingfromvoclist = findViewById<Button>(R.id.starttrainingfromvoclist)
        val voclistdone = findViewById<Button>(R.id.leavevoclist)
        val voclisthint = findViewById<TextView>(R.id.voclisthint)

        writer.putBoolean("isfirstvoclistrun", isfirstvoclistrun)

        var vocnumber = 1

        val usedrandom = mutableSetOf<Int>()
        var currentId = 0
        var lastvoc = 0

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.observeAll().collect { liste ->
                    adapter.submitList(liste)
                }
            }
        }

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
                val count = dao.getCount()
                if (count == 0) {
                    vocnumber = 0
                    vocnumberedit.setText("")
                    vocnameedit.setText("Nothing saved yet!")
                    voctranslationedit.setText("")
                    return@launch
                }
                vocnumber = vocnumber.coerceIn(1, count)
                vocnumberedit.setText(vocnumber.toString())
                val entry = dao.getById(vocnumber.toLong())
                if (entry != null) {
                    currentvoceditname = entry.title
                    currentvoceditcontent = entry.content
                    vocnameedit.setText(entry.title)
                    voctranslationedit.setText(entry.content)
                }
            }
        }

        fun getAllLanguages(): List<Locale> {
            return Locale.getAvailableLocales()
                .filter { it.language.isNotEmpty() }
                .distinctBy { it.language }
                .map { Locale(it.language) }
                .sortedBy { it.getDisplayLanguage(Locale.getDefault()) }
        }

        fun showLanguagePickerDialog(onResult: (Locale) -> Unit) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_language_picker, null)
            val etSearch = dialogView.findViewById<EditText>(R.id.etSearchLanguage)
            val rvLanguages = dialogView.findViewById<RecyclerView>(R.id.rvLanguages)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.chooselanguage))
                .setView(dialogView)
                .setNegativeButton(getString(R.string.cancel), null)
                .create()

            val adapter = LanguageAdapter(getAllLanguages()) { selectedLocale ->
                dialog.dismiss()
                onResult(selectedLocale)
            }

            rvLanguages.layoutManager = LinearLayoutManager(this)
            rvLanguages.adapter = adapter

            etSearch.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    adapter.filter(s.toString())
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            dialog.show()
        }

        voclistdone.setOnClickListener {
            voclistlayout.visibility = View.GONE
            startscreen.visibility = View.VISIBLE
        }

        starttrainingfromvoclist.setOnClickListener {
            usedrandom.clear()
            voclistlayout.visibility = View.GONE
            maincard.visibility = View.VISIBLE
            newvoc(0)
        }

        voclistbutton.setOnClickListener {
            if(isfirstvoclistrun) {
                voclisthint.setText(R.string.voclisthint)
            } else {
                voclisthint.setText("")
            }

            startscreen.visibility = View.GONE
            voclistlayout.visibility = View.VISIBLE
            isfirstvoclistrun = false
            writer.putBoolean("isfirstvoclistrun", isfirstvoclistrun)
        }

        startlanguagepickerbutton.setOnClickListener {
            showLanguagePickerDialog { selectedLocale ->
                writer.putString("learnlanguage", selectedLocale.language).apply()
            }
        }

        settingsbutton.setOnClickListener {
            val language = preferences.getString("learnlanguage", "en") ?: "en"
            startscreen.visibility = View.GONE
            settingslayout.visibility = View.VISIBLE
            currentlanguagedisplay.setText(getString(R.string.currentlanguage) + language)
        }

        setttingsdone.setOnClickListener {
            settingslayout.visibility = View.GONE
            startscreen.visibility = View.VISIBLE
        }

        playbutton.setOnClickListener {
            val language = preferences.getString("learnlanguage", "en") ?: "en"

            ttsManager.setlanguage(Locale.getDefault())
            ttsManager.speak(currentvoceditname) {
                runOnUiThread {
                    ttsManager.setlanguage(Locale(language))
                    ttsManager.speak(currentvoceditcontent)
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

        starteditbutton.setOnClickListener {
            startscreen.visibility = View.GONE
            mainedit.visibility = View.VISIBLE
            newvocedit()
        }

        vocnumberedit.setOnEditorActionListener { v, actionid, event ->
            if (actionid == EditorInfo.IME_ACTION_GO) {
                vocnumber = vocnumberedit.text.toString().toIntOrNull() ?: vocnumber
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
                Toast.makeText(this, getString(R.string.bothfields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                dao.deleteAndCompact(vocnumber.toLong())
                val id = dao.insertNext(name, translation)
                Toast.makeText(this@MainActivity, getString(R.string.saved) + "(#$id)", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, getString(R.string.bothfields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val id = dao.insertNext(name, translation)
                Toast.makeText(this@MainActivity, getString(R.string.saved) + "(#$id)", Toast.LENGTH_SHORT).show()
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