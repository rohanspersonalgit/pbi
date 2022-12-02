package com.doordash.interview.phone_number_parser.parser

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class ParserTest {
    private val perfectCase1 = ParserInputToExpectedOutput(
        "(Cell) 604-805-7254",
        PhoneNumber(
            "6048057254",
            PhoneType.CELL
        )
    )

    private val perfectCase2 = ParserInputToExpectedOutput(
        "(Home) 415-415-4155",
        PhoneNumber(
            "4154154155",
            PhoneType.HOME
        )
    )

    private val perfectCase3 = ParserInputToExpectedOutput(
        "(Cell) 1231231234",
        PhoneNumber(
            "1231231234",
            PhoneType.CELL
        )
    )

    @Test
    fun `handle a perfectly formatted input string with a single output`() {
        val res = Parser.clean(perfectCase1.input)

        checkResults(
            res,
            mapOf(
                perfectCase1.expectedOutput to 1
            )
        )
    }

    @Test
    fun `handle a perfectly formatted input string with multiple outputs`() {
        val testCases = listOf(perfectCase1, perfectCase2, perfectCase3)
        val res = Parser.clean(testCases.joinToString("") { it.input })
        checkResults(
            res, mapOf(
                perfectCase1.expectedOutput to 1,
                perfectCase2.expectedOutput to 1,
                perfectCase3.expectedOutput to 1
            )
        )
    }

    @Test
    fun `handle a case where there are dashes in the number`() {
        val testCase = ParserInputToExpectedOutput(
            "(Cell) 604-805-7254",
            PhoneNumber(
                "6048057254",
                PhoneType.CELL
            )
        )

        val res = Parser.clean(testCase.input)

        checkResults(res, mapOf(testCase.expectedOutput to 1))
    }

    @Test
    fun `handle input with some random letters in the number`() {
        val tesCase = ParserInputToExpectedOutput(
            "(Cell)6a04805b7254",
            PhoneNumber(
                "6048057254",
                PhoneType.CELL
            )
        )
        val res = Parser.clean(tesCase.input)
        checkResults(res, mapOf(tesCase.expectedOutput to 1))

    }

    @Test
    fun `return none when there is a invalid phone type`() {
        val inputToExpectedOutput = "(Home6048057254"
        val res = Parser.clean(inputToExpectedOutput)
        checkResults(res, mapOf())
    }

    @Test
    fun `return none when phone number is to short`() {
        val res = Parser.clean("(Home)604805625")
        checkResults(res, mapOf())
    }

    @Test
    fun `return none when phone type is too short`() {
        val res = Parser.clean("(Home")
        checkResults(res, mapOf())
    }

    @Test
    fun `return none when invalid phone type given`() {
        val res = Parser.clean("(cell)")
        checkResults(res, mapOf())
    }

    @Test
    fun `return none when phone number is to long`() {
        val res = Parser.clean("(Home)60480562511")
        checkResults(res, mapOf())
    }

    @Test
    fun `return 1 valid 1 invalid value`() {
        val res = Parser.clean("(Home)604805762" + perfectCase1.input)
        checkResults(res, mapOf(perfectCase1.expectedOutput to 1))
    }

    @Test
    fun `return a map of size 1 with multiple occurrences of the same number`() {
        val expectedTimes = 3
        val res = Parser.clean(perfectCase1.input.repeat(expectedTimes))
        checkResults(res, mapOf(perfectCase1.expectedOutput to expectedTimes))
    }

    @Test
    fun `handle multiple valid and invalid inputs`() {
        val expectedTimes = 2
        val inputString = perfectCase1.input.repeat(expectedTimes) +
                "(home6048057254(Home)604805725".repeat(expectedTimes) +
                perfectCase2.input.repeat(expectedTimes)
        val res = Parser.clean(inputString)
        checkResults(
            res,
            mapOf(
                perfectCase1.expectedOutput to expectedTimes,
                perfectCase2.expectedOutput to expectedTimes
            )
        )
    }

    @Test
    fun `Handle case sensitivity for cell type`() {
        val res = Parser.clean(perfectCase1.input.toUpperCase())
        checkResults(res, mapOf())
    }

    @Test
    fun `generatePhoneNumberKey as expected`() {
        expectThat(
            perfectCase1.expectedOutput.key()
        ).isEqualTo("6048057254cell")
    }

    @Test
    fun `throw exception when given an invalid phone type`(){
        expectThrows<InvalidPhoneTypeException> {
            PhoneType.fromDataString("test")
        }
    }
}

private fun checkResults(result: Map<PhoneNumber, Int>, expectedOutput: Map<PhoneNumber, Int>) {
    expectThat(result.size)
        .isEqualTo(expectedOutput.size)
    expectedOutput.entries.forEach {
        expectThat(result.containsKey(it.key))
            .isTrue()
        expectThat(result[it.key])
            .isEqualTo(it.value)
    }

}

internal data class ParserInputToExpectedOutput(
    val input: String,
    val expectedOutput: PhoneNumber
)
