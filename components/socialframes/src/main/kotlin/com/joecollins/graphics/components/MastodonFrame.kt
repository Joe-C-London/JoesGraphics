package com.joecollins.graphics.components

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.joecollins.models.general.social.mastodon.Toot
import com.joecollins.models.general.social.mastodon.User
import com.joecollins.pubsub.map
import java.awt.Color
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Flow

class MastodonFrame(toot: Flow.Publisher<out Toot>, timezone: ZoneId = ZoneId.systemDefault()) : SocialMediaFrame<Toot>(toot, timezone) {

    override val color: Color
        get() = Color(99, 100, 255)
    override val emojiVersion: String
        get() = "google/noto-emoji/v2.034"
    override val protectedUserText: String
        get() = "This status is protected, and is therefore being blocked from this frame."

    companion object {

        private val objectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
        fun createFrame(postId: Flow.Publisher<out Pair<String, Long>>, timezone: ZoneId = ZoneId.systemDefault()): MastodonFrame {
            return MastodonFrame(
                postId.map { (server, id) ->
                    val url = URL("https://$server/api/v1/statuses/$id")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()
                    if (connection.responseCode < 400) {
                        objectMapper.readValue(connection.inputStream)
                    } else {
                        val error = objectMapper.readTree(connection.errorStream)["error"].asText()
                        fromError(error, url)
                    }
                },
                timezone,
            )
        }

        internal fun fromError(error: String, url: URL): Toot {
            return Toot(
                content = error,
                account = User(
                    username = "",
                    acct = "",
                    displayName = "Error retrieving post",
                    url = this::class.java.classLoader.getResource("1x1.png"),
                    avatar = this::class.java.classLoader.getResource("1x1.png"),
                ),
                createdAt = Instant.EPOCH,
                url = url,
            )
        }
    }
}
