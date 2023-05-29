plugins { alias(libs.plugins.benmanes) }

tasks.dependencyUpdates { checkConstraints = true }
