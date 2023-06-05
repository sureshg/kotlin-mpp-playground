Kotlin Multiplatform Playground!
----------

[![GitHub Workflow Status][gha_badge]][gha_url]
[![OpenJDK Version][java_img]][java_url]
[![Kotlin release][kt_img]][kt_url]
[![Style guide][ktfmt_img]][ktfmt_url]

### Install OpenJDK 21

```bash
# Mac OS
$ curl -s "https://get.sdkman.io" | bash
$ sdk i java 21.ea.25-open
$ sdk u java 21.ea.25-open
```

### Build & Run

```bash
$ ./gradlew build
```

<!-- Badges -->

[java_url]: https://www.azul.com/downloads/?version=java-21-ea&package=jdk#zulu

[java_img]: https://img.shields.io/badge/OpenJDK-21-ea791d?logo=java&style=for-the-badge&logoColor=ea791d

[kt_url]: https://github.com/JetBrains/kotlin/releases/latest

[kt_img]: https://img.shields.io/github/v/release/Jetbrains/kotlin?include_prereleases&color=7f53ff&label=Kotlin&logo=kotlin&logoColor=7f53ff&style=for-the-badge

[gha_url]: https://github.com/sureshg/kotlin-mpp-playground/actions/workflows/build.yml

[gha_badge]: https://img.shields.io/github/actions/workflow/status/sureshg/kotlin-mpp-playground/build.yml?branch=main&color=green&label=Build&logo=Github-Actions&logoColor=green&style=for-the-badge

[sty_url]: https://kotlinlang.org/docs/coding-conventions.html

[sty_img]: https://img.shields.io/badge/style-Kotlin--Official-40c4ff.svg?style=for-the-badge&logo=kotlin&logoColor=40c4ff

[ktfmt_url]: https://github.com/facebookincubator/ktfmt#ktfmt

[ktfmt_img]: https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg?logo=kotlin&style=for-the-badge&logoColor=FF4081

[Kotlin Multiplatform DSL]: https://kotlinlang.org/docs/multiplatform-dsl-reference.html
