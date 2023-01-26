package com.joecollins.models.general.social.mastodon

import com.joecollins.models.general.social.generic.Link
import java.net.URL

class LinkWithoutCard(val url: String) : Link {
    override val shortURL = URL(url)
    override val displayURL = url
    override val expandedURL = URL(url)
    override val preview = null
    override val isFromSocialNetwork = false
}
