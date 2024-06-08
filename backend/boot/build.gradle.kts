plugins {
  plugins.kotlin.jvm
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.depmgmt)
  org.graalvm.buildtools.native
  `kotlin-spring`
  plugins.publishing
}

description = "Kotlin SpringBoot app"

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  // implementation("org.springframework.boot:spring-boot-starter-jdbc")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  runtimeOnly("org.postgresql:postgresql")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.springframework.security:spring-security-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  // implementation("org.springframework.boot:spring-boot-starter-security")
  // implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  // developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}
