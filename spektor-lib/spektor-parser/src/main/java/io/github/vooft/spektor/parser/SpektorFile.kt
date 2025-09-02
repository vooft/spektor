package io.github.vooft.spektor.parser

import io.github.vooft.spektor.model.SpektorPath
import io.github.vooft.spektor.model.SpektorType
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class SpektorFile(private val file: Path) {
    private val parsed = OpenAPIV3Parser().read(file.absolutePathString(), null, ParseOptions().apply { isResolve = false })

    init {
        require(file.isAbsolute) { "File path must be absolute: $file" }
    }

    private val typeResolver = SpektorTypeResolver(file)
    private val pathResolver = SpektorPathResolver(typeResolver)

    fun parseModel(ref: SpektorType.Ref): SpektorType? {
        require(ref.file == file) { "Can only parse models from the same file, expected $file, got ${ref.file}" }

        val schema = parsed.components?.schemas?.get(ref.modelName) ?: error("Model ${ref.modelName} not found in file $file")
        return typeResolver.resolve(schema)
    }

    fun parsePaths(): List<SpektorPath> {
        val allPaths = parsed.paths ?: return listOf()
        return allPaths.flatMap { (path, item) ->
            listOfNotNull(
                item.get?.toSpektorPath(path, SpektorPath.Method.GET),
                item.post?.toSpektorPath(path, SpektorPath.Method.POST),
                item.put?.toSpektorPath(path, SpektorPath.Method.PUT),
                item.delete?.toSpektorPath(path, SpektorPath.Method.DELETE),
                item.patch?.toSpektorPath(path, SpektorPath.Method.PATCH),
                item.options?.toSpektorPath(path, SpektorPath.Method.OPTIONS),
                item.head?.toSpektorPath(path, SpektorPath.Method.HEAD)
            )
        }
    }

    private fun Operation.toSpektorPath(path: String, method: SpektorPath.Method) = pathResolver.resolve(file, path, method, this)
}
