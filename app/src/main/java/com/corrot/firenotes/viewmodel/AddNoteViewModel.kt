package com.corrot.firenotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.utils.notifyObserver
import java.util.*

class AddNoteViewModel : ViewModel() {
    companion object {
        @JvmField
        val TAG: String = AddNoteViewModel::class.java.simpleName
    }

    val firebaseRepository = FirebaseRepository()

    var titleLiveData = MutableLiveData<String>()
    var bodyLiveData = MutableLiveData<String>()
    var colorLiveData = MutableLiveData<Int>()

    fun setTitle(title: String) {
        titleLiveData.value = title
        titleLiveData.notifyObserver()
    }

    fun getTitle(): LiveData<String> {
        return this.titleLiveData
    }

    fun setBody(title: String) {
        bodyLiveData.value = title
        bodyLiveData.notifyObserver()
    }

    fun getBody(): LiveData<String> {
        return this.bodyLiveData
    }

    fun setColor(color: Int) {
        colorLiveData.value = color
        colorLiveData.notifyObserver()
    }

    fun getColor(): LiveData<Int> {
        return this.colorLiveData
    }

    fun addNoteToDatabase() {
        val title = getTitle().value
        val body = getBody().value
        val color = getColor().value
        val date = Calendar.getInstance().timeInMillis

        val firebaseRepository = FirebaseRepository()
        firebaseRepository.addNoteToDatabase(title, body, color, date)
    }
}
