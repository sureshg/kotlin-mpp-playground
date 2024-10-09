import common.jvmRunArgs

plugins {
  plugins.kotlin.jvm
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.depmgmt)
  `kotlin-spring`
  // org.graalvm.buildtools.native
  // plugins.publishing
}

description = "Kotlin SpringBoot app"

springBoot { buildInfo {} }

dependencies {
  implementation(projects.shared)
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  runtimeOnly("org.postgresql:postgresql")
  // implementation("org.springframework.boot:spring-boot-starter-jdbc")
  // implementation("org.springframework.boot:spring-boot-starter-security")
  // implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  // developmentOnly("org.springframework.boot:spring-boot-docker-compose")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:postgresql")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Fix for https://github.com/Kotlin/dokka/issues/3472
// configurations
//     .matching { it.name.startsWith("dokka") }
//     .configureEach {
//       resolutionStrategy.eachDependency {
//         if (requested.group.startsWith("com.fasterxml.jackson")) {
//           useVersion("2.15.3")
//         }
//       }
//     }

tasks { bootRun { jvmArgs = project.jvmRunArgs } }
