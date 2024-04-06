package com.example.k.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.k.R

class MultiSelectSpinnerAdapter(context : Context,
                                private val items: List<ListItem>,
private val selectedItems : MutableList<ListItem>) : ArrayAdapter<ListItem>(context,0,items) {
    private val checkedItems = BooleanArray(items.size)
    private var onItemSelectedListener : OnItemSelectedListener ?=null

    interface OnItemSelectedListener{
        fun onItemSelected(
            selectedItems: List<ListItem>,
            pos: Int
        )
    }
    init {
        for (i in items.indices)
        {
            checkedItems[i] = selectedItems.contains(items[i])
        }
    }
    fun setOnItemSelectedListener(listener: OnItemSelectedListener?)
    {
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
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                checkedItems[position] = isChecked
                if (isChecked) {
                    if (!selectedItems.contains(items[position])) {
                        selectedItems.add(items[position])
                    }

                } else {
                    selectedItems.remove(items[position])

                }
                notifyDataSetChanged()
                onItemSelectedListener?.onItemSelected(selectedItems,position)

            }
        } else {
            if (selectedItems.isEmpty()){
                textView.text = "Select"
            }
            else{
                var names : ArrayList<String>  = ArrayList()
                for (i in getSelectedItems().indices){
                    names.add(getSelectedItems()[i].name)
                }
                textView.text = names.toString().replace("[","").replace("]","")

            }
        }

        return view
    }

    private fun getSelectedItems(): List<ListItem>  {
        return selectedItems
    }

}