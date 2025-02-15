package com.joecollins.models.general.social.bluesky

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.joecollins.models.general.social.generic.Emoji
import com.joecollins.models.general.social.generic.Hashtag
import com.joecollins.models.general.social.generic.Link
import com.joecollins.models.general.social.generic.Media
import com.joecollins.models.general.social.generic.Poll
import com.joecollins.models.general.social.generic.UserMention
import java.awt.Image
import java.net.URL
import java.time.Instant
import javax.imageio.ImageIO

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "\$type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Embed.Images::class, name = "app.bsky.embed.images#view"),
    JsonSubTypes.Type(value = Embed.External::class, name = "app.bsky.embed.external#view"),
    JsonSubTypes.Type(value = Embed.Record::class, name = "app.bsky.embed.record#view"),
)
open class Embed {
    fun mediaEntities(): List<Media> = takeIf { it is Embed.Images }
        ?.let { (it as Embed.Images).images }
        ?: emptyList()

    fun quoted(): Embed.Record.ViewRecord? = takeIf { it is Embed.Record }
        ?.let { (it as Embed.Record).record }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Images(val images: List<Image>) : Embed() {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Image(
            val thumb: URL,
            val fullsize: URL,
            val alt: String,
        ) : com.joecollins.models.general.social.generic.Media {
            override val mediaURL: URL = fullsize
            override val displayURL: String = fullsize.toString()
        }
    }

    data class External(val external: ViewExternal) : Embed() {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class ViewExternal(
            val uri: URL,
            override val title: String,
            val description: String,
            val thumb: URL? = null,
        ) : com.joecollins.models.general.social.generic.Link.Preview {
            override val image: Image? = thumb?.let { ImageIO.read(it) }
            override val domain: String = uri.host
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Record(val record: ViewRecord) : Embed() {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class ViewRecord(
            val author: User,
            val value: Post.Record,
            val embeds: List<Embed>,
        ) : com.joecollins.models.general.social.generic.Post {
            override val text: String = value.text
            override val user: com.joecollins.models.general.social.generic.User = author
            override val createdAt: Instant = value.createdAt
            override val quoted: ViewRecord? = null
            override val links: List<Link> = value.links(embeds.firstOrNull())
            override val mediaEntities: List<Media> = embeds.flatMap { it.mediaEntities() }
            override val hashtagEntities: List<Hashtag> = value.hashtags()
            override val userMentionEntities: List<UserMention> = value.userMentions()
            override val polls: List<Poll> = emptyList()
            override val emojis: List<Emoji> = emptyList()
            override val url: URL? = null
        }
    }
}
