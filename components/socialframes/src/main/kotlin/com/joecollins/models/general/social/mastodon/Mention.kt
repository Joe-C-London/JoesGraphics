package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.joecollins.models.general.social.generic.UserMention
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class Mention(
    private val username: String,
    private val url: URL,
    private val acct: String,
) : UserMention {

    override val text: String = "@$username"

    override val display: String = "@$username@${url.host}"
}
