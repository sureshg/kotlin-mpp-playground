### Build Logic of the projects

#### Run the `Gradle Best Practices` plugin on the build logic:

```bash
$ ./gradlew -p gradle/build-logic :bestPracticesBaseline
$ ./gradlew checkBuildLogicBestPractices
# ./gradlew -p gradle/build-logic checkBestPractices
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
