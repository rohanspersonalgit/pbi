package com.doordash.interview.phone_number_parser.parser

import org.slf4j.LoggerFactory

const val AREA_CODE_LENGTH = 3
const val TELEPHONE_PREFIX_LENGTH = 3
const val LINE_NUMBER_LENGTH = 4
const val START_OF_VALID_TYPE_INPUT = "("
const val END_OF_VALID_TYPE_INPUT = ")"
const val VALID_PHONE_TYPE_SIZE = 4

abstract class PhoneParserException(cause: String) : Exception(cause)
class NumberToLongException(message: String) : PhoneParserException(message)
class NumberToShortException(message: String) : PhoneParserException(message)
class InvalidPhoneTypeException(message: String) : PhoneParserException("The following phone type given is invalid: $message")


object Parser {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun extractPhoneNumberSegment(expectedDigits: Int, iterator: ListIterator<Char>): String {
        try {
            val res = (1..expectedDigits).map {
                var check = iterator.next()
                while (!check.isDigit()) {
                    if (check.toString() == START_OF_VALID_TYPE_INPUT) {
                        // make the start of the next valid string iterable
                        iterator.previous()
                        throw NumberToShortException(
                            "Reached Start of new input string, " +
                                    "before extracting current number"
                        )
                    }
                    check = iterator.next()
                }
                check
            }
            return res.joinToString("")
        } catch (e: NoSuchElementException) {
            throw NumberToShortException(
                "Input does not contain enough characters " +
                        "to extract whole phone number"
            )
        }
    }

    private fun extractPhoneType(iterator: ListIterator<Char>): PhoneType {
        try {
            val res = (1..VALID_PHONE_TYPE_SIZE).map { iterator.next() }

            if (iterator.next().toString() != END_OF_VALID_TYPE_INPUT) {
                throw InvalidPhoneTypeException("Missing closing bracket for phone type")
            }

            return PhoneType.fromDataString(res.joinToString(""))
        } catch (e: NoSuchElementException) {
            throw InvalidPhoneTypeException("Not enough characters for the phone type")
        }
    }

    // place the index at a '(' signifying the start of a possibly valid input
    private fun findStart(iterator: Iterator<Char>) {
        while (iterator.hasNext()) {
            if (iterator.next().toString() == START_OF_VALID_TYPE_INPUT) return
        }
        throw InvalidPhoneTypeException("Missing starting bracket for phone type")
    }

    private fun isValidChar(char: Char): Boolean {
        return char.isLetterOrDigit() ||
                START_OF_VALID_TYPE_INPUT == char.toString() ||
                END_OF_VALID_TYPE_INPUT == char.toString()
    }

    fun clean(data: String): Map<PhoneNumber, Int> {
        val strippedData = data.filter {
            !it.isWhitespace() && isValidChar(it)
        }

        val iterator = strippedData.toList().listIterator()

        val resMap = mutableMapOf<PhoneNumber, Int>().withDefault { 0 }

        while (iterator.hasNext()) {
            try {
                findStart(iterator)

                val currType = extractPhoneType(iterator)
                val areaCode = extractPhoneNumberSegment(AREA_CODE_LENGTH, iterator)
                val phoneNumberPrefix = extractPhoneNumberSegment(TELEPHONE_PREFIX_LENGTH, iterator)
                val phoneNumberLineNumber = extractPhoneNumberSegment(LINE_NUMBER_LENGTH, iterator)

                val ind = iterator.nextIndex()

                // checkIfNumberIsToLong
                if (ind < strippedData.length && strippedData[ind].isDigit()) {
                    throw NumberToLongException("The given number is too long")
                } else {
                    // move the index back one now that we confirmed it is valid
                    iterator.previousIndex()
                }

                val phoneNumber = PhoneNumber(
                    areaCode + phoneNumberPrefix + phoneNumberLineNumber,
                    currType
                )

                resMap[phoneNumber] = resMap.getValue(phoneNumber) + 1
            } catch (e: PhoneParserException) {
                logger.info("Error thrown due to ${e.message}")
                continue
            }
        }
        return resMap
    }
}

enum class PhoneType(val value: String) {
    HOME("home"),
    CELL("cell");

    companion object {
        fun fromDataString(value: String): PhoneType {
            return when (value) {
                "Cell" -> CELL
                "Home" -> HOME
                else -> throw InvalidPhoneTypeException("Received phoneType = $value")
            }
        }
    }
}

data class PhoneNumber(val number: String, val phoneType: PhoneType) {
    fun key(): String {
        return number + phoneType.value
    }
}
