package com.oksusu.susu.extension

import com.fasterxml.jackson.core.type.TypeReference
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

fun String.encodeURL(type: String = "UTF-8"): String = URLEncoder.encode(this, type)
fun String.decodeURL(type: String = "UTF-8"): String = URLDecoder.decode(this, type)

fun String.decodeBase64(): String = String(Base64.getDecoder().decode(this))

fun <T : Any> T.encodeBase64(): String {
    val jsonValue = mapper.writeValueAsString(this)
    return Base64.getEncoder().encodeToString(jsonValue.encodeToByteArray())
}

fun <T : Any> String.decodeBase64(type: TypeReference<T>): T {
    val jsonValue = this.decodeBase64()
    return mapper.readValue(jsonValue, type)
}
