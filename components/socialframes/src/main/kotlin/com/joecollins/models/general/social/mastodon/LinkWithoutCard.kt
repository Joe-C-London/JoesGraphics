package com.joecollins.models.general.social.mastodon

import com.joecollins.models.general.social.generic.Link
import java.net.URI
import java.net.URL

class LinkWithoutCard(val url: String) : Link {
    override val shortURL = url
    override val displayURL = url
    override val expandedURL = URI(url).toURL()
    override val preview = null
    override val isFromSocialNetwork = false
}
