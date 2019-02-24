package com.blackspider.todo.util.lib.firebase.callback

import com.blackspider.todo.app.data.local.user.UserEntity

interface CreateUserCallback {
    fun onResponse(user: UserEntity?, error: String?)
}