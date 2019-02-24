package com.blackspider.todo.util.lib.firebase.callback

import com.blackspider.todo.app.data.local.todo.TodoEntity

interface TodoListCallback {
    fun onResponse(todoList: ArrayList<TodoEntity>?, error: String?)
}