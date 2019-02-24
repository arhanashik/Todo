package com.blackspider.todo.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blackspider.todo.R
import com.blackspider.todo.app.data.local.Const
import com.blackspider.todo.app.data.local.todo.TodoEntity
import com.blackspider.todo.app.data.local.user.UserEntity
import com.blackspider.todo.app.ui.helper.AuthHelper
import com.blackspider.todo.app.ui.helper.Validator
import com.blackspider.todo.app.ui.main.adapter.TodoAdapter
import com.blackspider.todo.app.ui.main.callback.TodoClickEvent
import com.blackspider.todo.databinding.PromptTodoBinding
import com.blackspider.todo.util.helper.*
import com.blackspider.todo.util.lib.firebase.FireStoreService
import com.blackspider.todo.util.lib.firebase.callback.CreateUserCallback
import com.blackspider.todo.util.lib.firebase.callback.TodoCallback
import com.blackspider.todo.util.lib.firebase.callback.TodoListCallback
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), AuthHelper {

    // firestore serveice class instance
    private val remote: FireStoreService by lazy { FireStoreService() }

    private var currentUserEntity: UserEntity? = null

    private val adapter = TodoAdapter()

    // menu for clearing all completed tasks at once
    private var clearCompletedMenu: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initView()

        // check session
        if(currentUser() == null) {
            signIn(this)
        }else {
            currentUserEntity = remote.getUserEntity(currentUser()!!)
            updateStatus()
            //loadTodoList()
            addTodoListListener()
        }
    }

    private fun initView() {
        // init recycler view
        adapter.setListener(object: TodoClickEvent{
            override fun onClickTodo(todo: TodoEntity, action: String, position: Int) {
                when(action) {
                    TodoClickEvent.ACTION_COMPLETE -> toggleMarkAsComplete(todo, position)
                    TodoClickEvent.ACTION_DETAILS -> showDetails(todo)
                    TodoClickEvent.ACTION_EDIT -> editTodo(todo, position)
                    TodoClickEvent.ACTION_DELETE -> deleteTodo(todo, position)
                }
            }
        })

        rv_todo_list.layoutManager = LinearLayoutManager(this)
        rv_todo_list.adapter = adapter
        rv_todo_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when(newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> if(!isAnimating) btn_add_todo.slideDown(150)
                    RecyclerView.SCROLL_STATE_IDLE -> if(!isAnimating) btn_add_todo.slideUp(150)
                }
            }
        })

        //add button click and swipe refresh listener
        btn_add_todo.setOnClickListener { addTodo() }
        swipe_refresh.setOnRefreshListener { loadTodoList() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Const.RequestCode.AUTH) {
            // Firebase auth success
            if (resultCode == RESULT_OK) createUser() else {
                // Firebase auth failed
                val response = IdpResponse.fromResultIntent(data)
                when {
                    response == null ->
                        Toaster(this).showToast(getString(R.string.sign_in_required_exception))
                    response.error!!.errorCode == ErrorCodes.NO_NETWORK ->
                        Toaster(this).showToast(getString(R.string.no_internet_connection_exception))
                    else -> Toaster(this).showToast(response.error!!.message!!)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        clearCompletedMenu = menu!!.getItem(1)
        clearCompletedMenu?.isVisible = currentUserEntity != null
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_account -> {
                // switch account
                if(currentUserEntity == null) signIn(this) else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.text_are_you_sure)
                        .setMessage(R.string.switch_account_warning)
                        .setPositiveButton(R.string.label_switch_account) { _,_-> switchAccount() }
                        .setNegativeButton(R.string.label_cancel) { _,_-> }
                        .create()
                        .show()
                }
            }
            R.id.action_clear_completed_task -> {
                //clear complete tasks
                clearCompletedTasks()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // create new user based on the firebase auth data
    private fun createUser() {
        swipe_refresh.isRefreshing = true
        remote.createUser(currentUser()!!, object: CreateUserCallback {
            override fun onResponse(user: UserEntity?, error: String?) {
                swipe_refresh.isRefreshing = false
                if(error == null) {
                    currentUserEntity = user
                    Toaster(this@MainActivity).showToast("Welcome ${currentUser()!!.displayName}")
                    updateStatus()
                    //loadTodoList()
                    addTodoListListener()
                }else {
                    Toaster(this@MainActivity).showToast(error)
                }
            }
        })
    }

    // load TodoList of current user without listener
    private fun loadTodoList() {
        if(currentUserEntity == null) { signIn(this); return }

        swipe_refresh.isRefreshing = true
        remote.getTodoList(currentUserEntity!!, object: TodoListCallback{
            override fun onResponse(todoList: ArrayList<TodoEntity>?, error: String?) {
                swipe_refresh.isRefreshing = false
                if (error != null) {
                    Toaster(this@MainActivity).showToast(error)
                }else {
                    if(todoList!!.size > 0) {
                        img_no_data.visibility = View.INVISIBLE
                        rv_todo_list.visibility = View.VISIBLE
                        adapter.setTodoList(todoList)
                        updateStatus()
                    }else {
                        img_no_data.visibility = View.VISIBLE
                        rv_todo_list.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    //show todoItem's details
    private fun showDetails(todo: TodoEntity) {
        val details = "Title: ${todo.todo}\nDate: ${todo.date}"
        AlertDialog.Builder(this)
            .setTitle(R.string.label_details)
            .setMessage(details)
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()
            .show()
    }

    // add new todoItem with custom alert dialog
    private fun addTodo() {
        if(currentUserEntity == null) { signIn(this); return }

        val binding = DataBindingUtil.inflate<PromptTodoBinding>(
            layoutInflater, R.layout.prompt_todo, null, false
        )

        val dateNow = FormatUtil().formatDate(Date(), FormatUtil.dd_MMM_yyyy)
        binding.tietTodoDate.text = SpannableStringBuilder(dateNow)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.label_add_todo)
            .setView(binding.root)
            .setPositiveButton(R.string.label_add_todo) {
                    _, _ ->
                swipe_refresh.isRefreshing = true

                val todoTitle = binding.tietTodoTitle.text.toString()
                val date = binding.tietTodoDate.text.toString()

                val todo = TodoEntity(
                    "", todoTitle, false, date, currentUserEntity?.id!!, ""
                )

                remote.addTodo(todo, object: TodoCallback {
                    override fun onResponse(todo: TodoEntity?, error: String?) {
                        swipe_refresh.isRefreshing = false
                        if(error == null) {
                            Toaster(this@MainActivity).showToast(getString(R.string.add_todo_success_message))
//                            img_no_data.visibility = View.INVISIBLE
//                            rv_todo_list.visibility = View.VISIBLE
//                            adapter.addTodo(todo!!)
                        }else {
                            Toaster(this@MainActivity).showToast(error)
                        }
                    }
                })
            }
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        dialog.show()

        Validator.forceValidation(arrayOf(binding.tietTodoTitle, binding.tietTodoDate), dialog)
    }

    // edit an existing and incomplete todoItem
    private fun editTodo(todo: TodoEntity, position: Int) {

        val binding = DataBindingUtil.inflate<PromptTodoBinding>(
            layoutInflater, R.layout.prompt_todo, null, false
        )

        binding.tietTodoTitle.text = SpannableStringBuilder(todo.todo)
        binding.tietTodoDate.text = SpannableStringBuilder(todo.date)
        binding.tietTodoTitle.setSelection(todo.todo.length)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.label_edit_todo)
            .setView(binding.root)
            .setPositiveButton(R.string.label_update_todo) {
                    _, _ ->
                swipe_refresh.isRefreshing = true

                val todoTitle = binding.tietTodoTitle.text.toString()
                val date = binding.tietTodoDate.text.toString()

                todo.todo = todoTitle
                todo.date = date

                remote.updateTodo(todo, object: TodoCallback {
                    override fun onResponse(todo: TodoEntity?, error: String?) {
                        swipe_refresh.isRefreshing = false
                        if(error == null) {
                            Toaster(this@MainActivity).showToast(getString(R.string.update_todo_success_message))
                            //adapter.notifyItemChanged(position)
                        }else {
                            Toaster(this@MainActivity).showToast(error)
                        }
                    }
                })
            }
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        dialog.show()

        Validator.forceValidation(arrayOf(binding.tietTodoTitle, binding.tietTodoDate), dialog)
    }

    // mark a todoItem as complete/incomplete
    private fun toggleMarkAsComplete(todo: TodoEntity, position: Int) {
        if(currentUserEntity == null) { signIn(this); return }

        swipe_refresh.isRefreshing = true

        var successMessage = R.string.task_marked_as_completed_success_message
        if(todo.completed) successMessage = R.string.task_marked_as_incomplete_success_message
        todo.completed = !todo.completed
        remote.markTodoListAsComplete(arrayListOf(todo), object: TodoListCallback {
            override fun onResponse(todoList: ArrayList<TodoEntity>?, error: String?) {
                swipe_refresh.isRefreshing = false
                if(error == null) {
                    Toaster(this@MainActivity).showToast(getString(successMessage))
                }else {
                    Toaster(this@MainActivity).showToast(error)
                }
            }
        })
    }

    //delete a todoItem permanently
    private fun deleteTodo(todo: TodoEntity, position: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.text_are_you_sure)
            .setMessage(R.string.todo_delete_warning)
            .setPositiveButton(R.string.label_delete) {
                _,_->
                swipe_refresh.isRefreshing = true
                remote.deleteTodo(todo, object: TodoCallback {
                    override fun onResponse(todo: TodoEntity?, error: String?) {
                        swipe_refresh.isRefreshing = false
                        if(error == null) {
                            Toaster(this@MainActivity).showToast(getString(R.string.delete_todo_success_message))
                            //adapter.getTodoList().remove(todo)
                            //adapter.notifyDataSetChanged()
                        }else {
                            Toaster(this@MainActivity).showToast(error)
                        }
                    }
                })
            }
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()
            .show()
    }

    // bulk clear all completed todo items
    private fun clearCompletedTasks() {
        if(currentUserEntity == null) { signIn(this); return }

        if(adapter.getCompletedTodoList().size == 0) {
            Toaster(this).showToast(getString(R.string.no_completed_task_found_exception))
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.text_are_you_sure)
            .setMessage(R.string.clear_completed_tasks_warning)
            .setPositiveButton(R.string.label_clear_completed) { _,_->
                swipe_refresh.isRefreshing = true
                remote.deleteTodoList(adapter.getCompletedTodoList(), object: TodoListCallback {
                    override fun onResponse(todoList: ArrayList<TodoEntity>?, error: String?) {
                        swipe_refresh.isRefreshing = false
                        if (error != null) {
                            Toaster(this@MainActivity).showToast(error)
                        }else {
                            Toaster(this@MainActivity).showToast(
                                getString(R.string.clear_completed_tasks_success_message)
                            )
                        }
                    }
                })
            }
            .setNegativeButton(R.string.label_cancel) { _,_-> }
            .create()
            .show()
    }

    // get todoItems of current user with firestore listener
    private fun addTodoListListener() {
        if(currentUserEntity == null) { signIn(this); return }

        remote.addTodoListListener(currentUserEntity!!, object: TodoListCallback {
            override fun onResponse(todoList: ArrayList<TodoEntity>?, error: String?) {
                if (error != null) {
                    Toaster(this@MainActivity).showToast(error)
                }else {
                    if(todoList!!.size > 0) {
                        img_no_data.visibility = View.INVISIBLE
                        rv_todo_list.visibility = View.VISIBLE
                        adapter.setTodoList(todoList)
                        updateStatus()
                    }else {
                        img_no_data.visibility = View.VISIBLE
                        rv_todo_list.visibility = View.INVISIBLE
                    }
                }
            }
        })
    }

    // logout from current account and login to another/new account
    private fun switchAccount() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    currentUserEntity = null
                    adapter.clear()
                    rv_todo_list.visibility = View.INVISIBLE
                    img_no_data.visibility = View.VISIBLE
                    updateStatus()
                    remote.removeTodoListListener()
                    signIn(this)
                }else {
                    Toaster(this).showToast(getString(R.string.unknown_exception))
                }
            }
    }

    // update the profile info and the empty data view
    private fun updateStatus() {
        clearCompletedMenu?.isVisible = currentUserEntity != null

        if(currentUserEntity == null) {
            container_profile.visibility = View.GONE
        }else {
            container_profile.img_profile.load(currentUserEntity!!.image)
            container_profile.tv_name.text = currentUserEntity!!.name
            container_profile.visibility = View.VISIBLE
        }

        var status = getString(R.string.label_no_todo_list_found)
        if(adapter.itemCount > 0) {
            status = "${adapter.itemCount} to-do(s) found"
        }

        container_profile.tv_status.text = status

        val calender = Calendar.getInstance()
        val day = calender.get(Calendar.DAY_OF_MONTH)

        container_profile.tv_dd.text = day.toString()
        container_profile.tv_MMM.text = FormatUtil().toMonth(calender.time)
        container_profile.tv_day.text = FormatUtil().toDay(calender.time)
    }

    // remove listener of the todoList on activity destroy
    override fun onDestroy() {
        remote.removeTodoListListener()
        super.onDestroy()
    }
}
