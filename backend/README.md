### Run the backend JVM app

```bash
# JVM
$ ./gradlew :backend:jvm:run

# Native
$ ./gradlew :backend:native:runDebugExecutable
$ ./gradlew :backend:native:linkReleaseExecutable

# Container
$ ./gradlew :backend:native:jibDockerBuild
$ docker run --rm -it sureshg/native
```
