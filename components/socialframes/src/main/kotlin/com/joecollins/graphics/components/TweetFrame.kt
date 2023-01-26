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

class TweetFrame internal constructor(tweet: Flow.Publisher<out Tweet>, timezone: ZoneId = ZoneId.systemDefault()) : SocialMediaFrame<Tweet>(tweet, timezone) {
    override val color
        get() = Color(0x00acee)

    override val emojiVersion: String
        get() = "twitter/v14.0"

    override val protectedUserText: String
        get() = "This user's tweets are protected, and this tweet has therefore been blocked from this frame."

    override val logo: Shape = run {
        // Path Source: https://upload.wikimedia.org/wikipedia/commons/4/4f/Twitter-logo.svg
        val parser = PathParser()
        val handler = AWTPathProducer()
        parser.pathHandler = handler
        parser.parse("M221.95 51.29c.15 2.17.15 4.34.15 6.53 0 66.73-50.8 143.69-143.69 143.69v-.04c-27.44.04-54.31-7.82-77.41-22.64 3.99.48 8 .72 12.02.73 22.74.02 44.83-7.61 62.72-21.66-21.61-.41-40.56-14.5-47.18-35.07 7.57 1.46 15.37 1.16 22.8-.87-23.56-4.76-40.51-25.46-40.51-49.5v-.64c7.02 3.91 14.88 6.08 22.92 6.32C11.58 63.31 4.74 33.79 18.14 10.71c25.64 31.55 63.47 50.73 104.08 52.76-4.07-17.54 1.49-35.92 14.61-48.25 20.34-19.12 52.33-18.14 71.45 2.19 11.31-2.23 22.15-6.38 32.07-12.26-3.77 11.69-11.66 21.62-22.2 27.93 10.01-1.18 19.79-3.86 29-7.95-6.78 10.16-15.32 19.01-25.2 26.16z")
        handler.shape
    }

    companion object {
        fun createTweetFrame(tweetId: Flow.Publisher<out Long>, timezone: ZoneId = ZoneId.systemDefault()): TweetFrame {
            return TweetFrame(tweetId.map { TweetLoader.loadTweetV2(it) }, timezone)
        }
    }
}
