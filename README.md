# Gradle code ownership verification plugin

A gradle plugin that will verify ownership files are in place in projects and have required information.

##### Use it

```kotlin
plugins {
    id("se.premex.ownership") version "0.0.1"
}
```
##### Configure it

```kotlin
ownership {
    validateOwnership = true
    generateGithubOwners = true
    generateBitbucketOwners = true
}
```

##### Verify ownership files is in place:

```shell script
./gradlew validateOwnership
```
Outputs from validation is available in `build/reports/ownershipValidation/`



##### `validateOwnership` will be run as part of `check` tasks

```shell script
./gradlew check
```

### Sample OWNERSHIP.toml file

```toml
# version is always required 
version = 1

[owner]
user = "secretUser"
users = ["secretUser", "anotherUser"]
group = "secretGroup"
groups = [ "secretGroup", "anotherGroup" ]
```

## Coming Soon!

Here are some ideas that we are thinking about. We are also not limited to these and would love to learn more about your
use cases.

- [x] Support for verifying codeownership files is in place
- [ ] Better rules for validating content in ownership files
- [x] Support for having codeownership files anywhere in the code and not only in a projects root
- [x] Generate GitHub & Bitbucket codeownership files based on the toml files
- [ ] Generate html reports of code ownerships and code health 
