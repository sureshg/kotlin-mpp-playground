### Build Logic of the projects

#### Run the `Gradle Best Practices` plugin on the build logic:

```bash
$ ./gradlew checkBuildLogicBestPractices
# OR
$ ./gradlew -p gradle/build-logic checkBestPractices
```

#### Submit Dependency Graph to [Github Dependabot](https://docs.github.com/en/code-security/dependabot/dependabot-version-updates/about-dependabot-version-updates)

```bash
$ export GITHUB_JOB="Build"
  export GITHUB_RUN_NUMBER="42"
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
    -d @build/reports/github-dependency-graph-plugin/github-dependency-snapshot.json
```

* [Gradle Best Practices](https://github.com/liutikas/gradle-best-practices)
* [Gradle Best Practices Plugin](https://github.com/autonomousapps/gradle-best-practices-plugin)
