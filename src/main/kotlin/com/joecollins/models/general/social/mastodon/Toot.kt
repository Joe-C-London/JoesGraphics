package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.joecollins.models.general.social.generic.Hashtag
import com.joecollins.models.general.social.generic.Link
import com.joecollins.models.general.social.generic.Media
import com.joecollins.models.general.social.generic.Post
import com.joecollins.models.general.social.generic.UserMention
import java.net.URL
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class Toot(
    private val content: String,
    private val account: User,
    override val url: URL,
    @JsonProperty("created_at") override val createdAt: Instant,
    private val mentions: List<Mention> = emptyList(),
    private val tags: List<Tag> = emptyList(),
    private val card: Card? = null,
    @JsonProperty("media_attachments") private val mediaAttachments: List<MediaAttachment> = emptyList(),
    private val emojis: List<Emoji> = emptyList()
) : Post<Toot> {

    override val text: String = content
        .replace("</p><p>", "\n\n")
        .replace("<p>", "")
        .replace("</p>", "")
        .replace(Regex("<span [^>]*>"), "")
        .replace("<span>", "")
        .replace("</span>", "")
        .replace(Regex("<a [^>]*>"), "<span style='color:#6364ff'>")
        .replace("</a>", "</span>")
        .let { text ->
            var t = text
            for (emoji in emojis) {
                t = t.replace(":${emoji.shortcode}:", "<img src='${emoji.staticUrl}' height='16' width='16'>")
            }
            t
        }

    override val user: User = account

    override val quoted: Toot? = null

    override val links: List<Link> = if (card == null || card.type != "link") emptyList() else listOf(card)

    override val mediaEntities: List<Media> = mediaAttachments

    override val hashtagEntities: List<Hashtag> = tags.flatMap { tag ->
        val matches = Regex("#(${tag.name})", RegexOption.IGNORE_CASE).find(text)
        (matches?.groupValues ?: emptyList())
            .filter { !it.startsWith("#") }
            .map { Tag(it, tag.url) }
    }

    override val userMentionEntities: List<UserMention> = mentions
}
