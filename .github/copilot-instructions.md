# GitHub Copilot Instructions for ownership-gradle-plugin

## Project Overview
This is a Gradle plugin that verifies ownership files (OWNERSHIP.toml) are in place in projects and contain required information. It also generates GitHub and Bitbucket code ownership files.

## Language and Style
- Use Kotlin for all code changes
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Prefer Kotlin idioms and features (e.g., data classes, extension functions, when expressions)
- Use explicit type declarations when it improves code clarity

## Code Style
- Use 4 spaces for indentation
- Use double quotes for strings
- Follow existing code style and formatting patterns in the repository
- Keep lines under 120 characters when possible
- Use trailing commas in multi-line parameter lists

## Build and Testing
- Use Gradle 8.7 for building the project
- Run `./gradlew build` to build the plugin
- Run `./gradlew test` to execute tests
- The project uses JUnit 5 (Jupiter) for testing
- Use Google Truth library for assertions in tests (e.g., `assertThat(...).isEqualTo(...)`)
- Write tests for all new functionality
- Test files should mirror the structure of source files

## Plugin Development
- This is a Gradle plugin project using the `java-gradle-plugin` plugin
- Main plugin class: `se.premex.OwnershipPlugin`
- Plugin ID: `se.premex.ownership`
- All tasks should extend Gradle's `DefaultTask`
- Use Gradle's configuration API properly (e.g., `target.afterEvaluate`)
- Leverage Gradle TestKit for integration testing

## Dependencies
- Jackson for TOML parsing (`jackson-dataformat-toml`)
- Kotlinx serialization for JSON handling
- Detekt for code quality checks with auto-correct enabled
- Never add dependencies without checking for security vulnerabilities first

## Linting and Code Quality
- The project uses Detekt for Kotlin code quality checks
- Run `./gradlew detekt` before committing changes
- Detekt is configured with `autoCorrect = true` and `buildUponDefaultConfig = true`
- Fix any Detekt warnings that are introduced by your changes

## Documentation
- Update README.md if adding new features or changing behavior
- Use KDoc comments for public APIs and complex functions
- Include usage examples in documentation
- Keep inline comments minimal and focused on explaining "why" not "what"

## Security
- Never commit secrets, API keys, or credentials
- Validate all file paths and user inputs
- Be cautious with file system operations
- Use secure defaults for all configurations

## TOML File Validation
- The plugin validates OWNERSHIP.toml files
- Current version supported: version = 1
- Required fields: `[owner]` section with `user` field
- File validation logic is in `FileValidator.kt`
- TOML parsing is handled by `TomlParser.kt`

## Git and Version Control
- Keep commits small and focused
- Write clear, descriptive commit messages
- Don't include build artifacts or generated files in commits
- The project uses `androidGitVersion` plugin for versioning based on git tags

## Task Naming
- Use camelCase for task names (e.g., `validateOwnership`, `generateOwnership`)
- Group tasks under "Ownership" group
- Provide clear descriptions for all tasks

## Common Patterns
- Extension DSL is defined in `OwnershipExtension.kt`
- Task implementations are in separate files (e.g., `ValidateOwnershipTask.kt`)
- TOML data classes are in the `se.premex.toml` package
- Use Gradle's lazy configuration where appropriate
