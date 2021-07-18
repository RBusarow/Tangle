package tangle.fragment.compiler

import com.squareup.anvil.compiler.internal.fqName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import org.jetbrains.kotlin.name.FqName
import tangle.inject.compiler.ClassNames
import tangle.inject.compiler.FqNames
import java.io.Serializable

enum class BundleSafe(val fqName: FqName, val className: TypeName) {
  STRING(String::class.fqName, String::class.asClassName()),
  BOOLEAN(Boolean::class.fqName, Boolean::class.asClassName()),
  BYTE(Byte::class.fqName, Byte::class.asClassName()),
  CHAR(Char::class.fqName, Char::class.asClassName()),
  DOUBLE(Double::class.fqName, Double::class.asClassName()),
  FLOAT(Float::class.fqName, Float::class.asClassName()),
  INT(Int::class.fqName, Int::class.asClassName()),
  LONG(Long::class.fqName, Long::class.asClassName()),
  SHORT(Short::class.fqName, Short::class.asClassName()),
  BUNDLE(FqNames.bundle, ClassNames.bundle),
  CHAR_SEQUENCE(CharSequence::class.fqName, CharSequence::class.asClassName()),
  PARCELABLE(FqNames.parcelable, ClassNames.parcelable),
  BOOLEAN_ARRAY(BooleanArray::class.fqName, BooleanArray::class.asClassName()),
  BYTE_ARRAY(ByteArray::class.fqName, ByteArray::class.asClassName()),
  CHAR_ARRAY(CharArray::class.fqName, CharArray::class.asClassName()),
  DOUBLE_ARRAY(DoubleArray::class.fqName, DoubleArray::class.asClassName()),
  FLOAT_ARRAY(FloatArray::class.fqName, FloatArray::class.asClassName()),
  INT_ARRAY(IntArray::class.fqName, IntArray::class.asClassName()),
  LONG_ARRAY(LongArray::class.fqName, LongArray::class.asClassName()),
  SHORT_ARRAY(ShortArray::class.fqName, ShortArray::class.asClassName()),
  ARRAY(Array::class.fqName, Array::class.asClassName()),
  SERIALIZABLE(Serializable::class.fqName, Serializable::class.asClassName()),
  IBINDER(FqNames.iBinder, ClassNames.iBinder),
  SIZE(FqNames.size, ClassNames.size),
  SIZE_F(FqNames.sizeF, ClassNames.sizeF);

  companion object {

    private val typeNames = values().associateBy { it.className }
    private val fqNames = values().associateBy { it.fqName }
    fun fromTypeNameOrNull(typeName: TypeName): BundleSafe? = typeNames[typeName]
    fun contains(typeName: TypeName): Boolean = typeNames[typeName] != null
    fun contains(fqName: FqName): Boolean = fqNames[fqName] != null
  }
}
