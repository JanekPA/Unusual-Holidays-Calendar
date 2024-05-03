package com.example.k.models

import java.io.Serializable

data class ListItem(var name : String) : Serializable{
    var itemId: Int = 0
        private set

    companion object {
        private var nextItemId = 1
    }

    init {
        itemId = nextItemId++
    }
}
