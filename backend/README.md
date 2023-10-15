### Run the backend JVM app

```bash
# JVM
$ ./gradlew :backend:jvm:run

# Native
$ ./gradlew :backend:native:macOsUniversalBinary

# Container
$ ./gradlew :backend:native:jibDockerBuild
$ docker run -it --rm sureshg/native
```

More Kotlin Native samples can be found
in [Kotlin GitHub Repo](https://github.com/JetBrains/kotlin/tree/master/kotlin-native/backend.native/tests/samples)
