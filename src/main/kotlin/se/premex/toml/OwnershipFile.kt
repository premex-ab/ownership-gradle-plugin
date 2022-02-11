package se.premex.toml

@kotlinx.serialization.Serializable
class OwnershipFile {
    var version: Long? = null
    var owner: Owner? = null
    var custom: Custom? = null
}

data class OwnershipFileResult(
    val exception: Exception?,
    val ownershipFile: OwnershipFile?,
)
