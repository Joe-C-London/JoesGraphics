package com.joecollins.graphics.components

import com.joecollins.models.general.social.twitter.Tweet
import com.joecollins.models.general.social.twitter.TweetLoader
import com.joecollins.pubsub.map
import org.apache.batik.parser.AWTPathProducer
import org.apache.batik.parser.PathParser
import java.awt.Color
import java.awt.Shape
import java.time.ZoneId
import java.util.concurrent.Flow

class TweetFrame internal constructor(
    tweet: Flow.Publisher<out Tweet>,
    timezone: ZoneId = ZoneId.systemDefault(),
) : SocialMediaFrame<Tweet>(tweet, Color(0x00acee), timezone) {

    override val emojiVersion: String
        get() = "twitter/v14.0"

    override val protectedUserText: String
        get() = "This user's tweets are protected, and this tweet has therefore been blocked from this frame."

    override val logo: Shape = run {
        // Path Source: https://upload.wikimedia.org/wikipedia/commons/c/ce/X_logo_2023.svg
        val parser = PathParser()
        val handler = AWTPathProducer()
        parser.pathHandler = handler
        parser.parse("M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z")
        handler.shape
    }

    companion object {
        fun createTweetFrame(tweetId: Flow.Publisher<out Long>, timezone: ZoneId = ZoneId.systemDefault()): TweetFrame {
            return TweetFrame(tweetId.map { TweetLoader.loadTweetV2(it) }, timezone)
        }
    }
}
