rootProject.name = "build-logic"

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }

  repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven("https://jitpack.io")
    jcenter()
    gradlePluginPortal()
  }
}

include(":plugins")
