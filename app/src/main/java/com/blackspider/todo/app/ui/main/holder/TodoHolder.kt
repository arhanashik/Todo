package com.blackspider.todo.app.ui.main.holder

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blackspider.todo.R
import com.blackspider.todo.app.data.local.todo.TodoEntity
import com.blackspider.todo.app.ui.main.callback.TodoClickEvent
import com.blackspider.todo.databinding.ItemTodoBinding

class TodoHolder(private val binding: ItemTodoBinding): RecyclerView.ViewHolder(binding.root) {

    fun bind(todo: TodoEntity, callback: TodoClickEvent?) {
        binding.tvTodo.text = todo.todo

        // if the task is complete make the text gray, change the icon, and hide the edit button
        // else make it black, show different icon, and show the edit icon
        if(todo.completed) {
            binding.tvTodo.setTextColor(Color.GRAY)
            binding.btnTodoComplete.setImageResource(R.drawable.ic_done_all)
            binding.btnEdit.visibility = View.GONE
        }else {
            binding.tvTodo.setTextColor(Color.BLACK)
            binding.btnTodoComplete.setImageResource(R.drawable.ic_check)
            binding.btnEdit.visibility = View.VISIBLE
        }

        // toggle the task completion(complete/incomplete) with this action
        binding.btnTodoComplete.setOnClickListener {
            callback?.onClickTodo(todo, TodoClickEvent.ACTION_COMPLETE, adapterPosition)
        }

        // show the task's details
        binding.tvTodo.setOnClickListener {
            callback?.onClickTodo(todo, TodoClickEvent.ACTION_DETAILS, adapterPosition)
        }

        // edit task
        binding.btnEdit.setOnClickListener {
            callback?.onClickTodo(todo, TodoClickEvent.ACTION_EDIT, adapterPosition)
        }

        // delete the task
        binding.btnDelete.setOnClickListener {
            callback?.onClickTodo(todo, TodoClickEvent.ACTION_DELETE, adapterPosition)
        }
    }
}