package io.github.vooft.spektor.codegen.codegen

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ResponseNameGeneratorTest {
    @Test
    fun `should generate when operationId is camelCase`() {
        ResponseNameGenerator.generate("searchBooks") shouldBe "SearchBooksResponse"
    }

    @Test
    fun `should generate when operationId is single lowercase word`() {
        ResponseNameGenerator.generate("create") shouldBe "CreateResponse"
    }

    @Test
    fun `should generate when operationId is PascalCase`() {
        ResponseNameGenerator.generate("GetAuthor") shouldBe "GetAuthorResponse"
    }

    @Test
    fun `should generate when operationId is hyphen separated`() {
        ResponseNameGenerator.generate("get-author") shouldBe "GetAuthorResponse"
    }

    @Test
    fun `should generate when operationId is underscore separated`() {
        ResponseNameGenerator.generate("list_all_books") shouldBe "ListAllBooksResponse"
    }

    @Test
    fun `should generate when operationId has mixed separators`() {
        ResponseNameGenerator.generate("get_author-books") shouldBe "GetAuthorBooksResponse"
    }

    @Test
    fun `should generate when operationId has multiple consecutive separators`() {
        ResponseNameGenerator.generate("get--author") shouldBe "GetAuthorResponse"
    }

    @Test
    fun `should generate when operationId is space separated`() {
        ResponseNameGenerator.generate("get author books") shouldBe "GetAuthorBooksResponse"
    }

    @Test
    fun `should throw when operationId is empty`() {
        shouldThrow<IllegalArgumentException> { ResponseNameGenerator.generate("") }
    }

    @Test
    fun `should throw when operationId is blank`() {
        shouldThrow<IllegalArgumentException> { ResponseNameGenerator.generate("   ") }
    }
}
