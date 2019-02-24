package com.blackspider.todo.util.lib.firebase

import com.blackspider.todo.app.data.local.Const
import com.blackspider.todo.app.data.local.todo.TodoEntity
import com.blackspider.todo.app.data.local.user.UserEntity
import com.blackspider.todo.util.helper.FormatUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

/*
* This class performs the conversions between firestore object and kotlin data class
*/
interface FireStoreMapper {

    // map firebase user data to userObject
    fun toUserObject(user: FirebaseUser): HashMap<String, Any> {
        val userObject = HashMap<String, Any>()
        userObject[Const.Key.User.ID] = user.uid
        userObject[Const.Key.User.NAME] = user.displayName!!
        userObject[Const.Key.User.EMAIL] = user.email!!
        val image = if(user.photoUrl == null) "" else user.photoUrl.toString()
        userObject[Const.Key.User.IMAGE] = image

        return userObject
    }

    // map firebase user to user entity
    fun toUserEntity(user: FirebaseUser): UserEntity {
        val image = if(user.photoUrl == null) "" else user.photoUrl.toString()
        return UserEntity(
            user.uid,
            user.displayName,
            user.email,
            image
        )
    }

    // map firestore object to user entity
    fun toUserEntity(document: Map<String, Any>): UserEntity {
        return UserEntity(
            document[Const.Key.User.ID] as String,
            document[Const.Key.User.NAME] as String,
            document[Const.Key.User.EMAIL] as String,
            document[Const.Key.User.IMAGE] as String
        )
    }

    // map todoEntity to firestore todoObject
    fun toTodoObject(todo: TodoEntity): HashMap<String, Any> {
        val todoObject = HashMap<String, Any>()
        todoObject[Const.Key.Todo.TODO] = todo.todo
        todoObject[Const.Key.Todo.COMPLETED] = todo.completed
        todoObject[Const.Key.Todo.DATE] = todo.date
        todoObject[Const.Key.Todo.USER] = todo.user
        todoObject[Const.Key.Todo.CREATED_AT] = FieldValue.serverTimestamp()

        return todoObject
    }

    // map firestore todoObject to todoEntity
    private fun toTodoEntity(document: QueryDocumentSnapshot): TodoEntity {
        val todoId = document.id
        val data = document.data

        val todo = data[Const.Key.Todo.TODO] as String
        val completed = data[Const.Key.Todo.COMPLETED] as Boolean
        val date = data[Const.Key.Todo.DATE] as String
        val user = data[Const.Key.Todo.USER] as String
        val timestamp = data[Const.Key.Todo.CREATED_AT]
        var timestampToDate = Date()
        if(timestamp != null) timestampToDate = (timestamp as Timestamp).toDate()
        val createdAt = FormatUtil().formatDate(timestampToDate, FormatUtil.dd_MMM_yyyy)

        return TodoEntity(
            todoId, todo, completed, date, user, createdAt
        )
    }

    // map firestore todoObject list to todoEntity list
    fun toTodoEntityList(data: QuerySnapshot): ArrayList<TodoEntity> {
        val todoEntityList = ArrayList<TodoEntity>()
        for (document in data)
            todoEntityList.add(toTodoEntity(document))

        return todoEntityList
    }
}