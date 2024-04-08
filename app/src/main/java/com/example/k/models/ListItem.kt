package com.example.k.models

data class ListItem(var name : String) {
    var itemId: Int = 0
        private set

    companion object {
        private var nextItemId = 1
    }

    init {
        itemId = nextItemId++
    }
}
