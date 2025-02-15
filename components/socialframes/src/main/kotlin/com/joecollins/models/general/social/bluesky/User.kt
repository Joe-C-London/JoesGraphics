package com.joecollins.models.general.social.bluesky

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    private val handle: String,
    private val displayName: String,
    private val avatar: URL,
) : com.joecollins.models.general.social.generic.User {
    override val screenName: String = if (handle.isBlank()) "" else "@$handle"
    override val name: String = displayName.takeUnless { it.isBlank() } ?: handle
    override val profileImageURL: URL = avatar
    override val isVerified: Boolean = false
    override val isProtected: Boolean = false
}
