package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import com.doordash.interview.phone_number_parser.proxy.CacheProxy
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory

@Controller("/phone-numbers")
class PhoneNumberController(
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val proxy: CacheProxy = CacheProxy()


    @Post("/")
    fun postPhoneNumber(@Body inputJson: InputJson): HttpResponse<List<Output>> {
        logger.info("received the following input=${inputJson}")
        proxy.save()
        return HttpResponse.created(emptyList())
    }
}

data class InputJson(val rawInputJson: String)

data class Output(val id: String, val phoneNumber: PhoneNumber, val type: PhoneType, val occurrences: Int)