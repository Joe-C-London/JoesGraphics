package com.joecollins.models.general.social.generic

import java.awt.Image
import java.net.URL

interface Link {
    val shortURL: URL
    val expandedURL: URL
    val displayURL: String
    val image: Image?
    val title: String
    val domain: String

    val isFromSocialNetwork: Boolean
}
