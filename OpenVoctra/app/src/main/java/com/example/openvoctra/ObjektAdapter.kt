package com.example.openvoctra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import android.content.Context
import android.os.Handler
import android.content.Intent
import kotlinx.coroutines.launch
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ObjektAdapter(
    private val ttsManager: TextToSpeechManager,
    private val onItemClick: (newtodo: String, todoobject: Int) -> Unit
) : ListAdapter<Entry, ObjektAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemTextView)
        val playButton: Button = view.findViewById(R.id.item_play)
        val dao = AppDatabase.get(view.context).entryDao()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.voclist_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentobjekt = getItem(position)
        holder.textView.text =
            "${currentobjekt.id}. ${currentobjekt.title} - ${currentobjekt.content}"

        holder.playButton.setOnClickListener { view ->
            val context = view.context
            val preferences = context.getSharedPreferences("learnlanguage", Context.MODE_PRIVATE)
            val language = preferences.getString("learnlanguage", "en") ?: "en"

            ttsManager.setlanguage(Locale.getDefault())
            ttsManager.speak(currentobjekt.title) {
                Handler(Looper.getMainLooper()).post {
                    ttsManager.setlanguage(Locale(language))
                    ttsManager.speak(currentobjekt.content)
                }
            }
        }
        holder.textView.setOnClickListener {
            val newtodo = "openeditor"
            val newtodoobject = currentobjekt.id

            onItemClick(newtodo, newtodoobject.toInt())

        }
        holder.textView.setOnLongClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                holder.dao.deleteAndCompact(currentobjekt.id)
            }
            true
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) =
            oldItem == newItem
    }
}