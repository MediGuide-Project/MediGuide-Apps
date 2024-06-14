package capstone.app.mediguide.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MyNetworkClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(180, TimeUnit.SECONDS) // Timeout untuk koneksi
        .readTimeout(180, TimeUnit.SECONDS)    // Timeout untuk membaca respon
        .writeTimeout(180, TimeUnit.SECONDS)   // Timeout untuk menulis permintaan
        .callTimeout(180, TimeUnit.SECONDS)    // Timeout total untuk request
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://34.101.106.128:8080/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
