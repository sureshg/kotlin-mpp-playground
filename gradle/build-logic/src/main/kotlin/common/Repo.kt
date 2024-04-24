package common

object Repo {

  const val MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/"

  const val SONATYPE_SNAPSHOT = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

  const val NODEJS = "https://nodejs.org/dist/"

  const val KOBWEB = "https://us-central1-maven.pkg.dev/varabyte-repos/public"

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
