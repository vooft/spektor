package io.github.vooft.spektor.codegen.common

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec

data class TypeAndClass(val type: TypeSpec, val className: ClassName, val imports: Set<Import> = setOf()) {
    data class Import(val packageName: String, val name: String)
}
