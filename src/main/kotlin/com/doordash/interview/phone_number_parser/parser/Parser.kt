package com.doordash.interview.phone_number_parser.parser

const val AREA_CODE_LENGTH = 3
const val TELEPHONE_PREFIX_LENGTH = 3
const val LINE_NUMBER_LENGTH = 4
const val START_OF_VALID_INPUT = "("
const val VALID_PHONE_TYPE_SIZE = 6

class Parser(
    private val data: String
) {
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

    fun clean(): List<PhoneNumber> {
        val results  = mutableListOf<PhoneNumber>()
        while(currIndex < data.length){
            try{
                findStart()
                val currType = extractPhoneType()
                val areaCode = extractPhoneNumberSegment(AREA_CODE_LENGTH)
                val phoneNumberPrefix = extractPhoneNumberSegment(TELEPHONE_PREFIX_LENGTH)
                val phoneNumberLineNumber = extractPhoneNumberSegment(LINE_NUMBER_LENGTH)
                results.add(PhoneNumber(areaCode + phoneNumberPrefix + phoneNumberLineNumber, currType))
            }catch (e: Exception){
                currIndex += 1
                // we can log something meaningful here :)
            }
        }
        return results
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

data class PhoneNumber(val number: String, val phoneType: PhoneType)
