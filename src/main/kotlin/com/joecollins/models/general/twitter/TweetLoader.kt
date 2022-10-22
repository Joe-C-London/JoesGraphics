package com.joecollins.models.general.twitter

import java.util.Properties

object TweetLoader {

    fun loadTweetV1(id: Long): Tweet {
        val cb = twitter4j.conf.ConfigurationBuilder()
        val twitterPropertiesFile = this::class.java.classLoader.getResourceAsStream("twitter.properties")
            ?: throw IllegalStateException("Unable to find twitter.properties")
        val properties = Properties()
        properties.load(twitterPropertiesFile)
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey(properties["oauthConsumerKey"].toString())
            .setOAuthConsumerSecret(properties["oauthConsumerSecret"].toString())
            .setOAuthAccessToken(properties["oauthAccessToken"].toString())
            .setOAuthAccessTokenSecret(properties["oauthAccessTokenSecret"].toString())
        val instance = twitter4j.TwitterFactory(cb.build()).instance
        return Tweet.fromV1(instance.showStatus(id))
    }
}
