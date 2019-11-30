package com.corrot.firenotes.utils

class Constants {
    companion object {
        const val USER_KEY: String = "USER"
        const val NOTE_KEY: String = "NOTE"

        const val MAIN_FRAGMENT_KEY = "mainFragment"
        const val SIGN_UP_FRAGMENT_KEY = "signUpFragment"
        const val SIGN_IN_FRAGMENT_KEY = "signInFragment"

        const val SAVE_STATE_ID = "saveStateId"
        const val SAVE_STATE_TITLE = "saveStateTitle"
        const val SAVE_STATE_BODY = "saveStateBody"
        const val SAVE_STATE_COLOR = "saveStateColor"

        const val FLAG_ADD_NOTE = 1
        const val FLAG_EDIT_NOTE = 2

        const val FLAG_NOTE_KEY = "noteFlag"

        const val NOTE_ID_KEY = "noteId"
        const val NOTE_TITLE_KEY = "noteTitle"
        const val NOTE_BODY_KEY = "noteBody"
        const val NOTE_COLOR_KEY = "noteColor"
        const val NOTE_LAST_CHANGED_KEY = "noteLastChanged"

        const val DRAWER_NOTES_ITEM = 1L
        const val DRAWER_LOG_OUT_ITEM = 2L

        const val GOOGLE_SIGN_IN_RESULT_CODE = 11

        const val FLAG_LOGIN_PROVIDER = "loginProviderFlag"
        const val LOGIN_PROVIDER_EMAIL_PASSWORD = 1
        const val LOGIN_PROVIDER_GOOGLE_ACCOUNT = 2
    }
}
