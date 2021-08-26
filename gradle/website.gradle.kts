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

/**
 * Looks for all references to Tangle artifacts in the md/mdx files
 * in the un-versioned /website/docs. Updates all versions to the pre-release version.
 */
val updateWebsiteNextDocsVersionRefs by tasks.registering {
  doLast {

    val version = project.extra.properties["VERSION_NAME"] as String

    fileTree("$rootDir/website/docs") {
      include("**/*.md*")
    }
      .forEach { file ->
        file.updateTangleVersionRef(version)
      }
  }
}

/**
 * Looks for all references to Tangle artifacts in the project README.md
 * to the current released version.
 */
val updateProjectReadmeVersionRefs by tasks.registering {
  doLast {

    val version = project.extra.properties["VERSION_NAME"] as String

    File("$rootDir/README.md")
      .updateTangleVersionRef(version)
  }
}

fun File.updateTangleVersionRef(version: String) {

  val group = project.extra.properties["GROUP"] as String

  val pluginRegex = """^([^'"\n]*['"])$group[^'"]*(['"].*) version (['"])[^'"]*(['"])${'$'}""".toRegex()
  val moduleRegex = """^([^'"\n]*['"])$group:([^:]*):[^'"]*(['"].*)${'$'}""".toRegex()

  val newText = readText()
    .lines()
    .joinToString("\n") { line ->
      line
        .replace(pluginRegex) { matchResult ->

          val (preId, postId, preVersion, postVersion) = matchResult.destructured

          "$preId$group$postId version $preVersion$version$postVersion"
        }
        .replace(moduleRegex) { matchResult ->

          val (config, module, suffix) = matchResult.destructured

          "$config$group:$module:$version$suffix"
        }
    }

  writeText(newText)
}

val startSite by tasks.registering(Exec::class) {
  workingDir("./website")
  commandLine("npm", "run", "start")
}

val versionDocs by tasks.registering(Exec::class) {
  workingDir("./website")
  val version = project.extra.properties["VERSION_NAME"] as String
  commandLine("npm", "run", "docusaurus", "docs:version", version)
}

val updateWebsiteApiDocs by tasks.registering(Copy::class) {

  doFirst {
    delete(
      fileTree("./website/static/api") {
        exclude("**/styles/*")
      }
    )
  }

  dependsOn(tasks.findByName("knit"))

  from(
    fileTree("$buildDir/dokka/htmlMultiModule") {
      exclude("**/styles/*")
    }
  )

  into("./website/static/api")
}
