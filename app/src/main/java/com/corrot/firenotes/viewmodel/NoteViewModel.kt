package com.corrot.firenotes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.corrot.firenotes.FirebaseRepository
import com.corrot.firenotes.model.Note
import com.corrot.firenotes.utils.notifyObserver
import java.util.*

class NoteViewModel : ViewModel() {
    companion object {
        @JvmField
        val TAG: String = NoteViewModel::class.java.simpleName
    }

    private val firebaseRepository = FirebaseRepository()

    private var id: String? = null
    private var title: String? = null
    private var body:String? = null
    private var colorLiveData = MutableLiveData<Int>()

    var originalNote: Note? = null


    fun setId(id: String) {
        this.id = id
    }

    fun getId(): String? {
        return this.id
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getTitle(): String? {
        return this.title
    }

    fun setBody(body: String) {
        this.body = body
    }

    fun getBody(): String? {
        return this.body
    }

    fun setColor(color: Int) {
        colorLiveData.value = color
        colorLiveData.notifyObserver()
    }

    fun getColor(): LiveData<Int> {
        return this.colorLiveData
    }

    fun setNote(n: Note) {
        setId(n.id)
        n.title?.let { setTitle(it) }
        n.body?.let { setBody(it) }
        n.color?.let { setColor(it) }
    }

    fun getNote(): Note? {
        return if (!id.isNullOrEmpty()) {
            val n = Note(id!!)
            n.title = getTitle()
            n.body = getBody()
            n.color = getColor().value
            n
        } else {
            null
        }
    }

    fun addNoteToDatabase() {
        val id = getId()
        val title = getTitle()
        val body = getBody()
        val color = getColor().value
        val date = Calendar.getInstance().timeInMillis

        if (id.isNullOrEmpty()) {
            firebaseRepository.addNoteToDatabase(title, body, color, date)
        } else {
            firebaseRepository.editNoteFromDatabase(id, title, body, color, date)
        }
    }
}
