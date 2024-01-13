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

### Misc

* [JFR Speedscope](https://github.com/parttimenerd/jfrtofp/blob/main/src/main/kotlin/me/bechberger/jfrtofp/other)
* [JFR Spring Starter](https://github.com/mirkosertic/flight-recorder-starter)
* [Kotlin Native Samples](https://github.com/JetBrains/kotlin/tree/master/kotlin-native/backend.native/tests/samples)
