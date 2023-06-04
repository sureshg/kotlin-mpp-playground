package plugins

import dev.suresh.gradle.libs

plugins { `version-catalog` }

group = libs.versions.group.get()

catalog {
  versionCatalog {
    // version("kotlin", kotlinVersion.get())
    // library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
    from(files(project.rootProject.file("gradle/libs.versions.toml")))
  }
}
