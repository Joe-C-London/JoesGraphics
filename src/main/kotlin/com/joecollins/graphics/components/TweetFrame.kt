package com.joecollins.graphics.components

import com.joecollins.models.general.social.twitter.Tweet
import com.joecollins.models.general.social.twitter.TweetLoader
import com.joecollins.pubsub.map
import java.awt.Color
import java.time.ZoneId
import java.util.concurrent.Flow

class TweetFrame(tweet: Flow.Publisher<out Tweet>, private val timezone: ZoneId = ZoneId.systemDefault()) : SocialMediaFrame(tweet, timezone) {
    override val color
        get() = Color(0x00acee)

    override val emojiVersion: String
        get() = "twitter/v14.0"

    override val protectedUserText: String
        get() = "This user's tweets are protected, and this tweet has therefore been blocked from this frame."

    companion object {
        fun createTweetFrame(tweetId: Flow.Publisher<out Long>): TweetFrame {
            return TweetFrame(tweetId.map { TweetLoader.loadTweetV2(it) })
        }
    }
}
