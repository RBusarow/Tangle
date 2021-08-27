/*
 * Copyright (C) 2021 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tangle.inject.compiler

import com.squareup.anvil.compiler.internal.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.jvm.jvmSuppressWildcards
import org.jetbrains.kotlin.builtins.isFunctionType
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.io.ByteArrayOutputStream

private fun String.addGeneratedByComment(): String {
  return """
  // Generated by Tangle
  // https://github.com/rbusarow/Tangle

  """.trimIndent() + this
}

private fun FileSpec.writeToString(): String {
  val stream = ByteArrayOutputStream()
  stream.writer().use {
    writeTo(it)
  }
  return stream.toString()
}

private fun FileSpec.Builder.annotateFile(): FileSpec.Builder =
  addAnnotation(
    AnnotationSpec
      .builder(Suppress::class)
      .addMember("\"DEPRECATION\"").build()
  )

fun FileSpec.Companion.buildFile(
  packageName: String,
  fileName: String,
  block: FileSpec.Builder.() -> Unit
): String = builder(packageName, fileName)
  .annotateFile()
  .apply { block() }
  .build()
  .writeToString()
  .addGeneratedByComment()

fun TypeSpec.Builder.addFunction(
  name: String,
  block: FunSpec.Builder.() -> Unit
): TypeSpec.Builder = addFunction(
  FunSpec.builder(name)
    .apply { block() }
    .build()
)

fun FunSpec(
  name: String,
  block: FunSpec.Builder.() -> Unit
): FunSpec = FunSpec.builder(name)
  .apply { block() }
  .build()

fun AnnotationSpec(
  name: ClassName,
  block: AnnotationSpec.Builder.() -> Unit
): AnnotationSpec = AnnotationSpec.builder(name)
  .apply { block() }
  .build()

fun List<KtAnnotationEntry>.qualifierAnnotationSpecs(
  module: ModuleDescriptor
): List<AnnotationSpec> = mapNotNull {

  val fqName = it.requireFqName(module)

  if (fqName == FqNames.inject) return@mapNotNull null

  val classDescriptor = fqName.requireClassDescriptor(module)

  val qualifierAnnotation = classDescriptor.annotations
    .findAnnotation(FqNames.qualifier)
    ?: return@mapNotNull null

  AnnotationSpec(classDescriptor.asClassName()) {
    qualifierAnnotation.allValueArguments
      .forEach { (name, value) ->
        when (value) {
          is KClassValue -> {
            val className = value.argumentType(module).classDescriptorForType()
              .asClassName()
            addMember("${name.asString()} = %T::class", className)
          }
          is EnumValue -> {
            val enumMember = MemberName(
              enclosingClassName = value.enumClassId.asSingleFqName()
                .asClassName(module),
              simpleName = value.enumEntryName.asString()
            )
            addMember("${name.asString()} = %M", enumMember)
          }
          // String, int, long, ... other primitives.
          else -> addMember("${name.asString()} = $value")
        }
      }
  }
}

internal fun TypeName.withJvmSuppressWildcardsIfNeeded(
  callableMemberDescriptor: CallableMemberDescriptor
): TypeName {
  // If the parameter is annotated with @JvmSuppressWildcards, then add the annotation
  // to our type so that this information is forwarded when our Factory is compiled.
  val hasJvmSuppressWildcards = callableMemberDescriptor.hasAnnotation(FqNames.jvmSuppressWildcards)

  // Add the @JvmSuppressWildcards annotation even for simple generic return types like
  // Set<String>. This avoids some edge cases where Dagger chokes.
  val isGenericType = callableMemberDescriptor.typeParameters.isNotEmpty()

  val type = callableMemberDescriptor.safeAs<PropertyDescriptor>()?.type
    ?: callableMemberDescriptor.valueParameters.first().type

  // Same for functions.
  val isFunctionType = type.isFunctionType

  return when {
    hasJvmSuppressWildcards || isGenericType -> this.jvmSuppressWildcards()
    isFunctionType -> this.jvmSuppressWildcards()
    else -> this
  }
}

fun ClassName.generateSimpleNameString(
  separator: String = "_"
): String = simpleNames.joinToString(separator)
