pluginManagement { includeBuild("gradle/build-logic") }

plugins { id("settings.repos") }

rootProject.name = "kotlin-mpp-playground"

include(":common")

include(":api-client")

include(":backend")

include(":web")

include(":benchmarks")

include(":compose:web")

include(":compose:desktop")

include(":dep-mgmt:bom")

include(":dep-mgmt:catalog")

include(":ksp:processor")
