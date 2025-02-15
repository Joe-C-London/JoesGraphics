package com.joecollins.graphics.components

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.joecollins.graphics.SVGUtils
import com.joecollins.models.general.social.bluesky.BlueSkyInstance
import com.joecollins.models.general.social.bluesky.Post
import com.joecollins.models.general.social.bluesky.User
import com.joecollins.pubsub.map
import org.apache.hc.client5.http.classic.methods.HttpGet
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Area
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Flow

class BlueskyFrame(
    toot: Flow.Publisher<out Post>,
    timezone: ZoneId = ZoneId.systemDefault(),
) : SocialMediaFrame<Post>(toot, Color(16, 131, 254), timezone) {

    override val emojiVersion: String
        get() = "google/noto-emoji/v2.034"
    override val protectedUserText: String
        get() = "This post is protected, and is therefore being blocked from this frame."

    override val logo: Shape = run {
        // Path Source: https://upload.wikimedia.org/wikipedia/commons/7/7a/Bluesky_Logo.svg
        val outline = SVGUtils.createShape("m135.72 44.03c66.496 49.921 138.02 151.14 164.28 205.46 26.262-54.316 97.782-155.54 164.28-205.46 47.98-36.021 125.72-63.892 125.72 24.795 0 17.712-10.155 148.79-16.111 170.07-20.703 73.984-96.144 92.854-163.25 81.433 117.3 19.964 147.14 86.092 82.697 152.22-122.39 125.59-175.91-31.511-189.63-71.766-2.514-7.3797-3.6904-10.832-3.7077-7.8964-0.0174-2.9357-1.1937 0.51669-3.7077 7.8964-13.714 40.255-67.233 197.36-189.63 71.766-64.444-66.128-34.605-132.26 82.697-152.22-67.108 11.421-142.55-7.4491-163.25-81.433-5.9562-21.282-16.111-152.36-16.111-170.07 0-88.687 77.742-60.816 125.72-24.795z")
        Area(outline)
    }

    companion object {

        private val objectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
        fun createFrame(postId: Flow.Publisher<out Triple<String, String, String>>, timezone: ZoneId = ZoneId.systemDefault()): BlueskyFrame = BlueskyFrame(
            postId.map { (server, handle, key) ->
                val did = BlueSkyInstance.execute({
                    HttpGet("https://$server/xrpc/app.bsky.actor.getProfile?actor=$handle")
                }, { res ->
                    res.asJsonObject["did"].asString
                })
                val uri = "at://$did/app.bsky.feed.post/$key"
                BlueSkyInstance.execute({
                    HttpGet("https://${BlueSkyInstance.server}/xrpc/app.bsky.feed.getPosts?uris=$uri")
                }, { res ->
                    val post = res.asJsonObject["posts"].asJsonArray[0]
                    objectMapper.readValue(post.toString(), Post::class.java)
                })
            },
            timezone,
        )

        internal fun fromError(error: String): Post = Post(
            record = Post.Record(
                text = error,
                createdAt = Instant.EPOCH,
            ),
            author = User(
                handle = "",
                displayName = "Error retrieving post",
                avatar = this::class.java.classLoader.getResource("1x1.png"),
            ),
        )
    }
}
