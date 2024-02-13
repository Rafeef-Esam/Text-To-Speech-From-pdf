package com.example.texttospeech.data

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable


data class Attachment(
    var attachmentUri : Uri? = null,
    var attachmentFileName: String? = null
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(attachmentUri, flags)
        parcel.writeString(attachmentFileName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Attachment> {
        override fun createFromParcel(parcel: Parcel): Attachment {
            return Attachment(parcel)
        }

        override fun newArray(size: Int): Array<Attachment?> {
            return arrayOfNulls(size)
        }
    }
}
