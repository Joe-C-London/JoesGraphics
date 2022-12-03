package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.joecollins.models.general.social.generic.Media
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class MediaAttachment(
    val type: String,
    val url: URL
) : Media {
    override val mediaURL: URL = url
    override val displayURL: String? = null
}
