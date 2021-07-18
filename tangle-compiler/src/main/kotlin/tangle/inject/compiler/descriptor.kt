package tangle.inject.compiler

import com.squareup.anvil.compiler.internal.argumentType
import com.squareup.anvil.compiler.internal.asClassName
import com.squareup.anvil.compiler.internal.classDescriptorForType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation.FROM_BACKEND
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.TypeNullability.NULLABLE
import org.jetbrains.kotlin.types.typeUtil.nullability
import org.jetbrains.kotlin.types.typeUtil.representativeUpperBound
import org.jetbrains.kotlin.types.typeUtil.supertypes

fun ClassDescriptor.isFragment() = defaultType
  .supertypes()
  .any { it.classDescriptorForType().fqNameSafe == FqNames.androidxFragment }

fun ClassDescriptor.memberInjectedProperties(): List<PropertyDescriptor> =
  unsubstitutedMemberScope.memberInjectedProperties()

fun ClassDescriptor.memberInjectedProperties(
  module: ModuleDescriptor
): List<Parameter> =
  unsubstitutedMemberScope.memberInjectedProperties()
    .mapToParameters(module)

fun MemberScope.memberInjectedProperties(): List<PropertyDescriptor> =
  getVariableNames()
    .flatMap { getContributedVariables(it, FROM_BACKEND) }
    .filter { it.hasAnnotation(FqNames.inject) }

fun PropertyDescriptor.hasAnnotation(annotationFqName: FqName): Boolean {

  // `@Inject lateinit var` is really `@field:Inject lateinit var`, which needs `backingField`
  return backingField?.annotations?.hasAnnotation(annotationFqName)
    ?: annotations.hasAnnotation(annotationFqName)
}

fun PropertyDescriptor.isNullable() = type.nullability() == NULLABLE
fun KotlinType.isNullable() = nullability() == NULLABLE
fun KotlinType.toClassName(): ClassName = classDescriptorForType()
  .asClassName()

fun KotlinType.fqNameOrNull(): FqName? = classDescriptorForType()
  .fqNameOrNull()

fun TypeParameterDescriptor.boundClassName(): ClassName = representativeUpperBound
  .classDescriptorForType()
  .asClassName()

fun PropertyDescriptor.tangleParamNameOrNull(): String? {
  return annotations.findAnnotation(FqNames.tangleParam)
    ?.argumentValue("name")
    ?.value
    ?.toString()
}

fun PropertyDescriptor.requireTangleParamName(): String {
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

fun AnnotationDescriptor.toAnnotationSpec(
  module: ModuleDescriptor
): AnnotationSpec = AnnotationSpec(type.toClassName()) {
  qualifierArgumentsOrNull()
    ?.forEach { (name, value) ->
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

fun AnnotationDescriptor.isQualifier() = qualifierArgumentsOrNull() != null

fun AnnotationDescriptor.qualifierArgumentsOrNull() = type
  .classDescriptorForType()
  // Often entries are annotated with @Inject, in this case we know it's not a qualifier and we
  // can stop early.
  .takeIf { it.fqNameOrNull() != FqNames.inject }
  ?.annotations
  ?.findAnnotation(FqNames.qualifier)
  ?.allValueArguments

fun List<PropertyDescriptor>.mapToParameters(
  module: ModuleDescriptor
) = mapIndexed { index, propertyDescriptor ->

  val type = propertyDescriptor.type
  val typeFqName = propertyDescriptor.fqNameOrNull()

  val isWrappedInProvider = typeFqName == FqNames.provider
  val isWrappedInLazy = typeFqName == FqNames.daggerLazy

  val annotations = propertyDescriptor.annotations.toList()

  val typeName = when {
    propertyDescriptor.isNullable() -> type.toClassName()
      .copy(nullable = true)

    isWrappedInLazy || isWrappedInProvider -> propertyDescriptor.typeParameters
      .first()
      .boundClassName()

    else -> type.toClassName()
  }.withJvmSuppressWildcardsIfNeeded(propertyDescriptor)

  val tangleParamName = propertyDescriptor.tangleParamNameOrNull()

  val qualifiers = annotations.qualifierAnnotationSpecs(module)

  Parameter(
    name = propertyDescriptor.name.asString(),
    typeName = typeName,
    providerTypeName = typeName.wrapInProvider(),
    lazyTypeName = typeName.wrapInLazy(),
    isWrappedInProvider = isWrappedInProvider,
    isWrappedInLazy = isWrappedInLazy,
    tangleParamName = tangleParamName,
    qualifiers = qualifiers
  )
}
