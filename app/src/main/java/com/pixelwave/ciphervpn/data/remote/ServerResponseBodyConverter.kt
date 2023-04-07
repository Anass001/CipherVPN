package com.pixelwave.ciphervpn.data.remote

import com.pixelwave.ciphervpn.data.model.Server
import com.pixelwave.ciphervpn.util.CsvParser
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class ServerResponseBodyConverter : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, List<Server>> {
        return Converter { value ->
            CsvParser.parse(value.byteStream())
        }
    }
}