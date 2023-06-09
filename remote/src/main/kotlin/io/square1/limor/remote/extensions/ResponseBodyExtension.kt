package io.square1.limor.remote.extensions


import com.google.gson.Gson
import io.square1.limor.remote.entities.responses.NWErrorResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody


fun<T> ResponseBody.parseSuccessResponse(serializer: DeserializationStrategy<T>): T {
    val  jsonString = string()
    try {
        return Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.decodeFromString(serializer, jsonString)
    } catch (e: Exception){
        throw parseErrorResponse(jsonString)
    }
}

fun<T> ResponseBody.parseSuccessResponseGson(serializer: Class<T>): T {
    val  jsonString = string()
    try {
        return Gson().fromJson(jsonString, serializer)
    } catch (e: Exception){
        throw parseErrorResponse(jsonString)
    }
}


@Throws
private fun parseErrorResponse(jsonString: String): NWErrorResponse {
    var customError = NWErrorResponse()
    try {
        customError = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.decodeFromString(NWErrorResponse.serializer(), jsonString)
    } catch (e: Exception) {
        customError.code = 999
        customError.messageStr = "Unexpected Error UFO"
        //customError.data = "ufo error ufo error"
    }
    return customError
}

