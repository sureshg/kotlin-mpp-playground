import org.gradle.github.GitHubDependencyGraphPlugin

initscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
  dependencies { classpath("org.gradle:github-dependency-graph-gradle-plugin:+") }
}

apply<GitHubDependencyGraphPlugin>()
