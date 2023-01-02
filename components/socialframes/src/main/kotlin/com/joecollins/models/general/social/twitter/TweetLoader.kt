package com.joecollins.models.general.social.twitter

import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.model.TweetReferencedTweets
import java.time.Instant
import java.util.*

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

    fun loadTweetV2(id: Long): Tweet {
        val instance: TwitterApi = TwitterV2InstanceFactory.instance
        return loadTweetV2(id, instance)
    }

    internal fun loadTweetV2(id: Long, instance: TwitterApi): Tweet {
        val response = instance.tweets().findTweetById(id.toString())
            .expansions(setOf("author_id", "attachments.media_keys", "attachments.poll_ids"))
            .tweetFields(setOf("created_at", "entities", "referenced_tweets"))
            .userFields(setOf("profile_image_url", "verified", "protected"))
            .mediaFields(setOf("url"))
            .execute()
        val errors = response.errors?.takeIf { it.isNotEmpty() }
        if (errors != null) {
            return Tweet(
                id,
                errors.joinToString("\n\n") { "${it.title}: ${it.detail}" },
                User(
                    "",
                    "Error retrieving tweet",
                    javaClass.classLoader.getResource("1x1.png"),
                    false,
                    false,
                ),
                Instant.EPOCH,
                null,
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
                emptyList(),
            )
        }
        val tweet = response
            .data
            ?: throw NoSuchElementException("No tweet with ID $id")
        val user = response
            .includes
            ?: throw NoSuchElementException("No expansions for tweet with ID $id")
        val quotedTweet = tweet.referencedTweets
            ?.firstOrNull { it.type == TweetReferencedTweets.TypeEnum.QUOTED }
            ?.let { loadTweetV2(it.id.toLong(), instance) }
        return Tweet.fromV2(tweet, user, quotedTweet)
    }
}
