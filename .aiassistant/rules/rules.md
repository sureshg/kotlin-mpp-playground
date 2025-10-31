---
apply: always
---

Kotlin Multiplatform Project - AI Instructions

## Tech Stack

- **Kotlin**: 2.3.0-Beta2 (use latest features)
- **Build System**: Amper (latest dev version)
- **Java Target**: 25
- **Platform**: Kotlin Multiplatform

## Core Libraries

- `kotlinx-coroutines` - Asynchronous programming
- `kotlinx-serialization` - JSON/XML serialization
- `kotlinx-datetime` - Date/time handling
- `kotlinx-io` - I/O operations
- `ktor-client` - HTTP client
- `io.github.oshai:kotlin-logging` - Logging
- `dev.whyoleg.cryptography:cryptography-core` - Cryptography

**Important**:

- Always check `libs.versions.toml` for existing library versions before adding or updating dependencies.
- Use the latest stable versions of core libraries when adding new dependencies.

## Code Standards

### Style & Quality

- Write simple, clean, **idiomatic, and concise** Kotlin code
- Avoid verbose or overly complex solutions
- Use Kotlin stdlib functions before third-party libraries
- Follow official Kotlin coding conventions
- Prefer functional style where appropriate
- Avoid excessive scope function nesting (`.let{}`, `.apply{}`, etc.)

### Kotlin Multiplatform

- Write common code compatible with all JVM and Native targets
- Use `expect`/`actual` declarations only when platform-specific code is required
- Prefer common Kotlin libraries over platform-specific ones

### Code Conversion from Other Languages

- **Never** translate code line-by-line from other languages
- Rewrite code idiomatically using Kotlin's features and conventions
- Replace loops with stdlib functions (`map`, `filter`, `fold`, etc.)
- Use Kotlin's null safety, data classes, and extension functions
- Simplify verbose patterns with concise Kotlin equivalents
- Leverage stdlib before adding dependencies

### Requirements

- Production-ready code only (no bugs, placeholders, or TODOs)
- Do not introduce bugs to existing code
- Use modern Kotlin features from the latest available version
- Add **concise, clean, idiomatic** error handling
- Include KDoc comments for public APIs
- Use meaningful, **concise** variable names

### Research & References

- Check well-maintained GitHub projects for implementation patterns
- Verify library usage against official documentation

## Documentation Links

- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Kotlinx I/O](https://github.com/Kotlin/kotlinx-io)
- [Amper Build System](https://github.com/JetBrains/amper)
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)
- [Java 25 API](https://docs.oracle.com/en/java/javase/25/docs/api/index.html)