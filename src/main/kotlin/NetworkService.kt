import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkService {
    const val BASE_URL = "https://admin.openbudget.uz/api/v1/"

    var client: OkHttpClient

    init {
        //set self sign certificate
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers() = arrayOf<X509Certificate>()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        // Create an ssl socket factory with our all-trusting manager
        val sslSocketFactory = sslContext.socketFactory

        // connect to server
        client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager).hostnameVerifier { _, _ -> true }
            .connectTimeout(Duration.ofSeconds(15))
            .writeTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(15))
            .addInterceptor {
                it.proceed(
                    it.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader(
                            "User-Agent",
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36"
                        )
                        .addHeader("Referer", "https://openbudget.uz/")
                        .build()
                )
            }
            .build()

    }

    val retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(OpenApi::class.java)
}