package com.joecollins.models.general.twitter

import java.net.URL

class Media(val mediaURL: URL, val displayURL: String) {

    companion object {
        fun fromV1(mediaEntity: twitter4j.MediaEntity): Media {
            return Media(URL(mediaEntity.mediaURL), mediaEntity.displayURL)
        }
    }
}
