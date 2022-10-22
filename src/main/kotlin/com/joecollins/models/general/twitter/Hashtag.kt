package com.joecollins.models.general.twitter

import twitter4j.HashtagEntity

class Hashtag(val text: String) {

    companion object {
        fun fromV1(hashtagEntity: twitter4j.HashtagEntity): Hashtag {
            return Hashtag(hashtagEntity.text)
        }

        fun fromV2(hashtagEntity: com.twitter.clientlib.model.HashtagEntity): Hashtag {
            return Hashtag(hashtagEntity.tag)
        }
    }
}
