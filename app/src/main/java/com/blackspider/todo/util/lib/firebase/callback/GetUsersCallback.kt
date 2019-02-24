package com.blackspider.todo.util.lib.firebase.callback

import com.blackspider.todo.app.data.local.user.UserEntity

interface GetUsersCallback {
    fun onResponse(users: ArrayList<UserEntity>?, error: String?)
}