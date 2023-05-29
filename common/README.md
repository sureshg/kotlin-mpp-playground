### Run common JVM app

```bash
$ ./gradlew :common:run
# or
$ ./gradlew build
$ java --enable-preview \
       --add-modules=ALL-SYSTEM \
       -jar common/build/libs/common-*-all.jar
```
