package common

object Repo {

  const val MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/"

  const val MAVEN_SNAPSHOT = "https://oss.sonatype.org/content/repositories/snapshots/"

  const val MAVEN_SNAPSHOT_2 = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

  const val NODEJS = "https://nodejs.org/dist/"

  const val YARN = "https://github.com/yarnpkg/yarn/releases/download/"

  const val COMPOSE_MULTIPLATFORM_DEV = "https://maven.pkg.jetbrains.space/public/p/compose/dev"

  /**
   * Returns the latest download URL for a given [groupId] and [artifactId] from Maven Central.
   *
   * @param groupId the group ID of the Maven artifact
   * @param artifactId the artifact ID of the Maven artifact
   * @return the latest download URL for the specified Maven artifact
   */
  fun latestDownloadUrl(groupId: String, artifactId: String): String {
    return "https://search.maven.org/remote_content?g=${groupId}&a=${artifactId}&v=LATEST"
  }
}
