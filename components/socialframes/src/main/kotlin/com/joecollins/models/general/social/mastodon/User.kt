package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.joecollins.models.general.social.generic.User
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    private val username: String,
    private val acct: String,
    @JsonProperty("display_name") private val displayName: String,
    private val url: URL,
    private val avatar: URL,
) : User {

    override val screenName: String = if (username.isEmpty()) "" else "@$username@${url.host}"

    override val name: String = displayName

    override val profileImageURL: URL = avatar

    override val isVerified: Boolean = false

    override val isProtected: Boolean = false
}
