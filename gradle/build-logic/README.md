### Build Logic of the projects

#### Run the `Gradle Best Practices` plugin on the build logic:

```bash
$ ./gradlew -p gradle/build-logic :bestPracticesBaseline
$ ./gradlew checkBuildLogicBestPractices
# ./gradlew -p gradle/build-logic checkBestPractices
```

#### Submit Dependency Graph to [Github Dependabot](https://github.com/gradle/github-dependency-graph-gradle-plugin)

* init.gradle.kts

```kotlin
import org.gradle.github.GitHubDependencyGraphPlugin

initscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies { classpath("org.gradle:github-dependency-graph-gradle-plugin:+") }
}

apply<GitHubDependencyGraphPlugin>()
```

* Run

```bash
$ export GITHUB_DEPENDENCY_GRAPH_JOB_ID="42"
  export GITHUB_DEPENDENCY_GRAPH_JOB_CORRELATOR="dep-graph"
  export GITHUB_REF="refs/heads/main"
  export GITHUB_SHA=$(git rev-parse HEAD)
  export GITHUB_WORKSPACE=$(pwd)

$ ./gradlew -init-script gradle/build-logic/init.gradle.kts build

# Submit the dependency graph to GitHub
$ curl -L \
    -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer $GITHUB_TOKEN"\
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/repos/gradle/github-dependency-graph-gradle-plugin/dependency-graph/snapshots \
    -d @build/reports/github-dependency-graph-snapshots/${GITHUB_DEPENDENCY_GRAPH_JOB_CORRELATOR}.json
```

#### References

* [Gradle Best Practices](https://github.com/liutikas/gradle-best-practices)
* [Gradle Best Practices Plugin](https://github.com/autonomousapps/gradle-best-practices-plugin)
* [Stampeding Elephants](https://developer.squareup.com/blog/stampeding-elephants/)
* [Stop using buildSrc](https://proandroiddev.com/stop-using-gradle-buildsrc-use-composite-builds-instead-3c38ac7a2ab3)
* [Project Dependency Graph Script](https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle)
* <details> <summary>Gradle Providers and Properties </summary>

   ```kotlin
    val p1: Property<String> =  project.objects.property<String>().convention("prop")
    val p2: Provider<String> =  project.providers.provider { "provider" }
    val p3: Provider<String> =  project.providers.environmentVariable( "ENV_VAR" )
    val p4: Provider<String> =  project.providers.systemProperty("sys.prop")
   ```
  </details>
