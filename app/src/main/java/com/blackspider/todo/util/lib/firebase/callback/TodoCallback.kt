package com.blackspider.todo.util.lib.firebase.callback

import com.blackspider.todo.app.data.local.todo.TodoEntity

interface TodoCallback {
    fun onResponse(todo: TodoEntity?, error: String?)
}