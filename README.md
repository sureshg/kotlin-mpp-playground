🎨 Kotlin Multiplatform Playground!
----------

[![GitHub Workflow Status][gha_badge]][gha_url]
[![OpenJDK Version][java_img]][java_url]
[![Kotlin release][kt_img]][kt_url]
[![Maven Central Version][maven_img]][maven_url]
[![Ktor][ktor_img]][ktor_url]
[![Style guide][ktfmt_img]][ktfmt_url]

This repo shows a Gradle multi-project build structure that uses the [Kotlin Multiplatform][Kotlin Multiplatform] to
build a [JVM][Kotlin-JVM], [JS][Kotlin-JS], [Desktop][Compose-Multiplatform]
and [Compose Web (wasm)][Compose-Multiplatform] applications.

### Install OpenJDK EA Build

```bash
$ curl -s "https://get.sdkman.io" | bash
$ sdk i java 26.ea-open
```

### Build & Run

```bash
$ ./gradlew build [-Pskip.test]
$ backend/jvm/build/libs/jvm

# Publish to local repo
$ ./gradlew buildAndPublish
```

### Publishing

Push a new tag to trigger the release workflow and publish the artifacts. That's it 🎉.
The next version will be based on the semantic version scope (`major`, `minor`, `patch`)

   ```bash
   $ ./gradlew pushSemverTag "-Psemver.scope=patch"

   # To see the current version
   # ./gradlew v

   # Print the new version
   # ./gradlew printSemver "-Psemver.scope=patch"
   ```

<details>
<summary> <b>Multiplatform Targets</b></summary>

### JVM

* Build and Run

  ```bash
  # Kotlin Multiplatform
  $ ./gradlew :shared:runJvm
  $ ./gradlew :shared:jvmDistZip
  # Run task for target 'jvm' and compilation 'main' (it's confusing)
  $ ./gradlew :shared:jvmRun

  # Kotlin JVM
  $ ./gradlew :backend:jvm:run
  $ ./gradlew :backend:jvm:build
  $ ./gradlew :backend:jvm:jdeprscan
  $ ./gradlew :backend:jvm:printModuleDeps


  # Benchmark
  $ ./gradlew :benchmark:benchmark
  ```
* GraalVM Native Image

  ```bash
  $ sdk u java graalvm-ce-dev
  $ ./gradlew :backend:jvm:nativeCompile
  $ backend/jvm/build/native/nativeCompile/jvm

  # To generate the native image configurations
  $ ./gradlew :backend:jvm:run -Pagent
  $ curl http://localhost:8080/shutdown
  $ ./gradlew :backend:jvm:metadataCopy

  ```

* Containers

  ```bash
  # Running app on container
  $ docker run \
           -it \
           --rm \
           --pull always \
           --workdir /app \
           --publish 8080:8080 \
           --publish 8081:8081 \
           --name kotlin-mpp-playground \
           --mount type=bind,source=$(pwd),destination=/app,readonly \
           openjdk:26-slim /bin/bash -c "printenv && backend/jvm/build/libs/jvm"

   # Build a container image and run
   $ ./gradlew :backend:jvm:jibDockerBuild --no-configuration-cache
   $ docker run -it --rm --name jvm -p 8080:8080 -p 9898:9898 sureshg/jvm
   $ docker stats
  ```

* OpenTelemetry

  ```bash
   # Run otel tui
   $ brew install ymtdzzz/tap/otel-tui
   $ otel-tui

   # or run hyperdx
   $ docker run \
            -it --rm \
            -p 8081:8080 \
            -p 8123:8123 \
            -p 4317:4317 \
            -p 4318:4318 \
            --name hyperdx \
            --ulimit nofile=262144:262144 \
             hyperdx/hyperdx-local:2-beta
   $ open http://localhost:8081/search

   # Run the app
   $ docker run -it --rm \
                --name jvm \
                -p 8080:8080 \
                -p 9898:9898 \
                sureshg/jvm:latest
   $ curl -v -X GET http://localhost:8080/trace

   # Change/Reset log level
   $ curl -v -X POST http://localhost:8080/loglevel/dev.suresh.http/debug
   $ curl -v -X POST http://localhost:8080/loglevel/reset
  ```

* JVM Agents

  ```bash
  # Normal agent with Launcher-Agent-Class
  $ ./gradlew :backend:agent:jfr:build
  $ backend/agent/jfr/build/libs/jfr

  # Custom OpenTelemetry agent
  $ ./gradlew :backend:agent:otel:build
  ```

* AOT Cache

  ```bash
  # Training Run to create AOT cache
  $ java --enable-preview \
         -XX:+UseZGC \
         -XX:+UseCompactObjectHeaders \
         -XX:AOTCacheOutput=app.aot \
         -jar backend/jvm/build/libs/jvm-all.jar

  # Run with AOT
  $ java --enable-preview \
         -XX:+UseZGC \
         -XX:+UseCompactObjectHeaders \
         -XX:AOTCache=app.aot \
         -jar backend/jvm/build/libs/jvm-all.jar

  # Show native memory details
  $ jcmd jvm System.map
  ```

* Tests

  ```bash
  $ ./gradlew :backend:jvm:test -PktorTest
  $ ./gradlew :backend:jvm:test -Pk8sTest
  $ ./gradlew :backend:jvm:jvmRun -DmainClass=dev.suresh.lang.SysCallKt --quiet
  ```

* BinCompat & Missing Targets

  ```bash
  $ ./gradlew :backend:security:apiDump
  $ ./gradlew :backend:security:apiCheck

  # KMP missing targets report
  $ ./gradlew :shared:kmpMissingTargets
  $ open shared/build/reports/kmp-missing-targets.md
  ```

### Wasm/JS

  ```bash
  $ ./gradlew :web:jsBrowserProductionRun -t
  $ ./gradlew :web:wasmJsBrowserProductionRun -t
  $ ./gradlew kotlinUpgradePackageLock

  # Kobweb
  $ kobweb run -p compose/web
  $ ./gradlew :compose:html:kobwebStart -t
  $ ./gradlew :compose:html:kobwebStop
  ```

### Native

  ```bash
  $ ./gradlew :backend:native:build
  $ find backend/native/build/bin -type f -perm +111 -exec ls -lh {} \; | awk '{print $9 ": " $5}'

  # Arch specific binaries
  $ ./gradlew :backend:native:macosArm64Binaries
  $ ./gradlew :backend:native:macosX64Binaries
  $ ./gradlew :backend:native:macOsUniversalBinary

  # Native container image
  $ ./gradlew :backend:native:jibDockerBuild --no-configuration-cache
  $ docker run -it --rm --name native sureshg/native

  # Debug distroless image
  # docker run -it --entrypoint=sh gcr.io/distroless/cc-debian12:debug

  # Test linux binary on ARM64 MacOS
  $ ./gradlew :backend:native:linuxArm64Binaries
  $ docker run  \
           -it \
           --rm \
           --publish 8080:80 \
           --mount type=bind,source=$(pwd),destination=/app,readonly \
           debian:stable-slim
    # /app/backend/native/build/bin/linuxArm64/releaseExecutable/native.kexe
    # libtree -v /app/backend/native/build/bin/linuxArm64/releaseExecutable/native.kexe

  # Build native binaries on container
  $ docker run \
           --platform=linux/amd64 \
           -it \
           --rm \
           --pull always \
           --workdir /app \
           --name kotlin-native-build \
           --mount type=bind,source=$(pwd),destination=/app \
           --mount type=bind,source=${HOME}/.gradle,destination=/root/.gradle \
           openjdk:26-slim /bin/bash
  # apt update && apt install libtree tree
  # ./gradlew --no-daemon :backend:native:build
  #  backend/native/build/bin/linuxX64/releaseExecutable/native.kexe
  ```

### Compose

  ```bash
  # Compose Desktop
  $ ./gradlew :compose:cmp:runDistributable
  $ ./gradlew :compose:cmp:packageDistributionForCurrentOS
  $ ./gradlew :compose:cmp:packageReleaseUberJarForCurrentOS
  $ ./gradlew :compose:cmp:suggestModules

  # Hot Reload
  $ ./gradlew :compose:cmp:jvmRunHot --mainClass=MainKt [--auto]

  # Compose Web
  $ ./gradlew :compose:cmp:wasmJsBrowserProductionRun -t

  # Compose multiplatform tests
  $ ./gradlew :compose:cmp:allTests
  $ ./gradlew :compose:cmp:jvmTest
  ```

### Publishing

  ```bash
  $ ./gradlew publishAllPublicationsToLocalRepository

  # Publishing to all repo except Maven Central
  $ ./gradlew buildAndPublish

  # Maven Central Publishing
  # https://central.sonatype.org/publish/publish-portal-gradle/#alternatives
  # https://vanniktech.github.io/gradle-maven-publish-plugin/central/#in-memory-gpg-key
  $ gpg --export-secret-keys --armor XXXXXXXX | grep -v '\-\-' | grep -v '^=.' | tr -d '\n'
  # OR
  $ gpg --export-secret-keys --armor XXXXXXXX | awk 'NR == 1 { print "SIGNING_KEY=" } 1' ORS='\\n'

  $ export ORG_GRADLE_PROJECT_mavenCentralUsername=<Username from https://central.sonatype.com/account>
  $ export ORG_GRADLE_PROJECT_mavenCentralPassword=<Token from https://central.sonatype.com/account>
  $ export ORG_GRADLE_PROJECT_signingInMemoryKeyId=<GPG Key ID>
  $ export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=<Password>
  $ export ORG_GRADLE_PROJECT_signingInMemoryKey=$(gpg --export-secret-keys --armor ${ORG_GRADLE_PROJECT_signingInMemoryKeyId} | grep -v '\-\-' | grep -v '^=.' | tr -d '\n')

  # For aggregated publication (preferred) to Central
  $ ./gradlew publishAggregationToCentralPortal

  # For all publications (separate publications)
  $ ./gradlew publishAllPublicationsToCentralPortal
  ```

### Misc

  ```bash
  # Dependency Insight
  $ ./gradlew dependencies
  $ ./gradlew :shared:dependencies --configuration testRuntimeClasspath

  $ ./gradlew -q :build-logic:dependencyInsight --dependency kotlin-compiler-embeddable --configuration RuntimeClasspath
  $ ./gradlew -q :shared:dependencyInsight --dependency slf4j-api --configuration RuntimeClasspath

  $ ./gradlew :backend:jvm:listResolvedArtifacts

  # KMP hierarchy and module graphs
  $ ./gradlew :shared:printHierarchy
  $ ./gradlew createModuleGraph
  $ ./gradlew generateChangelog

  # Clean
  $ ./gradlew cleanAll

  # Gradle Toolchains
  $ ./gradlew buildEnvironment
  $ ./gradlew updateDaemonJvm
  $ ./gradlew javaToolchains
  $ ./gradlew wrapper --gradle-version=x.x.x

  # Gradle Best Practices
  $ ./gradlew -p gradle/build-logic :bestPracticesBaseline
  $ ./gradlew checkBuildLogicBestPractices

  # GitHub Actions lint
  $ actionlint
  ```

</details>

### Deployed App and Docs

- [Web App](https://suresh.dev/kotlin-mpp-playground/app/)
- [Docs](https://suresh.dev/kotlin-mpp-playground/docs)
- [Coverage](https://suresh.dev/kotlin-mpp-playground/reports)
- [Tests](https://suresh.dev/kotlin-mpp-playground/tests)

### Verifying Artifacts

The published artifacts are signed using this [key][signing_key]. The best way to verify artifacts
is [automatically with Gradle][gradle_verification].

[gradle_verification]: https://docs.gradle.org/current/userguide/dependency_verification.html#sec:signature-verification

[signing_key]: https://keyserver.ubuntu.com/pks/lookup?op=get&search=0xc124db3a8ad1c13f7153decdf209c085c8b53ca1

### Resources

- [🔍 Kotlin Multiplatform Package Search](https://package-search.jetbrains.com/search?query=ktor&onlyMpp=true)
- [🎨 Kotlin Multiplatform Wizard](https://kmp.jetbrains.com/)
- [📏 Jetbrains Compose Rules](https://mrmans0n.github.io/compose-rules/rules/)

<!-- Badges -->

[java_url]: https://jdk.java.net/26/

[java_img]: https://img.shields.io/badge/OpenJDK-26-e76f00?logo=openjdk&logoColor=e76f00

[kt_url]: https://github.com/JetBrains/kotlin/releases/latest

[kt_img]: https://img.shields.io/github/v/release/Jetbrains/kotlin?include_prereleases&color=7f53ff&label=Kotlin&logo=kotlin&logoColor=7f53ff

[maven_img]: https://img.shields.io/maven-central/v/dev.suresh.kmp/shared?logo=apachemaven&logoColor=cd2237&color=cd2237

[maven_url]: https://central.sonatype.com/search?q=dev.suresh.kmp&namespace=dev.suresh.kmp

[maven_dl]: https://search.maven.org/remote_content?g=dev.suresh.kmp&a=shared&v=LATEST

[gha_url]: https://github.com/sureshg/kotlin-mpp-playground/actions/workflows/build.yml

[gha_badge]: https://img.shields.io/github/actions/workflow/status/sureshg/kotlin-mpp-playground/build.yml?branch=main&color=green&label=Build&logo=Github-Actions&logoColor=green

[sty_url]: https://kotlinlang.org/docs/coding-conventions.html

[sty_img]: https://img.shields.io/badge/style-Kotlin--Official-40c4ff.svg?style=for-the-badge&logo=kotlin&logoColor=40c4ff

[ktfmt_url]: https://github.com/facebookincubator/ktfmt#ktfmt

[ktfmt_img]: https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg?logo=kotlin&logoColor=FF4081

[cmp_url]: https://github.com/JetBrains/compose-multiplatform/releases

[cmp_img]: https://img.shields.io/github/v/release/JetBrains/compose-multiplatform?color=3cdc84&label=Compose%20MP&logo=JetpackCompose&logoColor=3cdc84

[ktor_url]: https://search.maven.org/artifact/io.ktor/ktor-bom

[ktor_download]: https://search.maven.org/remote_content?g=io.ktor&a=ktor-client&v=LATEST

[ktor_img]: https://img.shields.io/maven-central/v/io.ktor/ktor-bom?color=4a79fe&label=Ktor&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZlcnNpb249IjEuMSIgdmlld0JveD0iMCAwIDE2IDE2IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxkZWZzPgogICAgPHN0eWxlPi5he2ZpbGw6bm9uZTt9LmJ7Y2xpcC1wYXRoOnVybCgjYSk7fS5je2ZpbGw6I2ZmZjt9PC9zdHlsZT4KICAgIDxjbGlwUGF0aCBpZD0iYSI+CiAgICAgIDxyZWN0IGNsYXNzPSJhIiB4PSIxNC43IiB5PSIxMSIgd2lkdGg9IjE3MSIgaGVpZ2h0PSIxNTEiLz4KICAgIDwvY2xpcFBhdGg+CiAgICA8Y2xpcFBhdGggaWQ9ImNsaXBQYXRoMTMiPgogICAgICA8cmVjdCBjbGFzcz0iYSIgeD0iMTQuNyIgeT0iMTEiIHdpZHRoPSIxNzEiIGhlaWdodD0iMTUxIi8+CiAgICA8L2NsaXBQYXRoPgogIDwvZGVmcz4KICA8cGF0aCBjbGFzcz0iYyIgdHJhbnNmb3JtPSJtYXRyaXgoLjE2NCAwIDAgLjE2NCAtOC4zNyAtMS44MSkiIGQ9Im0xMDAgMTEtNDIuMyAyNC40djQ4LjlsNDIuMyAyNC40IDQyLjMtMjQuNHYtNDguOXptMzAuMiA2Ni4zLTMwLjIgMTcuNC0zMC4yLTE3LjR2LTM0LjlsMzAuMi0xNy40IDMwLjIgMTcuNHoiIGNsaXAtcGF0aD0idXJsKCNjbGlwUGF0aDEzKSIvPgo8L3N2Zz4K

[Kotlin-JVM]: https://kotlinlang.org/docs/jvm-get-started.html

[Kotlin-JS]: https://kotlinlang.org/docs/js-project-setup.html

[Kotlin Multiplatform]: https://kotlinlang.org/docs/multiplatform.html

[Compose-Multiplatform]: https://github.com/JetBrains/compose-multiplatform

[Kotlin Multiplatform DSL]: https://kotlinlang.org/docs/multiplatform-dsl-reference.html

[simple-icons-logo]: https://simpleicons.org/icons/kotlin.svg


<details>
<summary>Module Dependency Graph</summary>

### Module Dependency

```mermaid
%%{
  init: {
    'theme': 'neutral'
  }
}%%
graph LR
    subgraph :backend
        :backend:native["native"]
        :backend:data["data"]
        :backend:profiling["profiling"]
        :backend:jvm["jvm"]
        :backend:security["security"]
    end
    subgraph :compose
        :compose:desktop["desktop"]
        :compose:html["html"]
    end
    subgraph :dep-mgmt
        :dep-mgmt:bom["bom"]
        :dep-mgmt:catalog["catalog"]
    end
    subgraph :meta
        :meta:compiler["compiler"]
        :meta:ksp["ksp"]
    end
    subgraph :meta:compiler
        :meta:compiler:plugin["plugin"]
    end
    subgraph :meta:ksp
        :meta:ksp:processor["processor"]
    end
    subgraph :web
        :web:js["js"]
        :web:wasm["wasm"]
    end
    :web:js --> :shared
    :benchmark --> :shared
    :backend:native --> :shared
    :web:wasm --> :shared
    :compose:desktop --> :shared
    :meta:compiler:plugin --> :shared
    :meta:ksp:processor --> :shared
    :backend:data --> :shared
    :backend:profiling --> :shared
    :compose:html --> :shared
    : --> :backend
    : --> :benchmark
    : --> :compose
    : --> :meta
    : --> :shared
    : --> :web
    : --> :backend:data
    : --> :backend:jvm
    : --> :backend:native
    : --> :backend:profiling
    : --> :backend:security
    : --> :compose:desktop
    : --> :compose:html
    : --> :meta:compiler
    : --> :meta:ksp
    : --> :web:js
    : --> :web:wasm
    : --> :meta:compiler:plugin
    : --> :meta:ksp:processor
    : --> :dep-mgmt:bom
    : --> :dep-mgmt:catalog
    :backend:jvm --> :shared
    :backend:jvm --> :backend:data
    :backend:jvm --> :backend:profiling
    :backend:jvm --> :web:js
    :backend:jvm --> :web:wasm
    :backend:security --> :shared
```

</details>
