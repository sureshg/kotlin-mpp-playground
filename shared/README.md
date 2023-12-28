### Run shared JVM app

```bash
$ ./gradlew :shared:run
# or
$ ./gradlew build
$ java --enable-preview \
       --add-modules=ALL-SYSTEM \
       -jar shared/build/libs/common-*-all.jar
```
