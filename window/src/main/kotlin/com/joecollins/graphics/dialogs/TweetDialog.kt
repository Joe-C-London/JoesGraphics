package com.joecollins.graphics.dialogs

import com.joecollins.models.general.social.twitter.TwitterV2InstanceFactory
import com.twitter.clientlib.model.TweetCreateRequest
import com.twitter.clientlib.model.TweetCreateRequestMedia
import twitter4j.HttpClientFactory
import twitter4j.HttpParameter
import twitter4j.HttpResponseEvent
import twitter4j.HttpResponseListener
import twitter4j.JSONObject
import twitter4j.StatusUpdate
import twitter4j.TwitterFactory
import twitter4j.auth.AuthorizationFactory
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder
import java.awt.Color
import java.io.File
import java.util.Properties
import javax.swing.JPanel

class TweetDialog(panel: JPanel) : GenericSocialDialog(panel) {

    override val siteColor: Color
        get() = Color(0x00acee)

    override val action: String
        get() = "Tweet"

    override val actionInProgress: String
        get() = "Tweeting"

    override val maxLength: Int
        get() = 280

    init {
        title = "Tweet"
    }

    override fun send(post: String, image: File, altText: String?) {
        sendTweetV2(post, image, altText)
    }

    private fun sendTweetV2(tweet: String, image: File, altText: String?) {
        val instance = TwitterV2InstanceFactory.instance
        val mediaId = uploadMediaV1(image, altText)
        val mediaRequest = TweetCreateRequestMedia()
        mediaRequest.mediaIds = listOf(mediaId.toString())
        val tweetRequest = TweetCreateRequest()
        tweetRequest.text = tweet
        tweetRequest.media = mediaRequest
        instance.tweets().createTweet(tweetRequest).execute()
    }

    private fun uploadMediaV1(image: File, altText: String?): Long {
        val cb = ConfigurationBuilder()
        val twitterPropertiesFile = this.javaClass.classLoader.getResourceAsStream("twitter.properties")
            ?: throw IllegalStateException("Unable to find twitter.properties")
        val properties = Properties()
        properties.load(twitterPropertiesFile)
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey(properties["oauthConsumerKey"].toString())
            .setOAuthConsumerSecret(properties["oauthConsumerSecret"].toString())
            .setOAuthAccessToken(properties["oauthAccessToken"].toString())
            .setOAuthAccessTokenSecret(properties["oauthAccessTokenSecret"].toString())
        val conf = cb.build()
        val twitter = TwitterFactory(conf).instance
        val response = twitter.uploadMedia(image)

        if (altText != null) {
            addAltText(conf, response.mediaId, altText)
        }

        return response.mediaId
    }

    private fun addAltText(conf: Configuration, mediaId: Long, altText: String) {
        val client = HttpClientFactory.getInstance(conf.httpClientConfiguration)
        val auth = AuthorizationFactory.getInstance(conf)
        val params = JSONObject()
        params.put("media_id", mediaId)
        params.put("alt_text", JSONObject().also { it.put("text", altText) })
        client.post(
            "${conf.uploadBaseURL}/media/metadata/create.json",
            arrayOf(
                HttpParameter(params)
            ),
            auth,
            object : HttpResponseListener {
                override fun httpResponseReceived(event: HttpResponseEvent) {
                    // no-op
                }
            }
        )
    }

    private fun sendTweetV1(tweet: String, image: File) {
        val status = StatusUpdate(tweet)
        status.media(image)
        val cb = ConfigurationBuilder()
        val twitterPropertiesFile = this.javaClass.classLoader.getResourceAsStream("twitter.properties")
            ?: throw IllegalStateException("Unable to find twitter.properties")
        val properties = Properties()
        properties.load(twitterPropertiesFile)
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey(properties["oauthConsumerKey"].toString())
            .setOAuthConsumerSecret(properties["oauthConsumerSecret"].toString())
            .setOAuthAccessToken(properties["oauthAccessToken"].toString())
            .setOAuthAccessTokenSecret(properties["oauthAccessTokenSecret"].toString())
        TwitterFactory(cb.build()).instance.updateStatus(status)
    }
}
