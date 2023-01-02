package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.joecollins.models.general.social.generic.Link
import java.awt.Image
import java.net.URL
import javax.imageio.ImageIO

@JsonIgnoreProperties(ignoreUnknown = true)
data class Card(
    val url: URL,
    override val title: String,
    val description: String,
    @JsonProperty("image") private val imageURL: URL,
    @JsonProperty("provider_name") val providerName: String,
    val type: String,
) : Link {

    override val image: Image = ImageIO.read(imageURL)
    override val shortURL: URL = url
    override val expandedURL: URL = url
    override val displayURL: String = url.toString()
    override val domain: String = providerName
    override val isFromSocialNetwork: Boolean = false
}
