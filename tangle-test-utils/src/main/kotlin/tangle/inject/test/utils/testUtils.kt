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

import com.squareup.anvil.annotations.ExperimentalAnvilApi
import com.tschuchort.compiletesting.KotlinCompilation.Result
import dagger.internal.Factory
import org.jetbrains.kotlin.utils.addToStdlib.cast
import java.lang.reflect.Executable
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@ExperimentalAnvilApi
val Member.isStatic: Boolean
  get() = Modifier.isStatic(modifiers)

@ExperimentalAnvilApi
val Member.isAbstract: Boolean
  get() = Modifier.isAbstract(modifiers)

/**
 * Creates a new instance of this class with the given arguments. This method assumes that this
 * class only declares a single constructor.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Class<T>.createInstance(
  vararg initargs: Any?
): T = declaredConstructors.single().use { it.newInstance(*initargs) } as T

@Suppress("UNCHECKED_CAST")
fun <T : Any> Class<T>.newInstanceStatic(
  vararg initargs: Any?
): T = declaredMethods.filter { it.isStatic }
  .single { it.name == "newInstance" }
  .invoke(null, *initargs) as T

@Suppress("UNCHECKED_CAST")
fun <T : Any> Class<T>.createStatic(
  vararg initargs: Any?
): T = declaredMethods.filter { it.isStatic }
  .single { it.name == "create" }
  .invoke(null, *initargs) as T

@Suppress("UNCHECKED_CAST")
fun <T> T.factoryGet(): Any = cast<Factory<*>>().get()

inline fun <T, E : Executable> E.use(block: (E) -> T): T {
  // Deprecated since Java 9, but many projects still use JDK 8 for compilation.
  @Suppress("DEPRECATION")
  val original = isAccessible

  return try {
    isAccessible = true
    block(this)
  } finally {
    isAccessible = original
  }
}

const val MODULE_PACKAGE_PREFIX = "anvil.module"

val Result.daggerAppComponent: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.DaggerAppComponent")

val Result.daggerAppComponent2: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.DaggerAppComponent2")

val Result.contributingInterface: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.ContributingInterface")

val Result.secondContributingInterface: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.SecondContributingInterface")

val Result.innerInterface: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.SomeClass\$InnerInterface")

val Result.parentInterface: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.ParentInterface")

val Result.componentInterface: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.ComponentInterface")

val Result.componentInterfaceAnvilModule: Class<*>
  get() = classLoader
    .loadClass("$MODULE_PACKAGE_PREFIX.tangle.inject.tests.ComponentInterfaceAnvilModule")

val Result.subcomponentInterface: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.SubcomponentInterface")

val Result.subcomponentInterfaceAnvilModule: Class<*>
  get() = classLoader
    .loadClass("$MODULE_PACKAGE_PREFIX.tangle.inject.tests.SubcomponentInterfaceAnvilModule")

val Result.daggerModule1: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.DaggerModule1")

val Result.assistedService: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.AssistedService")

val Result.assistedServiceFactory: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.AssistedServiceFactory")

val Result.daggerModule1AnvilModule: Class<*>
  get() = classLoader
    .loadClass("$MODULE_PACKAGE_PREFIX.tangle.inject.tests.DaggerModule1AnvilModule")

val Result.daggerModule2: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.DaggerModule2")

val Result.daggerModule3: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.DaggerModule3")

val Result.daggerModule4: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.DaggerModule4")

val Result.innerModule: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.ComponentInterface\$InnerModule")

val Result.injectClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.InjectClass")

val Result.myViewModelClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.MyViewModel")

val Result.myViewModelFactoryClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.MyViewModel\$Factory")

val Result.myFragmentClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.MyFragment")

val Result.myFragmentFactoryClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.MyFragment\$Factory")

val Result.myFragmentFactoryImplClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.MyFragment_Factory_Impl")

val Result.tangleUnitFragmentModuleClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.Tangle_Unit_Fragment_Module")

val Result.tangleUnitFragmentInjectModuleClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.Tangle_Unit_FragmentInject_Module")

val Result.tangleUnitFragmentModuleCompanionClass: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.Tangle_Unit_Fragment_Module\$Companion")

val Result.anyQualifier: Class<*>
  get() = classLoader.loadClass("tangle.inject.tests.AnyQualifier")

val Result.bindMyFragment: Method
  get() = tangleUnitFragmentModuleClass.getDeclaredMethod("bind_MyFragment", myFragmentClass)

val Result.provideMyFragment: Method
  get() = tangleUnitFragmentModuleCompanionClass.getDeclaredMethod("provide_MyFragment")

fun Method.annotationClasses() = annotations.map { it.annotationClass }
fun Class<*>.annotationClasses() = annotations.map { it.annotationClass }
fun KClass<*>.property(name: String) = memberProperties
  .first { it.name == name }
