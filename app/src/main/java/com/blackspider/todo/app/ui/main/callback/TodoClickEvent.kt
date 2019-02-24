package com.blackspider.todo.app.ui.main.callback

import com.blackspider.todo.app.data.local.todo.TodoEntity

interface TodoClickEvent {

    companion object {
        // actions are used to make the callback method generic for all
        const val ACTION_COMPLETE = "complete"
        const val ACTION_DETAILS = "details"
        const val ACTION_EDIT = "edit"
        const val ACTION_DELETE = "delete"
    }

    fun onClickTodo(todo: TodoEntity, action: String, position: Int)
}