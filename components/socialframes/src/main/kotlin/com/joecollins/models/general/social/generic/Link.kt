package com.joecollins.models.general.social.generic

import java.awt.Image
import java.net.URL

interface Link {
    val shortURL: String
    val expandedURL: URL
    val displayURL: String
    val preview: Preview?

    val isFromSocialNetwork: Boolean

    interface Preview {
        val image: Image?
        val title: String
        val domain: String
    }
}
