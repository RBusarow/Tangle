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

@file:Suppress("TooManyFunctions")

package tangle.inject.compiler

import com.squareup.anvil.compiler.internal.argumentType
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.asTypeName
import com.squareup.anvil.compiler.internal.classDescriptorForType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.TypeNullability.NULLABLE
import org.jetbrains.kotlin.types.typeUtil.nullability
import org.jetbrains.kotlin.types.typeUtil.representativeUpperBound
import org.jetbrains.kotlin.types.typeUtil.supertypes
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

fun ClassDescriptor.isFragment() = defaultType
  .supertypes()
  .any { it.classDescriptorForType().fqNameSafe == FqNames.androidxFragment }

fun ClassDescriptor.isViewModel() = defaultType
  .supertypes()
  .any { it.classDescriptorForType().fqNameSafe == FqNames.androidxViewModel }

fun ClassDescriptor.memberInjectedParameters(
  module: ModuleDescriptor
): List<MemberInjectParameter> =
  memberInjectedProperties(module)
    .mapToParameters(module)

fun ClassDescriptor.memberInjectedProperties(
  module: ModuleDescriptor
): List<CallableMemberDescriptor> {

  return getAllSuperClassifiers()
    .toList()
    // start parsing at the class which is extended by the target class,
    // so that the last class is the root of the hierarchy
    .reversed()
    // no point in parsing android/androidx classes for injected params, so skip them
    .filter { it.containingPackage()?.asString()?.startsWith("android") == false }
    // look for any @Inject-annotated members.
    // Upstream properties are parsed before the properties they override.
    // If a property has already been added from a downstream class, then ignore it.
    .fold(mutableMapOf<Name, CallableMemberDescriptor>()) { acc, classifierDescriptor ->

      classifierDescriptor.fqNameSafe
        .requireClassDescriptor(module)
        .unsubstitutedMemberScope
        .memberInjectedProperties()
        .forEach {
          if (!acc.contains(it.name)) {
            acc[it.name] = it
          }
        }
      acc
    }
    .values
    .toList()
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

fun KotlinType.fqNameOrNull(): FqName? = classDescriptorForType()
  .fqNameOrNull()

fun TypeParameterDescriptor.boundClassName(): ClassName = representativeUpperBound
  .classDescriptorForType()
  .asClassName()

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

fun ValueParameterDescriptor.tangleParamNameOrNull(): String? {
  return annotations.findAnnotation(FqNames.tangleParam)
    ?.argumentValue("name")
    ?.value
    ?.toString()
}

fun ValueParameterDescriptor.requireTangleParamName(): String {
  return tangleParamNameOrNull()
    ?: throw TangleCompilationException(
      this,
      "could not find a @TangleParam annotation for parameter `${name.asString()}`"
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
              .classDescriptorForType()
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

fun AnnotationDescriptor.qualifierArgumentsOrNull() = type
  .classDescriptorForType()
  // Often entries are annotated with @Inject, in this case we know it's not a qualifier and we
  // can stop early.
  .takeIf { it.fqNameOrNull() != FqNames.inject }
  ?.annotations
  ?.findAnnotation(FqNames.qualifier)
  ?.allValueArguments

fun List<CallableMemberDescriptor>.mapToParameters(
  module: ModuleDescriptor
): List<MemberInjectParameter> {

  return map { callableMemberDescriptor ->

    val type = callableMemberDescriptor.safeAs<PropertyDescriptor>()?.type
      ?: callableMemberDescriptor.valueParameters.first().type

    val typeFqName = type.fqNameOrNull()

    val isWrappedInProvider = typeFqName == FqNames.provider
    val isWrappedInLazy = typeFqName == FqNames.daggerLazy

    val annotations = callableMemberDescriptor.annotations.toList()

    val typeName = when {
      type.isNullable() -> type.asTypeName()
        .copy(nullable = true)

      isWrappedInLazy || isWrappedInProvider ->
        type.argumentType().asTypeName()

      else -> type.asTypeName()
    }.withJvmSuppressWildcardsIfNeeded(callableMemberDescriptor)

    val tangleParamName = callableMemberDescriptor.tangleParamNameOrNull()

    val qualifiers = annotations.qualifierAnnotationSpecs(module)

    val containingDeclaration = callableMemberDescriptor.containingDeclaration
    val packageName = containingDeclaration.requireContainingPackage().asString()

    val containingClass = containingDeclaration.fqNameSafe.asClassName(module)
    val memberInjectorClassName =
      "${containingClass}_MembersInjector"
    val memberInjectorClass = ClassName(packageName, memberInjectorClassName)

    MemberInjectParameter(
      name = callableMemberDescriptor.name.asString(),
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
}

fun DeclarationDescriptor.requireContainingPackage() = containingPackage()
  ?: throw TangleCompilationException(
    this,
    "Cannot determine the package name for ${fqNameSafe.asString()}."
  )
