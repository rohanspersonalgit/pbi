package com.doordash.interview.phone_number_parser.parser

import org.slf4j.LoggerFactory

const val AREA_CODE_LENGTH = 3
const val TELEPHONE_PREFIX_LENGTH = 3
const val LINE_NUMBER_LENGTH = 4
const val START_OF_VALID_INPUT = "("
const val VALID_PHONE_TYPE_SIZE = 6

class Parser(
    private val data: String
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var currIndex = 0

    private fun extractPhoneNumberSegment(expectedDigits: Int): String{
        var digits = ""
        while(currIndex < data.length && digits.length < expectedDigits){
            if (data[currIndex].isDigit()){
                digits += data[currIndex]
            }else if(data[currIndex].toString() == START_OF_VALID_INPUT) {
                // move back 1 so the next iteration can find the start
                currIndex -= 1
                break
            }
            currIndex += 1
        }
        if(digits.length == expectedDigits) return digits
        throw Exception("")
    }

    private fun extractPhoneType(): PhoneType{
        val checkType = if(currIndex + VALID_PHONE_TYPE_SIZE < data.length) {
            generatePhoneType(
                data.subSequence(currIndex, currIndex + VALID_PHONE_TYPE_SIZE).toString()
            )
        } else {
            throw Exception("Phone number being evaluated starting at position $currIndex" +
                    "does not have a valid phone type")
        }
        currIndex += VALID_PHONE_TYPE_SIZE
        return checkType
    }

    // place the index at a '(' signifying the start of a possibly valid input
    private fun findStart() {
        while (currIndex < data.length
            && data[currIndex].toString() != START_OF_VALID_INPUT){
            currIndex++
        }
    }

    fun clean(): Map<PhoneNumber,Int> {
        val resMap = mutableMapOf<PhoneNumber, Int>().withDefault { 0 }
        while(currIndex < data.length){
            try{
                findStart()
                val currType = extractPhoneType()
                val areaCode = extractPhoneNumberSegment(AREA_CODE_LENGTH)
                val phoneNumberPrefix = extractPhoneNumberSegment(TELEPHONE_PREFIX_LENGTH)
                val phoneNumberLineNumber = extractPhoneNumberSegment(LINE_NUMBER_LENGTH)
                // we are gonna say that if the next character is a number then invalid
                // TODO But what about 6048057254-43? Do we include?
                // 6048057254a? Lets say thats okay? what if its 6048057254ac6048057254?
                // 6048057254 435
                // 6048057254604 a NO from prior questions
                // generalize to 6048057254EOF or 6048067254 (any_char) or 6048057254
                val phoneNumber = PhoneNumber(areaCode + phoneNumberPrefix + phoneNumberLineNumber, currType)
                resMap[phoneNumber] = resMap.getValue(phoneNumber) + 1
            }catch (e: Exception){
                currIndex += 1
                // we can log something meaningful here :)
            }
        }
        return resMap
    }

    private fun generatePhoneType(phoneTypeData: String): PhoneType {
        return when (phoneTypeData) {
            "(Home)" -> PhoneType.HOME
            "(Cell)" -> PhoneType.CELL
            else -> throw Exception("")
        }
    }
}

enum class PhoneType(val value: String) {
    HOME("(Home)"),
    CELL("(Cell)");

}

data class PhoneNumber(val number: String, val phoneType: PhoneType){
    fun generateKey():String{
        return number + phoneType.value
    }
}
