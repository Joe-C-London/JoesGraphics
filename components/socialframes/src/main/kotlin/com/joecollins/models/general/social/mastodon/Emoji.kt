package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class Emoji(
    val shortcode: String,
    val url: URL,
    @JsonProperty("static_url") val staticUrl: URL,
)
