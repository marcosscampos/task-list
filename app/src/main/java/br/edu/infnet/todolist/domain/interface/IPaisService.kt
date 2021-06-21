package br.edu.infnet.todolist.domain.`interface`

import br.edu.infnet.todolist.domain.models.pais.Paises
import retrofit2.Call
import retrofit2.http.GET

interface IPaisService {

    @GET("paises")
    suspend fun getAllCountries() : ArrayList<Paises>
}