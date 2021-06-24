package br.edu.infnet.todolist.domain.service

import br.edu.infnet.todolist.domain.exception.PaisError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory

class RetrofitService {

    companion object {
        private const val url = "https://restcountries-v1.p.rapidapi.com/"

        suspend fun getInstance(): Retrofit {
            val builder = OkHttpClient.Builder()
            builder.connectionSpecs(
                listOf(
                    ConnectionSpec.CLEARTEXT,
                    ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .allEnabledTlsVersions()
                        .allEnabledCipherSuites()
                        .build()
                )
            ).addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .addHeader("x-rapidapi-key",
                                    "9850361e18msh7304f065c92219bp1992e3jsne081b44f24f4")
                        .addHeader("x-rapidapi-host",
                                    "restcountries-v1.p.rapidapi.com")
                        .method(original.method(), original.body())
                        .build()

                    chain.proceed(request)
                }

            val client = builder.build()

            try {
                return Retrofit.Builder().client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build()
            } catch (cause: Throwable) {
                throw PaisError("Erro ao carregar os dados", cause)
            }
        }
    }
}