package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.joecollins.models.general.social.generic.Emoji
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class Emoji(
    val shortcode: String,
    override val url: URL,
    @JsonProperty("static_url") val staticUrl: URL,
) : Emoji {
    override val text = ":$shortcode:"
}
