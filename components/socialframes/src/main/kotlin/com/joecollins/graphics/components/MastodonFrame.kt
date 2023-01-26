package com.joecollins.graphics.components

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.joecollins.models.general.social.mastodon.Toot
import com.joecollins.models.general.social.mastodon.User
import com.joecollins.pubsub.map
import org.apache.batik.parser.AWTPathProducer
import org.apache.batik.parser.PathParser
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Area
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

    override val logo: Shape = run {
        // Path Source: https://upload.wikimedia.org/wikipedia/commons/4/48/Mastodon_Logotype_%28Simple%29.svg
        val parser = PathParser()

        val handler = AWTPathProducer()
        parser.pathHandler = handler
        parser.parse("M211.80734 139.0875c-3.18125 16.36625-28.4925 34.2775-57.5625 37.74875-15.15875 1.80875-30.08375 3.47125-45.99875 2.74125-26.0275-1.1925-46.565-6.2125-46.565-6.2125 0 2.53375.15625 4.94625.46875 7.2025 3.38375 25.68625 25.47 27.225 46.39125 27.9425 21.11625.7225 39.91875-5.20625 39.91875-5.20625l.8675 19.09s-14.77 7.93125-41.08125 9.39c-14.50875.7975-32.52375-.365-53.50625-5.91875C9.23234 213.82 1.40609 165.31125.20859 116.09125c-.365-14.61375-.14-28.39375-.14-39.91875 0-50.33 32.97625-65.0825 32.97625-65.0825C49.67234 3.45375 78.20359.2425 107.86484 0h.72875c29.66125.2425 58.21125 3.45375 74.8375 11.09 0 0 32.975 14.7525 32.975 65.0825 0 0 .41375 37.13375-4.59875 62.915")
        val outer = handler.shape

        val handler2 = AWTPathProducer()
        parser.pathHandler = handler2
        parser.parse("M177.50984 80.077v60.94125h-24.14375v-59.15c0-12.46875-5.24625-18.7975-15.74-18.7975-11.6025 0-17.4175 7.5075-17.4175 22.3525v32.37625H96.20734V85.42325c0-14.845-5.81625-22.3525-17.41875-22.3525-10.49375 0-15.74 6.32875-15.74 18.7975v59.15H38.90484V80.077c0-12.455 3.17125-22.3525 9.54125-29.675 6.56875-7.3225 15.17125-11.07625 25.85-11.07625 12.355 0 21.71125 4.74875 27.8975 14.2475l6.01375 10.08125 6.015-10.08125c6.185-9.49875 15.54125-14.2475 27.8975-14.2475 10.6775 0 19.28 3.75375 25.85 11.07625 6.36875 7.3225 9.54 17.22 9.54 29.675")
        val inner = handler2.shape

        val ret = Area(outer)
        ret.subtract(Area(inner))
        ret
    }

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
