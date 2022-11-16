package com.doordash.interview.phone_number_parser.controllers

import com.doordash.interview.phone_number_parser.parser.Parser
import com.doordash.interview.phone_number_parser.proxy.StoragePersistException
import com.doordash.interview.phone_number_parser.proxy.StorageService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Body
import org.slf4j.LoggerFactory
import java.time.Instant

// TODO ASK ABOUT THE 100MS should we just end the reaquest
//  or is somethign else like maybe a error log beteter
const val MAX_EXECUTION_TIME = 100
const val EMPTY_JSON_INPUT = "{}"

@Controller("/phone-numbers")
class PhoneNumberController(
    private val proxy: StorageService,
    private val mapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(this::class.java)


    @Post(
        value = "/",
        consumes = [MediaType.APPLICATION_JSON],
        produces = [MediaType.APPLICATION_JSON]
    )
    fun postPhoneNumber(@Body input: String): HttpResponse<*> {
        val starTime = Instant.now()

        logger.info("Received the following input: $input")

        if (input == EMPTY_JSON_INPUT) {
            logger.error("Empty json string provided by merchant")
            return HttpResponse.badRequest(
                "Empty json provided! " +
                        "Please provided a json string of the following format"
            )
        }

        val inputJson: InputJson = try {
            mapper.readValue(input)
        } catch (e: Exception) {
            logger.error(
                "Input=$input threw the following error: " +
                        "$e"
            )
            return HttpResponse.badRequest("Provided input was not of expected input")
        }

        val res = Parser
            .clean(
                inputJson.raw_phone_numbers
            )
            .map {
                try {
                    proxy.save(it.key, it.value)
                } catch (e: StoragePersistException) {
                    logger.error(e.message)
                    return HttpResponse.serverError("Database down, please try again later")
                }
            }

        if (res.isEmpty()) return HttpResponse.badRequest(
            "raw_phone_numbers contained zero valid inputs"
        )

        val executionTime = Instant.now().minusMillis(starTime.toEpochMilli()).toEpochMilli()

        // TODO DO WE FAIL ON EXECUTION?
        if (executionTime > MAX_EXECUTION_TIME) {
            logger.error(
                "The following input took $executionTime milliseconds to complete.\n" +
                        "input=$inputJson"
            )
        }

        return HttpResponse.created(res)
    }
}

data class InputJson(val raw_phone_numbers: String)

// todo: Healthendpoint https://docs.micronaut.io/2.2.1/guide/#healthEndpoint