package br.edu.infnet.todolist.domain.models.pais

import com.google.gson.annotations.SerializedName

data class Paises(
    @SerializedName("nome")
    var nome: Nome
)