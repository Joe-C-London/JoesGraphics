package com.joecollins.models.general.social.twitter

import java.net.URI
import java.net.URL

class Media(
    override val mediaURL: URL,
    override val displayURL: String,
) : com.joecollins.models.general.social.generic.Media {

    companion object {
        fun fromV1(mediaEntity: twitter4j.MediaEntity): Media = Media(URI(mediaEntity.mediaURL).toURL(), mediaEntity.displayURL)

        fun fromV2(urlEntity: com.twitter.clientlib.model.UrlEntity, media: Map<String, URL>): Media? {
            if (urlEntity.mediaKey == null) return null
            if (!media.containsKey(urlEntity.mediaKey)) return null
            return Media(
                media[urlEntity.mediaKey]!!,
                urlEntity.url.toString(),
            )
        }
    }
}
