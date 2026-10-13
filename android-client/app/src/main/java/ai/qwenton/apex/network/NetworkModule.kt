package ai.qwenton.apex.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private var currentBaseUrl = "http://127.0.0.1:8765/"
    private var cachedApi: QwentonApi? = null
    private var cachedClient: OkHttpClient? = null

    fun getOkHttpClient(): OkHttpClient {
        if (cachedClient == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            cachedClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()
        }
        return cachedClient!!
    }

    fun isValidUrl(url: String): Boolean {
        return try {
            val parsed = okhttp3.HttpUrl.parse(url)
            parsed != null && (parsed.scheme() == "http" || parsed.scheme() == "https")
        } catch (e: Exception) {
            false
        }
    }

    fun getApi(baseUrl: String = currentBaseUrl): QwentonApi {
        val candidate = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val safeUrl = if (isValidUrl(candidate)) candidate else "http://127.0.0.1:8765/"

        if (cachedApi == null || safeUrl != currentBaseUrl) {
            currentBaseUrl = safeUrl
            val retrofit = Retrofit.Builder()
                .baseUrl(safeUrl)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            cachedApi = retrofit.create(QwentonApi::class.java)
        }
        return cachedApi!!
    }
}
