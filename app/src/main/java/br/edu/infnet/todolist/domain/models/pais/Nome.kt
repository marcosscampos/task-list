package br.edu.infnet.todolist.domain.models.pais

import com.google.gson.annotations.SerializedName

data class Nome(
    @SerializedName("abreviado")
    var abreviado: String
)