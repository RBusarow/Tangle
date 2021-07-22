package tangle.inject.compiler

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class)
class TangleCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = "tangle.compiler"

  override val pluginOptions: Collection<AbstractCliOption> = listOf(
    CliOption(
      optionName = fooName,
      valueDescription = "<true|false>",
      description = "Whether Foo should do Foo.",
      required = false,
      allowMultipleOccurrences = false
    )
  )

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration
  ) {
    when (option.optionName) {
      fooName -> configuration.put(fooKey, value)
    }
  }
}

internal const val fooName = "fooo"
internal val fooKey = CompilerConfigurationKey.create<String>("anvil $fooName")
