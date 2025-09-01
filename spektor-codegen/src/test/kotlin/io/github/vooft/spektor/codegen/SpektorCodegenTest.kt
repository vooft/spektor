package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles
import io.github.vooft.spektor.test.TestFiles.listFile
import io.github.vooft.spektor.test.TestFiles.pathVarFile
import io.github.vooft.spektor.test.TestFiles.requestBodyFile
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class SpektorCodegenTest {
    @Test
    fun test() {
        val parser = SpektorParser()
        val schema = parser.parse(listOf(listFile, pathVarFile, requestBodyFile))

        val config = SpektorCodegenConfig(
            basePackage = "io.github.vooft.spektor.example",
            specRoot = TestFiles.rootPath,
        )

        val codegen = SpektorCodegen(config)
        val context = codegen.generate(schema)

        codegen.write(context, Paths.get("../generated"))
    }
}
