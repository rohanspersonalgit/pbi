package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.parser.Parser
import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import com.doordash.interview.phone_number_parser.proxy.CacheProxy
import com.doordash.interview.phone_number_parser.proxy.CacheProxyImpl
import com.doordash.interview.phone_number_parser.proxy.PhoneNumberRecord
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import java.time.Instant

@Controller("/phone-numbers")
class PhoneNumberController internal constructor(
    internal val proxy: CacheProxy
) {

    private val logger = LoggerFactory.getLogger(this::class.java)



    @Post("/")
    fun postPhoneNumber(@Body inputJson: InputJson): HttpResponse<List<PhoneNumberRecord>> {
        val starTime = Instant.now()
        logger.info("received the following input=${inputJson}")
        val parser = Parser(inputJson.rawInputJson).clean().map { proxy.save(it) }
        val endTime = Instant.now()
        if(endTime.minusMillis(starTime.toEpochMilli()).toEpochMilli() > 100){
            logger.info("TOOK TO LONG")
        }
        return HttpResponse.created(parser)
    }
}

data class InputJson(val rawInputJson: String)

data class Output(val id: String, val phoneNumber: PhoneNumber, val type: PhoneType, val occurrences: Int)