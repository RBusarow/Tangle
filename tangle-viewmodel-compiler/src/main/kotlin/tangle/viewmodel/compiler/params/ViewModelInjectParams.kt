package tangle.viewmodel.compiler.params

import com.squareup.kotlinpoet.ClassName
import org.jetbrains.kotlin.name.FqName

data class TangleScopeModule(
  val packageName: String,
  val viewModelParamsList: List<ViewModelInjectParams>
)

sealed interface ViewModelInjectParams {
  val packageName: String
  val scopeName: FqName
  val viewModelClassName: ClassName
  val factoryFunctionName: String
}
