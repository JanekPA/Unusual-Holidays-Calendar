package com.example.k.models

import android.os.Parcel
import android.os.Parcelable

data class ListItem(var name: String) : Parcelable {
    var itemId: Int = 0
        private set

    companion object {
        private var nextItemId = 1
        @JvmField
        val CREATOR: Parcelable.Creator<ListItem> = object : Parcelable.Creator<ListItem> {
            override fun createFromParcel(parcel: Parcel): ListItem {
                return ListItem(parcel)
            }

            override fun newArray(size: Int): Array<ListItem?> {
                return arrayOfNulls(size)
            }
        }
    }

    init {
        itemId = nextItemId++
    }

    constructor(parcel: Parcel) : this(parcel.readString()!!) {
        itemId = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(itemId)
    }

    override fun describeContents(): Int {
        return 0
    }
}