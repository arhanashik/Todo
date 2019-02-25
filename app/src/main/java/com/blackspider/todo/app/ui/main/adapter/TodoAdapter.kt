package com.blackspider.todo.app.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.blackspider.todo.R
import com.blackspider.todo.app.data.local.todo.TodoEntity
import com.blackspider.todo.app.ui.main.callback.TodoClickEvent
import com.blackspider.todo.app.ui.main.holder.TodoHolder
import com.blackspider.todo.databinding.ItemTodoBinding

/*
* Recycler view Adpater.
* It shows the todoItems list.
* It needs a list of todoItems and a TodoClickEvent listener
*/
class TodoAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listOfTodo = ArrayList<TodoEntity>()
    private var listener: TodoClickEvent? = null

    fun setTodoList(listOfTodo: List<TodoEntity>) {
        this.listOfTodo.clear()
        this.listOfTodo.addAll(listOfTodo)
        notifyDataSetChanged()
    }

    fun addTodoList(listOfTodo: List<TodoEntity>) {
        this.listOfTodo.addAll(listOfTodo)
        notifyDataSetChanged()
    }

    fun addTodo(todo: TodoEntity) {
        this.listOfTodo.add(todo)
        notifyDataSetChanged()
    }

    fun getTodoList(): ArrayList<TodoEntity> {
        return listOfTodo
    }

    fun getIncompleteTodoList(): ArrayList<TodoEntity> {
        val incompleteList = ArrayList<TodoEntity>()
        listOfTodo.forEach {
            if(!it.completed) incompleteList.add(it)
        }

        return incompleteList
    }

    fun getCompletedTodoList(): ArrayList<TodoEntity> {
        val completedList = ArrayList<TodoEntity>()
        listOfTodo.forEach {
            if(it.completed) completedList.add(it)
        }

        return completedList
    }

    fun setListener(listener: TodoClickEvent) {
        this.listener = listener
    }

    fun clear() {
        listOfTodo.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ItemTodoBinding>(
            LayoutInflater.from(parent.context), R.layout.item_todo, parent, false
        )

        return TodoHolder(binding)
    }

    override fun getItemCount(): Int = listOfTodo.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as TodoHolder

        holder.bind(listOfTodo[position], listener)
    }
}