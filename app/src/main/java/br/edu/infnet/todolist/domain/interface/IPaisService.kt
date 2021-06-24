package br.edu.infnet.todolist.domain.`interface`

import br.edu.infnet.todolist.domain.models.pais.Paises
import retrofit2.http.*

interface IPaisService {

    @GET("all")
    suspend fun getAllCountries(): ArrayList<Paises>

    @GET("name/{name}")
    suspend fun getCountry(@Path("name") name: String) : Paises
}