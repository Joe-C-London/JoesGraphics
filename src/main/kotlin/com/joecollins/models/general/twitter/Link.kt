package com.joecollins.models.general.twitter

import java.net.URL

class Link(val shortURL: URL, val expandedURL: URL, val displayURL: String) {

    companion object {
        fun fromV1(urlEntity: twitter4j.URLEntity): Link {
            return Link(
                URL(urlEntity.url),
                URL(urlEntity.expandedURL),
                urlEntity.displayURL
            )
        }
    }
}
