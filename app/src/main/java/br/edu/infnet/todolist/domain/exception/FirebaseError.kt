package br.edu.infnet.todolist.domain.exception

class FirebaseError(message: String, cause: Throwable) : Throwable(message, cause)