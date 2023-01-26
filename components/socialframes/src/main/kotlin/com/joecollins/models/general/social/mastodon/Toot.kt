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
    override val emojis: List<Emoji> = emptyList(),
    private val poll: Poll? = null,
) : Post<Toot> {

    override val text: String = content
        .replace("</p><p>", "\n\n")
        .replace("<p>", "")
        .replace("</p>", "")
        .replace(Regex("<span [^>]*>"), "")
        .replace("<span>", "")
        .replace("</span>", "")
        .replace(Regex("<a [^>]*>"), "")
        .replace("</a>", "")

    override val user: User = account

    override val quoted: Toot? = null

    private val urls = Regex("<a [^>]*>(.*)</a>").findAll(content)
        .mapNotNull { it.groups.filterNotNull().firstOrNull { g -> !g.value.contains("<a") } }
        .map {
            it.value
                .replace(Regex("<span [^>]*>"), "")
                .replace("<span>", "")
                .replace("</span>", "")
        }
        .map { LinkWithoutCard(it) }
        .toList()

    override val links: List<Link> =
        if (card == null || card.type != "link") {
            urls
        } else {
            listOf(listOf(card), urls).flatten()
        }

    override val mediaEntities: List<Media> = mediaAttachments

    override val hashtagEntities: List<Hashtag> = tags.flatMap { tag ->
        val matches = Regex("#(${tag.name})", RegexOption.IGNORE_CASE).find(text)
        (matches?.groupValues ?: emptyList())
            .filter { !it.startsWith("#") }
            .map { Tag(it, tag.url) }
    }

    override val userMentionEntities: List<UserMention> = mentions

    override val polls: List<com.joecollins.models.general.social.generic.Poll> = poll?.let { listOf(it) } ?: emptyList()
}
