import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import java.util.Optional

/** Get the `libs` version catalog for this project */
val Project.versionCatalog: VersionCatalog
  get() = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")

/** Get the library dependency with a given [alias] for this project's version catalog */
fun Project.namedLib(alias: String): Provider<MinimalExternalModuleDependency> =
  versionCatalog.findLibrary(alias).expectForAlias(alias)

/** Get the plugin dependency with a given [alias] for this project's version catalog */
fun Project.namedPlugin(alias: String): String =
  versionCatalog.findPlugin(alias).expectForAlias(alias).get().pluginId

/** Get the version specification with a given [alias] for this project's version catalog */
fun Project.namedVersion(alias: String) =
  versionCatalog.findVersion(alias).get().requiredVersion

/** Get the full maven coordinates for a module dependancy, for use with forcing specific versions */
val Provider<MinimalExternalModuleDependency>.fullMavenCoordinates: String
  get() = get().toString()

private inline fun <reified T> Optional<Provider<T>>.expectForAlias(alias: String): Provider<T> = orElseThrow {
  NoSuchElementException("Could not get Provider<${T::class.qualifiedName}> with alias $alias")
}

