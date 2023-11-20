/*
 * Copyright (C) 2022 Rick Busarow
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

@file:Suppress("TooManyFunctions")
@file:OptIn(com.squareup.anvil.annotations.ExperimentalAnvilApi::class)

package tangle.inject.compiler

import com.squareup.anvil.compiler.internal.argumentType
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.classDescriptor
import com.squareup.anvil.compiler.internal.reference.ClassReference
import com.squareup.anvil.compiler.internal.reference.MemberFunctionReference
import com.squareup.anvil.compiler.internal.reference.MemberPropertyReference
import com.squareup.anvil.compiler.internal.reference.ParameterReference
import com.squareup.anvil.compiler.internal.reference.Visibility.PRIVATE
import com.squareup.anvil.compiler.internal.reference.allSuperTypeClassReferences
import com.squareup.anvil.compiler.internal.reference.argumentAt
import com.squareup.anvil.compiler.internal.reference.asClassName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.TypeNullability.NULLABLE
import org.jetbrains.kotlin.types.typeUtil.nullability
import org.jetbrains.kotlin.types.typeUtil.representativeUpperBound

fun ClassReference.isFragment() = allSuperTypeClassReferences(true)
  .any { it.fqName == FqNames.androidxFragment }

fun ClassReference.isViewModel() = allSuperTypeClassReferences(true)
  .any { it.fqName == FqNames.androidxViewModel }

/**
 * Returns all member-injected parameters for the receiver class *and any superclasses*.
 *
 * We use Psi whenever possible, to support generated code.
 *
 * Order is important. Dagger expects the properties of the most-upstream class to be listed first
 * in a factory's constructor.
 *
 * Given the hierarchy: Impl -> Middle -> Base The order of dependencies in `Impl_Factory`'s
 * constructor should be: Base -> Middle -> Impl
 */
fun ClassReference.memberInjectedParameters(): List<MemberInjectParameter> {
  return allSuperTypeClassReferences(includeSelf = true)
    .filterNot { it.isInterface() }
    .toList()
    .foldRight(listOf()) { classReference, acc ->
      acc + classReference.declaredMemberInjectParameters(acc)
    }
}

/**
 * @param superParameters injected parameters from any super-classes, regardless of whether they're
 *   overridden by the receiver class
 * @return the member-injected parameters for this class only, not including any super-classes
 */
private fun ClassReference.declaredMemberInjectParameters(
  superParameters: List<Parameter>
): List<MemberInjectParameter> {
  return properties
    .filter { it.isAnnotatedWith(FqNames.inject) }
    .filter { it.visibility() != PRIVATE }
    .fold(listOf()) { acc, property ->
      val uniqueName = property.name.uniqueParameterName(superParameters, acc)
      acc + property.toMemberInjectParameter(uniqueName = uniqueName)
    }
}

/**
 * Returns a name which is unique when compared to the [Parameter.originalName] of the
 * [superParameters] argument.
 *
 * This is necessary for member-injected parameters, because a subclass may override a parameter
 * which is member-injected in the super. The `MembersInjector` corresponding to the subclass must
 * have unique constructor parameters for each declaration, so their names must be unique.
 *
 * This mimics Dagger's method of unique naming. If there are three parameters named "foo", the
 * unique parameter names will be [foo, foo2, foo3].
 */
internal fun String.uniqueParameterName(
  vararg superParameters: List<Parameter>
): String {
  val numDuplicates = superParameters.sumOf { list ->
    list.count { it.name == this }
  }

  return if (numDuplicates == 0) {
    this
  } else {
    this + (numDuplicates + 1)
  }
}

fun MemberScope.memberInjectedProperties(): List<PropertyDescriptor> {
  return getContributedDescriptors(DescriptorKindFilter.VARIABLES)
    .filterIsInstance<PropertyDescriptor>()
    .filter { it.hasAnnotation(FqNames.inject) }
}

fun CallableMemberDescriptor.hasAnnotation(annotationFqName: FqName): Boolean {
  return annotations.hasAnnotation(annotationFqName)
}

fun PropertyDescriptor.hasAnnotation(annotationFqName: FqName): Boolean {
  // `@Inject lateinit var` is really `@field:Inject lateinit var`, which needs `backingField`
  return backingField?.annotations?.hasAnnotation(annotationFqName)
    ?: annotations.hasAnnotation(annotationFqName)
}

fun PropertyDescriptor.isNullable() = type.nullability() == NULLABLE
fun KotlinType.isNullable() = nullability() == NULLABLE

fun KotlinType.fqNameOrNull(): FqName? = classDescriptor()
  .fqNameOrNull()

fun TypeParameterDescriptor.boundClassName(): ClassName = representativeUpperBound
  .classDescriptor()
  .asClassName()

fun MemberPropertyReference.tangleParamNameOrNull(): String? {
  return annotations.find { it.fqName == FqNames.tangleParam }
    ?.argumentAt("name", 0)
    ?.value<String>()
    ?.toString()
}

fun ParameterReference.tangleParamNameOrNull(): String? {
  return annotations.find { it.fqName == FqNames.tangleParam }
    ?.argumentAt("name", 0)
    ?.value<String>()
    ?.toString()
}

fun CallableMemberDescriptor.tangleParamNameOrNull(): String? {
  return annotations.findAnnotation(FqNames.tangleParam)
    ?.argumentValue("name")
    ?.value
    ?.toString()
}

fun CallableMemberDescriptor.requireTangleParamName(): String {
  return tangleParamNameOrNull()
    ?: throw TangleCompilationException(
      this,
      "could not find a @TangleParam annotation for parameter `${name.asString()}`"
    )
}

fun ParameterReference.requireTangleParamName(): String {
  return tangleParamNameOrNull()
    ?: throw TangleCompilationException(
      (this.declaringFunction as MemberFunctionReference).declaringClass,
      "could not find a @TangleParam annotation for parameter `$name`"
    )
}

fun List<AnnotationDescriptor>.qualifierAnnotationSpecs(
  module: ModuleDescriptor
): List<AnnotationSpec> = mapNotNull {
  if (it.fqName == FqNames.inject) return@mapNotNull null

  val classDescriptor = it.annotationClass ?: return@mapNotNull null

  val qualifierAnnotation = classDescriptor.annotations
    .findAnnotation(FqNames.qualifier)
    ?: return@mapNotNull null

  AnnotationSpec(classDescriptor.asClassName()) {
    qualifierAnnotation.allValueArguments
      .forEach { (name, value) ->
        when (value) {
          is KClassValue -> {
            val className = value.argumentType(module)
              .classDescriptor()
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

fun MemberPropertyReference.toMemberInjectParameter(
  uniqueName: String
): MemberInjectParameter {
  val propertyReference = this

  val annotations = propertyReference.annotations
  val type = propertyReference.type()

  val typeFqName = type.asClassReference().fqName

  val isWrappedInProvider = typeFqName == FqNames.provider
  val isWrappedInLazy = typeFqName == FqNames.daggerLazy

  val unwrappedTypeOrSelf = if (isWrappedInLazy || isWrappedInProvider) {
    type.unwrappedTypes.first()
  } else type

  val typeName = unwrappedTypeOrSelf.asTypeName()
    .withJvmSuppressWildcardsIfNeeded(module, unwrappedTypeOrSelf)

  val tangleParamName = propertyReference.tangleParamNameOrNull()

  val qualifiers = annotations.qualifierAnnotationSpecs(module)

  val containingDeclaration = propertyReference.declaringClass
  val packageName = propertyReference.declaringClass.packageFqName.asString()

  val containingClass = containingDeclaration.asClassName()
  val memberInjectorClassName = "${containingClass}_MembersInjector"
  val memberInjectorClass = ClassName(packageName, memberInjectorClassName)

  return MemberInjectParameter(
    name = uniqueName,
    typeName = typeName,
    providerTypeName = typeName.wrapInProvider(),
    lazyTypeName = typeName.wrapInLazy(),
    isWrappedInProvider = isWrappedInProvider,
    isWrappedInLazy = isWrappedInLazy,
    tangleParamName = tangleParamName,
    qualifiers = qualifiers,
    memberInjectorClass = memberInjectorClass
  )
}

fun DeclarationDescriptor.requireContainingPackage() = containingPackage()
  ?: throw TangleCompilationException(
    this,
    "Cannot determine the package name for ${fqNameSafe.asString()}."
  )
