package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.parser.Parser
import com.doordash.interview.phone_number_parser.parser.PhoneNumber
import com.doordash.interview.phone_number_parser.parser.PhoneType
import com.doordash.interview.phone_number_parser.proxy.CacheProxy
import com.doordash.interview.phone_number_parser.proxy.PhoneNumberRecord
import com.fasterxml.jackson.core.JsonParseException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import org.slf4j.LoggerFactory
import java.time.Instant

@Controller("/phone-numbers")
class PhoneNumberController internal constructor(
    private val proxy: CacheProxy
) {

    private val logger = LoggerFactory.getLogger(this::class.java)


     // TODO implement the case you get the same number mulitple tiems. You should not write to ache multiple times..

    @Post(value =  "/", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.APPLICATION_JSON])
    fun postPhoneNumber(@Body inputJson: InputJson): HttpResponse<*> {
        val starTime = Instant.now()
        logger.info("received the following input=${inputJson}")
        val parser = Parser(inputJson.raw_input_json).clean().map { proxy.save(it.key,it.value) }
        val endTime = Instant.now()
        if(endTime.minusMillis(starTime.toEpochMilli()).toEpochMilli() > 100){
            logger.info("TOOK TO LONG")
        }
//        https://docs.micronaut.io/2.2.1/guide/#lowLevelHttpClient 7.1.7
        if(false){
            // return HttpResponse.status<Any>(HttpStatus.UNAUTHORIZED).body(m)
        }
        return HttpResponse.created(parser)
    }

    @Error
    internal fun jsonError(request: HttpRequest<*>, e: JsonParseException): HttpResponse<JsonError> { //
        val error = JsonError("Invalid JSON: ${e.message}") //
            .link(Link.SELF, Link.of(request.uri))

        return HttpResponse.status<JsonError>(HttpStatus.BAD_REQUEST, "Fix Your JSON")
            .body(error) //
    }
}


data class InputJson(val raw_input_json: String)

data class Output(val id: String, val phoneNumber: PhoneNumber, val type: PhoneType, val occurrences: Int)


// todo: Healthendpoint https://docs.micronaut.io/2.2.1/guide/#healthEndpoint