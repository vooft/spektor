package io.github.vooft.spektor.codegen

import io.github.vooft.spektor.codegen.common.SpektorCodegenConfig
import io.github.vooft.spektor.parser.SpektorParser
import io.github.vooft.spektor.test.TestFiles
import io.github.vooft.spektor.test.TestFiles.pathParamRefFile
import io.github.vooft.spektor.test.TestFiles.queryParamRefFile
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class SpektorCodegenParamRefTest {

    private val parser = SpektorParser()

    @Test
    fun `should generate code for path parameter typed via ref`() {
        val schema = parser.parse(listOf(pathParamRefFile))

        val config = SpektorCodegenConfig(
            basePackage = "io.github.vooft.spektor.example",
            specRoot = TestFiles.rootPath,
        )

        val codegen = SpektorCodegen(config)
        val context = codegen.generate(schema)

        codegen.write(context, Paths.get("../generated"))
    }

    @Test
    fun `should generate code for query parameter typed via ref`() {
        val schema = parser.parse(listOf(queryParamRefFile))

        val config = SpektorCodegenConfig(
            basePackage = "io.github.vooft.spektor.example",
            specRoot = TestFiles.rootPath,
        )

        val codegen = SpektorCodegen(config)
        val context = codegen.generate(schema)

        codegen.write(context, Paths.get("../generated"))
    }
}
