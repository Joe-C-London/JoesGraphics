package com.joecollins.models.general.twitter

class Hashtag(val text: String) {

    companion object {
        fun fromV1(hashtagEntity: twitter4j.HashtagEntity): Hashtag {
            return Hashtag(hashtagEntity.text)
        }
    }
}
