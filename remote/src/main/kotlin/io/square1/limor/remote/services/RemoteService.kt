package io.square1.limor.remote.services

import io.square1.limor.remote.executors.JobExecutor
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val DATE_FORMAT_MASK = "yyyy-MM-dd'T'HH:mm:ss.SSS"

data class RemoteServiceConfig(
    val baseUrl: String,
    val debug: Boolean,
    val client_id: String,
    val client_secret: String,
    var token: String,
    var expiredIn: Long
)

const val REQUEST_TYPE_ID = "RequestTypeId"

abstract class RemoteService<T> constructor(c: Class<T>, private val config: RemoteServiceConfig) {

    protected var service: T

    init {
        service = initApiService().create(c)

    }

    private fun initApiService(): Retrofit {
        registerResponseDateFormatSerializer()

        //println("El token que se envía es Bearer " + config.token)

        val builder = OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(getLoggingInterceptor())
            .addInterceptor(getRequestInterceptor())

        return Retrofit.Builder().baseUrl(config.baseUrl)
            .callbackExecutor(JobExecutor())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(builder.build()).build()
    }

    private fun registerResponseDateFormatSerializer() {
        registerSerializer(
            "java.util.Date",
            @Serializer(forClass = Date::class) object : KSerializer<Date> {
                private val df: SimpleDateFormat = SimpleDateFormat(DATE_FORMAT_MASK)

                override val descriptor: SerialDescriptor =
                    StringDescriptor.withName("WithCustomDefault")

                override fun serialize(encoder: Encoder, obj: Date) {
                    encoder.encodeString(df.format(obj))
                }

                override fun deserialize(decoder: Decoder): Date {
                    return df.parse(decoder.decodeString())
                }
            })
    }


    private fun getRequestInterceptor(): Interceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .url(addBaseParameters(chain.request().url()))
                .header("Authorization", "Bearer " + config.token)
                .header("Accept-Language", "en")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                //.header("X-API-VERSION", "2.1") //TODO JJ modify this to send real data from device
                .header("Platform", "android 6.0.1") //TODO JJ modify this to send real data from device
                .header("OS", "android") //TODO JJ modify this to send real data from device
                .header("AppVersion", "2.2.7") //TODO JJ modify this to send real data from device
                .build()
        )
    }


    private fun addBaseParameters(url: HttpUrl): HttpUrl {
        val builder = url.newBuilder()
        //builder.addQueryParameter("ApiKey", config.apiKey)
        //builder.addQueryParameter("CorrelationId", UUID.randomUUID().toString())
        //config.sessionId?.let { builder.addQueryParameter("SessionId", it) }
        return builder.build()
    }


    private fun getLoggingInterceptor(): Interceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        //logging.level = if (config.debug)
        //    HttpLoggingInterceptor.Level.BODY
        //else
        //    HttpLoggingInterceptor.Level.NONE
        HttpLoggingInterceptor.Level.BODY
        return logging
    }

    fun Date.toString(mask: String): String? {
        try {
            val simpleDateFormat = SimpleDateFormat(mask, Locale.getDefault())
            return simpleDateFormat.format(this)
        } catch (ignored: Exception) {
            return null
        }
    }

    fun String.toDate(mask: String): Date? {
        return try {
            val simpleDateFormat = SimpleDateFormat(mask, Locale.getDefault())
            simpleDateFormat.parse(this)
        } catch (ignored: Exception) {
            null
        }
    }


}