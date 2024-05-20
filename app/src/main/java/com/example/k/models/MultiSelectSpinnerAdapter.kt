package com.example.k.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.k.R

class MultiSelectSpinnerAdapter(context: Context,
                                val items: List<ListItem>,
                                private val selectedItems: MutableList<ListItem>) : ArrayAdapter<ListItem>(context, 0, items) {
    private val checkedItems = BooleanArray(items.size)
    private var onItemSelectedListener: OnItemSelectedListener? = null

    interface OnItemSelectedListener {
        fun onItemSelected(
            selectedItems: List<ListItem>,
            pos: Int
        )
    }

    init {
        for (i in items.indices) {
            checkedItems[i] = selectedItems.contains(items[i])
        }
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        this.onItemSelectedListener = listener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent, true)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup, isDropDown: Boolean): View {
        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(
                if (isDropDown) R.layout.custom_spinner_dropdown_item else R.layout.custom_non_select_dropdown,
                parent,
                false
            )
        val textView = view.findViewById<TextView>(R.id.spin_txt)

        if (isDropDown) {
            val checkBox = view.findViewById<CheckBox>(R.id.spinnerCheckbox)
            val itemName = view.findViewById<TextView>(R.id.itemName)

            itemName.text = items[position].name

            checkBox.isChecked = checkedItems[position]

            checkBox.setOnCheckedChangeListener(null) // Usuń starego listenera, żeby uniknąć konfliktów
            checkBox.setOnClickListener {
                checkedItems[position] = checkBox.isChecked
                if (checkBox.isChecked) {
                    if (!selectedItems.contains(items[position])) {
                        selectedItems.add(items[position])
                    }
                } else {
                    selectedItems.remove(items[position])
                }
                notifyDataSetChanged()
                onItemSelectedListener?.onItemSelected(selectedItems, position)
            }
        } else {
            textView.text = if (selectedItems.isEmpty()) {
                "Select"
            } else {
                val names: ArrayList<String> = ArrayList()
                for (i in getSelectedItems().indices) {
                    names.add(getSelectedItems()[i].name)
                }
                names.toString().replace("[", "").replace("]", "")
            }

            // Update checkbox state
            val checkBox = view.findViewById<CheckBox>(R.id.spinnerCheckbox)
            checkBox.isChecked = checkedItems[position]
            checkBox.setOnClickListener(null) // Usuń listener, aby uniknąć konfliktów
        }

        return view
    }

    private fun getSelectedItems(): List<ListItem> {
        return selectedItems
    }

    fun setSelectedPositions(positions: List<Int>) {
        selectedItems.clear()
        positions.forEach { index ->
            if (index in items.indices) {
                val item = items[index]
                selectedItems.add(item)
            }
        }
        notifyDataSetChanged()
    }

    fun updateSelectedItems(names: List<String>) {
        for (i in items.indices) {
            val name = items[i].name
            val isChecked = names.contains(name)
            checkedItems[i] = isChecked // Aktualizacja zaznaczenia w tablicy checkedItems
            if (isChecked) {
                if (!selectedItems.contains(items[i])) {
                    selectedItems.add(items[i]) // Dodaj element do listy zaznaczonych, jeśli jest zaznaczony
                }
            } else {
                selectedItems.remove(items[i]) // Usuń element z listy zaznaczonych, jeśli nie jest zaznaczony
            }
        }
        notifyDataSetChanged() // Powiadom adapter o zmianach w danych
    }
}