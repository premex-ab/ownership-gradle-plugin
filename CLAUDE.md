# CLAUDE.md

## Project Overview

This is a Gradle plugin (`se.premex.ownership`) that verifies OWNERSHIP.toml files exist in Gradle projects with required information, and generates GitHub/Bitbucket CODEOWNERS files from them. Written in Kotlin, published to the Gradle Plugin Portal.

## Build & Test Commands

```bash
# Build everything (compile + test + detekt)
./gradlew build

# Run tests only
./gradlew test

# Run a single test class
./gradlew test --tests "se.premex.FixtureTest"

# Run detekt linting (auto-corrects issues)
./gradlew detekt

# Validate Gradle plugin configuration
./gradlew validatePlugins

# Run the full check suite (includes validateOwnership)
./gradlew check
```

## Project Structure

```
src/main/kotlin/se/premex/
├── OwnershipPlugin.kt          # Plugin entry point, registers tasks
├── OwnershipExtension.kt       # DSL configuration (ownership { ... })
├── ValidateOwnershipTask.kt    # Validates OWNERSHIP.toml files
├── GenerateOwnershipTask.kt    # Generates CODEOWNERS files
├── FileValidator.kt            # Validation rules engine
├── TomlParser.kt               # TOML parsing via Jackson
└── toml/                       # Data model classes
    ├── OwnershipFile.kt
    ├── Owner.kt
    └── Custom.kt

src/test/kotlin/se/premex/
├── FileValidatorTest.kt        # Unit tests for validation rules
├── ParseDslTest.kt             # DSL configuration tests
├── PathNormalizationTest.kt    # Path handling tests
├── FixtureTest.kt              # Integration tests (passing fixtures)
├── FailingFixtureTest.kt       # Integration tests (expected failures)
└── FixtureTaskTest.kt          # Task registration tests

src/test/fixtures/               # Gradle TestKit test projects
├── singlemodule/
├── multimodule/
├── singlemodule_multi_ownerships/
├── singlemodule_fail/
├── singlemodulewithoutowner_fail/
└── tomlparse_fail/
```

## Key Architecture

- **Plugin ID:** `se.premex.ownership`
- **Implementation class:** `se.premex.OwnershipPlugin`
- **Package:** `se.premex` (data models in `se.premex.toml`)

### Gradle Tasks Registered by Plugin

| Task | Scope | Description |
|------|-------|-------------|
| `validateOwnership` | All projects | Validates OWNERSHIP.toml files; attached to `check` |
| `generateOwnership` | Root project only | Generates CODEOWNERS files; depends on validateOwnership |
| `ownership` | All projects | Combined task (validate + generate on root, validate-only on subprojects) |

### Task Outputs

- Validation report: `build/reports/ownershipValidation/validation.json`
- Generated CODEOWNERS: `build/generated/ownershipValidation/gitownership` and `bitbucketownership`

## Tech Stack & Dependencies

- **Language:** Kotlin 2.3.x, JVM toolchain 17
- **Build:** Gradle 8.7 with version catalog (`gradle/libs.versions.toml`)
- **TOML parsing:** Jackson (`jackson-dataformat-toml`)
- **Serialization:** kotlinx-serialization-json
- **Testing:** JUnit 5 (Jupiter) + Google Truth + JSON-unit-assertj + Gradle TestKit
- **Linting:** Detekt with `autoCorrect = true` and `buildUponDefaultConfig = true`
- **Versioning:** `androidGitVersion` plugin from git tags (pattern `^v[0-9]+.*`)

## Code Conventions

- **Kotlin style:** Official (`kotlin.code.style=official` in gradle.properties)
- **Indentation:** 4 spaces
- **Line length:** Under 120 characters
- **String quotes:** Double quotes
- **Task classes:** Extend `DefaultTask`, annotated with `@CacheableTask`
- **Task inputs/outputs:** Use `@InputFiles`, `@OutputFile`, `@Input`, `@PathSensitive` annotations
- **Task actions:** Use `@TaskAction` annotation
- **Error handling:** Throw `GradleException` for validation failures
- **Extension DSL:** Properties in `OwnershipExtension` with `@Input` annotations

## Testing Patterns

- **Unit tests:** Standard JUnit 5 with `@Test` and `@ParameterizedTest`
- **Assertions:** Google Truth (`assertThat(...).isEqualTo(...)`) and JSON-unit (`assertThatJson(...)`)
- **Integration tests:** Gradle TestKit via `GradleRunner` with `--configuration-cache` and `--stacktrace`
- **Fixtures:** Each fixture in `src/test/fixtures/` has its own Gradle project with an `expected/` directory containing expected output files
- **Up-to-date checks:** Integration tests verify task caching works by running twice

## CI/CD

- **PR checks:** GitHub Actions runs `./gradlew check` and `./gradlew validatePlugins` on PRs to main
- **Releases:** Triggered on GitHub release publish; runs check then `./gradlew publishPlugins`
- **Java version in CI:** 17 (adopt distribution)
- **Dependabot:** Configured for Gradle dependencies and GitHub Actions (daily updates)
