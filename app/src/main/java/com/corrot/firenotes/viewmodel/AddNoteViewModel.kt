package com.corrot.firenotes.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.corrot.firenotes.utils.notifyObserver

class AddNoteViewModel : ViewModel() {
    companion object {
        @JvmField
        val TAG: String = AddNoteViewModel::class.java.simpleName
    }

    var titleLiveData = MutableLiveData<String>()
    var bodyLiveData = MutableLiveData<String>()

    fun setTitle(title: String) {
        titleLiveData.value = title
        titleLiveData.notifyObserver()
    }

    fun setBody(title: String) {
        bodyLiveData.value = title
        bodyLiveData.notifyObserver()
    }
}