package common

object Repo {

  const val MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/"

  const val SONATYPE_SNAPSHOT = "https://oss.sonatype.org/content/repositories/snapshots/"

  const val SONATYPE_SNAPSHOT_01 = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

  const val NODEJS = "https://nodejs.org/dist/"

  const val YARN = "https://github.com/yarnpkg/yarn/releases/download/"

  const val KOTLIN_NATIVE = "https://download.jetbrains.com/kotlin/native/builds/"

  const val COMPOSE_MULTIPLATFORM_DEV = "https://maven.pkg.jetbrains.space/public/p/compose/dev"

  const val KOBWEB = "https://us-central1-maven.pkg.dev/varabyte-repos/public"

  const val JETPACK_COMPOSE = "https://androidx.dev/storage/compose-compiler/repository"

  const val KOTLIN_WASM_EXPERIMENTAL =
      "https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental"

  /** The GitHub username */
  val GITHUB_USER = System.getenv("GITHUB_USER").orEmpty()

  /** GitHub personal access token */
  val GITHUB_TOKEN = System.getenv("GITHUB_TOKEN").orEmpty()

  /**
   * Generates the URL for the GitHub package repository based on the owner and repository name.
   *
   * @param owner The owner of the GitHub repository.
   * @param repository The name of the GitHub repository.
   * @return The URL of the GitHub package repository.
   */
  fun githubPackage(owner: String, repository: String) =
      "https://maven.pkg.github.com/${owner.lowercase()}/$repository"

  /**
   * Returns the latest download URL for a given [groupId] and [artifactId] from Maven Central.
   *
   * @param groupId the group ID of the Maven artifact
   * @param artifactId the artifact ID of the Maven artifact
   * @return the latest download URL for the specified Maven artifact
   */
  fun latestDownloadUrl(groupId: String, artifactId: String) =
      "https://search.maven.org/remote_content?g=${groupId}&a=${artifactId}&v=LATEST"
}
