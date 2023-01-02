package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.joecollins.models.general.social.generic.Hashtag
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tag(
    val name: String,
    val url: URL,
) : Hashtag {
    override val text: String = name
}
