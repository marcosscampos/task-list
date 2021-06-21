package br.edu.infnet.todolist.domain.service

import br.edu.infnet.todolist.domain.exception.PaisError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitService {
    companion object {
        private const val url = "https://servicodados.ibge.gov.br/api/v1/"

        suspend fun getInstance(): Retrofit {
            return withContext(Dispatchers.IO) {
                try {
                    Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(url)
                        .build()
                } catch (cause: Throwable) {
                    throw PaisError("Erro ao carregar os dados", cause)
                }
            } as Retrofit
        }
    }
}