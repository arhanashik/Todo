package com.blackspider.todo.app.data.local

object Const {
    //Request codes in one place
    interface RequestCode {
        companion object {
            const val AUTH = 11
        }
    }

    //Collections used in firestore
    interface Collection {
        companion object {
            const val USER = "user"
            const val TODO = "todo"
        }
    }

    //each collection's fields
    interface Key {
        interface User {
            companion object {
                const val ID = "id"
                const val NAME = "name"
                const val EMAIL = "email"
                const val IMAGE = "image"
            }
        }

        interface Todo {
            companion object {
                const val ID = "id"
                const val TODO = "todo"
                const val COMPLETED = "completed"
                const val DATE = "date"
                const val USER = "user"
                const val CREATED_AT = "created_at"
            }
        }
    }
}