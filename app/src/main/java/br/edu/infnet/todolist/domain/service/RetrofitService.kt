package br.edu.infnet.todolist.domain.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitService {

    companion object {
        private const val url = "https://servicodados.ibge.gov.br/api/v1/"

        fun getInstance() : Retrofit {
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build()
        }
    }
}