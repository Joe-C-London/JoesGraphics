package com.joecollins.models.general.social.twitter

class Hashtag(override val text: String) : com.joecollins.models.general.social.generic.Hashtag {

    companion object {
        fun fromV1(hashtagEntity: twitter4j.HashtagEntity): Hashtag = Hashtag(hashtagEntity.text)

        fun fromV2(hashtagEntity: com.twitter.clientlib.model.HashtagEntity): Hashtag = Hashtag(hashtagEntity.tag)
    }
}
