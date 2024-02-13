package com.example.texttospeech.utilites

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun observe(view: View, action: () -> Unit) {
    view.setOnClickListener { action() }
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this) { t -> action(t) }
}
