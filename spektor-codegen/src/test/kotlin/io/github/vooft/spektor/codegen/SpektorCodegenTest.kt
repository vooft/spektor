package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles
import io.github.vooft.spektor.test.TestFiles.listFile
import io.github.vooft.spektor.test.TestFiles.pathVarFile
import io.github.vooft.spektor.test.TestFiles.requestBodyFile
import org.junit.jupiter.api.Test

class SpektorCodegenTest {
    @Test
    fun test() {
        val parser = SpektorParser()
        val schema = parser.parse(listOf(listFile, pathVarFile, requestBodyFile))

        val codegen = SpektorCodegen(TestFiles.rootPath, "io.github.vooft.spektor.example")
        codegen.generate(schema)
    }
}
