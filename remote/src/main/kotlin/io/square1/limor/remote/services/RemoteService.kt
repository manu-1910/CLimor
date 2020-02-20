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


const val DATE_FORMAT_MASK = "yyyy-MM-dd'T'HH:mm:ss.SSS"

data class RemoteServiceConfig(
    val apiKey: String,
    var sessionId: String? = null,
    val baseUrl: String,
    val debug: Boolean,
    val appVersion: String
)

const val REQUEST_TYPE_ID = "RequestTypeId"

abstract class RemoteService<T>
constructor(c: Class<T>, private val config: RemoteServiceConfig) {
    protected var service: T

    init {
        service = initApiService().create(c)
    }

    private fun initApiService(): Retrofit {
        registerResponseDateFormatSerializer()

        val builder = OkHttpClient.Builder()
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
                .header("x-MyHome-App-Version", config.appVersion)
                .build()
        )
    }

    private fun addBaseParameters(url: HttpUrl): HttpUrl {
        val builder = url.newBuilder()
        builder.addQueryParameter("ApiKey", config.apiKey)
        builder.addQueryParameter("CorrelationId", UUID.randomUUID().toString())
        //config.sessionId?.let { builder.addQueryParameter("SessionId", it) }
        return builder.build()
    }


    private fun getLoggingInterceptor(): Interceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (config.debug)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
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