package com.blackspider.todo.util.lib.firebase

import com.blackspider.todo.app.data.local.Const
import com.blackspider.todo.app.data.local.todo.TodoEntity
import com.blackspider.todo.app.data.local.user.UserEntity
import com.blackspider.todo.util.lib.firebase.callback.TodoCallback
import com.blackspider.todo.util.lib.firebase.callback.CreateUserCallback
import com.blackspider.todo.util.lib.firebase.callback.TodoListCallback
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import timber.log.Timber
import java.util.ArrayList

class FireStoreService: FireStoreMapper {

    // firestore reference
    private val db = FirebaseFirestore.getInstance()

    //todoList documents listener
    private var todoListener: ListenerRegistration? = null

    // create user from firebase auth user
    fun createUser(user: FirebaseUser, callback: CreateUserCallback) {
        val userDb = db.collection(Const.Collection.USER)

        userDb
            .whereEqualTo(Const.Key.User.ID, user.uid)
            .get()
            .addOnSuccessListener { result ->
                if(result == null || result.size() == 0) {
                    val userObject = toUserObject(user)
                    val userEntity = toUserEntity(user)

                    db.collection(Const.Collection.USER)
                        .add(userObject)
                        .addOnSuccessListener {
                            callback.onResponse(userEntity, null)
                        }
                        .addOnFailureListener {
                            callback.onResponse(userEntity, "Failed to create new user. Error: ${it.message}")
                        }
                }else {
                    for (document in result) {
                        val userEntity = toUserEntity(document.data)
                        callback.onResponse(userEntity, null)
                        break
                    }
                }
            }
            .addOnFailureListener {
                callback.onResponse(null, "Failed to check user. Error: ${it.message}")
            }
    }

    // map firebase user to user entity
    fun getUserEntity(user: FirebaseUser): UserEntity {
        return toUserEntity(user)
    }

    // add new todoItem to firestore
    fun addTodo(todo: TodoEntity, callback: TodoCallback) {
        val todoObject = toTodoObject(todo)

        db.collection(Const.Collection.TODO)
            .add(todoObject)
            .addOnSuccessListener {
                todo.id = it.id
                callback.onResponse(todo, null)
            }
            .addOnFailureListener {
                Timber.e(it)
                callback.onResponse(todo, "Failed. Error: ${it.message}")
            }
    }

    // update an existing todoITem
    fun updateTodo(todo: TodoEntity, callback: TodoCallback) {
        val todoDoc = db.collection(Const.Collection.TODO).document(todo.id)

        val tasks = ArrayList<Task<Void>>()
        tasks.add(todoDoc.update(Const.Key.Todo.TODO, todo.todo))
        tasks.add(todoDoc.update(Const.Key.Todo.DATE, todo.date))

        Tasks.whenAllSuccess<Void>(tasks).addOnSuccessListener {
            callback.onResponse(todo, null)
        }.addOnFailureListener {
            Timber.e(it)
            callback.onResponse(null, "Opps! ${it.message}")
        }
    }

    // update the complete status of bulk todoItems
    fun markTodoListAsComplete(todoList: ArrayList<TodoEntity>, callback: TodoListCallback) {
        val todoDb = db.collection(Const.Collection.TODO)

        val tasks = ArrayList<Task<Void>>()
        todoList.forEach {
            tasks.add(todoDb.document(it.id).update(Const.Key.Todo.COMPLETED, it.completed))
        }

        Tasks.whenAllSuccess<Void>(tasks).addOnSuccessListener {
            callback.onResponse(todoList, null)
        }.addOnFailureListener {
            Timber.e(it)
            callback.onResponse(null, "Opps! ${it.message}")
        }
    }

    // delete todoItem
    fun deleteTodo(todo: TodoEntity, callback: TodoCallback) {
        val todoDb = db.collection(Const.Collection.TODO).document(todo.id)

        todoDb
            .delete()
            .addOnSuccessListener {
                callback.onResponse(todo, null)
            }
            .addOnFailureListener {
                Timber.e(it)
                callback.onResponse(todo, "Opps! ${it.message}")
            }
    }

    // delete bulk todoItems
    fun deleteTodoList(todoList: ArrayList<TodoEntity>, callback: TodoListCallback) {
        val todoDb = db.collection(Const.Collection.TODO)

        val tasks = ArrayList<Task<Void>>()
        todoList.forEach {
            tasks.add(todoDb.document(it.id).delete())
        }

        Tasks.whenAllSuccess<Void>(tasks).addOnSuccessListener {
            callback.onResponse(todoList, null)
        }.addOnFailureListener {
            Timber.e(it)
            callback.onResponse(null, "Opps! ${it.message}")
        }
    }

    // get todoList of a particuler user
    fun getTodoList(user: UserEntity, callback: TodoListCallback) {
        val todoDb = db.collection(Const.Collection.TODO)

        todoDb
            .whereEqualTo(Const.Key.Todo.USER, user.id)
            .get()
            .addOnSuccessListener {
                callback.onResponse(toTodoEntityList(it), null)
            }
            .addOnFailureListener {
                callback.onResponse(null, "Failed. Error: ${it.message}")
            }
    }

    // register a listener for getting the live changes of todoDocuments stored in firestore
    fun addTodoListListener(user: UserEntity, callback: TodoListCallback) {
        val query = db.collection(Const.Collection.TODO)
            .whereEqualTo(Const.Key.Todo.USER, user.id)

        todoListener = query.addSnapshotListener(EventListener<QuerySnapshot> { value, e ->
                if (e != null) {
                    Timber.e("Listen failed. %s", e.toString())
                    callback.onResponse(null, "Failed. Error: ${e.message}")
                    return@EventListener
                }

                callback.onResponse(toTodoEntityList(value!!), null)
            })
    }

    // unregister the listener
    fun removeTodoListListener() {
        todoListener?.remove()
    }
}