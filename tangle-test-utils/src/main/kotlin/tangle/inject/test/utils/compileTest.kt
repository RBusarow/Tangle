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

package tangle.inject.test.utils

import com.squareup.anvil.compiler.AnvilCommandLineProcessor
import com.squareup.anvil.compiler.AnvilComponentRegistrar
import com.squareup.anvil.compiler.internal.capitalize
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.Result
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import dagger.Component
import dagger.Module
import dagger.Subcomponent
import dagger.internal.codegen.ComponentProcessor
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.config.JvmTarget
import java.io.File
import java.io.OutputStream
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Borrowed from Anvil with some slight adaptations
 * https://github.com/square/anvil/blob/399fcae9bd4bbf6f76ab675743848b0f06af7c11/compiler-utils/src/testFixtures/java/com/squareup/anvil/compiler/internal/testing/TestUtils.kt#L27
 */
@Suppress("LongParameterList")
fun compileTangle(
  vararg sources: String,
  enableDaggerAnnotationProcessor: Boolean = false,
  generateDaggerFactories: Boolean = false,
  generateDaggerFactoriesOnly: Boolean = false,
  disableComponentMerging: Boolean = false,
  allWarningsAsErrors: Boolean = true,
  useIR: Boolean = true,
  messageOutputStream: OutputStream = System.out,
  workingDir: File? = null,
  block: Result.() -> Unit = { }
): Result {
  return KotlinCompilation()
    .apply {
      compilerPlugins = listOf(AnvilComponentRegistrar())
      this.useIR = useIR
      useOldBackend = !useIR
      inheritClassPath = true
      jvmTarget = JvmTarget.JVM_1_8.description
      verbose = false
      this.allWarningsAsErrors = allWarningsAsErrors
      this.messageOutputStream = messageOutputStream

      if (workingDir != null) {
        this.workingDir = workingDir
      }

      if (enableDaggerAnnotationProcessor) {
        annotationProcessors = listOf(ComponentProcessor())
      }

      val anvilCommandLineProcessor = AnvilCommandLineProcessor()
      commandLineProcessors = listOf(anvilCommandLineProcessor)

      pluginOptions = listOf(
        PluginOption(
          pluginId = anvilCommandLineProcessor.pluginId,
          optionName = "src-gen-dir",
          optionValue = File(workingDir, "build/anvil").absolutePath
        ),
        PluginOption(
          pluginId = anvilCommandLineProcessor.pluginId,
          optionName = "generate-dagger-factories",
          optionValue = generateDaggerFactories.toString()
        ),
        PluginOption(
          pluginId = anvilCommandLineProcessor.pluginId,
          optionName = "generate-dagger-factories-only",
          optionValue = generateDaggerFactoriesOnly.toString()
        ),
        PluginOption(
          pluginId = anvilCommandLineProcessor.pluginId,
          optionName = "disable-component-merging",
          optionValue = disableComponentMerging.toString()
        )
      )

      this.sources = sources.map { content ->
        val packageDir = content.lines()
          .firstOrNull { it.trim().startsWith("package ") }
          ?.substringAfter("package ")
          ?.replace('.', '/')
          ?.let { "$it/" }
          ?: ""

        val name = "${this.workingDir.absolutePath}/sources/src/main/kotlin/$packageDir/Source.kt"
        with(File(name).parentFile) {
          check(exists() || mkdirs())
        }

        SourceFile.kotlin(name, contents = content, trimIndent = true)
      }
    }
    .compile()
    .also(block)
}

fun Class<*>.moduleFactoryClass(
  providerMethodName: String,
  companion: Boolean = false
): Class<*> {
  val companionString = if (companion) "_Companion" else ""
  val enclosingClassString = enclosingClass?.let { "${it.simpleName}_" } ?: ""

  return classLoader.loadClass(
    "${packageName()}$enclosingClassString$simpleName$companionString" +
      "_${providerMethodName.capitalize()}Factory"
  )
}

fun Class<*>.moduleClass(): Class<*> {
  val enclosingClassString = enclosingClass?.let { "${it.simpleName}_" } ?: ""

  return classLoader.loadClass("${packageName()}$enclosingClassString${simpleName}_Factory")
}

fun Class<*>.providerClass(): Class<*> {
  val enclosingClassString = enclosingClass?.let { "${it.simpleName}_" } ?: ""

  return classLoader.loadClass("${packageName()}$enclosingClassString${simpleName}_Provider")
}

fun Class<*>.createFunction(): Method {
  return methods.first { it.name == "create" }
}

fun Class<*>.factoryFunction(): Method {
  return declaredMethods.first { it.name == "factory" }
}

fun Class<*>.getterFunction(): Method {
  return getDeclaredMethod("get")
}

fun Class<*>.providerFactoryClass(): Class<*> {
  val enclosingClassString = enclosingClass?.let { "${it.simpleName}_" } ?: ""

  return classLoader.loadClass(
    "${packageName()}$enclosingClassString${simpleName}_Provider_Factory"
  )
}

fun Class<*>.implClass(): Class<*> {
  val enclosingClassString = enclosingClass?.let { "${it.simpleName}_" } ?: ""
  return classLoader.loadClass("${packageName()}$enclosingClassString${simpleName}_Impl")
}

fun Class<*>.membersInjector(): Class<*> {
  val enclosingClassString = enclosingClass?.let { "${it.simpleName}_" } ?: ""

  return classLoader.loadClass(
    "${packageName()}$enclosingClassString${simpleName}_MembersInjector"
  )
}

fun Class<*>.tangleInjector(): Class<*> {
  val enclosingClassString = enclosingClass?.let { "${it.simpleName}_" } ?: ""

  return classLoader.loadClass(
    "${packageName()}${enclosingClassString}Tangle_${simpleName}Injector"
  )
}

fun Class<*>.packageName(): String = `package`.name.let {
  if (it.isBlank()) "" else "$it."
}

val Class<*>.daggerComponent: Component
  get() = annotations.filterIsInstance<Component>()
    .also { it.size shouldBe 1 }
    .first()

val Class<*>.daggerSubcomponent: Subcomponent
  get() = annotations.filterIsInstance<Subcomponent>()
    .also { it.size shouldBe 1 }
    .first()

val Class<*>.daggerModule: Module
  get() = annotations.filterIsInstance<Module>()
    .also { it.size shouldBe 1 }
    .first()

infix fun Class<*>.extends(other: Class<*>): Boolean = other.isAssignableFrom(this)

infix fun KClass<*>.extends(other: KClass<*>): Boolean =
  other.java.isAssignableFrom(this.java)

fun Array<KClass<*>>.withoutAnvilModule(): List<KClass<*>> = toList().withoutAnvilModule()

fun Collection<KClass<*>>.withoutAnvilModule(): List<KClass<*>> =
  filterNot { it.qualifiedName!!.startsWith("anvil.module") }

fun Any.invokeGet(vararg args: Any?): Any {
  val method = this::class.java.methods.first { it.name == "get" }
  return method.invoke(this, *args)
}

fun Any.invokeCreate(vararg args: Any?): Any {

  return this::class.java.methods.first { it.name == "create" }
    .use { it.invoke(this@invokeCreate, *args) }
}

@OptIn(ExperimentalStdlibApi::class)
fun Any.fieldsValues(): Map<String, Any?> {
  val thisRef = this
  return buildMap {
    thisRef::class.java.fields.forEach { field ->
      put(field.name, field.get(thisRef))
    }
  }
}

@OptIn(ExperimentalStdlibApi::class)
fun Any.declaredFieldsValues(): Map<String, Any?> {
  val thisRef = this
  return buildMap {
    thisRef::class.java.fields.forEach { field ->
      put(field.name, field.get(thisRef))
    }
  }
}

@Suppress("UNCHECKED_CAST")
fun <T> Annotation.getValue(): T =
  this::class.java.declaredMethods.single { it.name == "value" }.invoke(this) as T
