package com.limor.app.scenes.auth_new.data

data class UserNameStateBundle(val state: UserNameState, val params: List<String?>?) {

    val approved: Boolean
        get() = state == UserNameState.Approved

    companion object {
        fun Editing(): UserNameStateBundle = UserNameStateBundle(UserNameState.Editing, null)

        fun Error(variants: List<String?>): UserNameStateBundle =
            UserNameStateBundle(UserNameState.Error, variants)

        fun Approved(): UserNameStateBundle = UserNameStateBundle(UserNameState.Approved, null)
    }
}

enum class UserNameState {
    Editing,
    Error,
    Approved
}