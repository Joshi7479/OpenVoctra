package com.example.openvoctra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class LanguageAdapter(
    private val allLocales: List<Locale>,
    private val onLocaleSelected: (Locale) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var filteredLocales: List<Locale> = allLocales

    inner class LanguageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLanguageName: TextView = view.findViewById(R.id.tvLanguageName)
        val tvNativeName: TextView = view.findViewById(R.id.tvNativeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val locale = filteredLocales[position]

        // Name in der Systemsprache (erster Buchstabe groß)
        val displayName = locale.getDisplayLanguage(Locale.getDefault())
            .replaceFirstChar { it.uppercase() }

        // Eigenname der Sprache (z.B. "日本語" für Japanisch)
        val nativeName = locale.getDisplayLanguage(locale)
            .replaceFirstChar { it.uppercase() }

        holder.tvLanguageName.text = displayName
        holder.tvNativeName.text = nativeName

        holder.itemView.setOnClickListener {
            onLocaleSelected(locale)
        }
    }

    override fun getItemCount(): Int = filteredLocales.size

    fun filter(query: String) {
        filteredLocales = if (query.isBlank()) {
            allLocales
        } else {
            val lowerQuery = query.lowercase()
            allLocales.filter { locale ->
                locale.getDisplayLanguage(Locale.getDefault()).lowercase().contains(lowerQuery) ||
                        locale.getDisplayLanguage(locale).lowercase().contains(lowerQuery) ||
                        locale.language.lowercase().contains(lowerQuery)
            }
        }
        notifyDataSetChanged()
    }
}

