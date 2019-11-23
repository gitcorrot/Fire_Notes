package com.corrot.firenotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.utils.notifyObserver
import java.util.*

class NoteViewModel : ViewModel() {
    companion object {
        @JvmField
        val TAG: String = NoteViewModel::class.java.simpleName
    }

    private val firebaseRepository = FirebaseRepository()
    private var titleLiveData = MutableLiveData<String>()
    private var bodyLiveData = MutableLiveData<String>()
    private var colorLiveData = MutableLiveData<Int>()

    fun setTitle(title: String) {
        titleLiveData.value = title
        titleLiveData.notifyObserver()
    }

    fun getTitle(): LiveData<String> {
        return this.titleLiveData
    }

    fun setBody(body: String) {
        bodyLiveData.value = body
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

        firebaseRepository.addNoteToDatabase(title, body, color, date)
    }
}
