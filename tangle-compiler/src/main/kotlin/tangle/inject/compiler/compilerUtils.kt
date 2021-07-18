package tangle.inject.compiler

import com.squareup.anvil.compiler.internal.classDescriptorForType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.typeUtil.supertypes

fun ClassDescriptor.isFragment() = defaultType
  .supertypes()
  .any { it.classDescriptorForType().fqNameSafe == FqNames.androidxFragment }
