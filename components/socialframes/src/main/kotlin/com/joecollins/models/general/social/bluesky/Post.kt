package com.joecollins.models.general.social.bluesky

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.joecollins.models.general.social.generic.Emoji
import com.joecollins.models.general.social.generic.Hashtag
import com.joecollins.models.general.social.generic.Link
import com.joecollins.models.general.social.generic.Media
import com.joecollins.models.general.social.generic.Poll
import com.joecollins.models.general.social.generic.UserMention
import java.net.URL
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class Post(
    val author: User,
    val record: Record,
    val embed: Embed? = null,
) : com.joecollins.models.general.social.generic.Post {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Record(
        val text: String,
        val createdAt: Instant,
        val facets: List<Facet> = emptyList(),
    ) {
        fun links(embed: Embed?) = facets
            .filter { it.features[0] is Facet.Link }
            .map {
                (it.features[0] as Facet.Link).also { m ->
                    m.shortURL = it.displayText(text)
                    m.preview = embed?.takeIf { it is Embed.External }
                        ?.let { (it as Embed.External).external }
                }
            }

        fun hashtags() = facets
            .map { it.features[0] }
            .filterIsInstance(Facet.Tag::class.java)

        fun userMentions() = facets
            .filter { it.features[0] is Facet.Mention }
            .map { (it.features[0] as Facet.Mention).also { m -> m.text = it.displayText(text) } }
    }

    override val text: String = record.text
    override val user: User = author
    override val createdAt: Instant = record.createdAt
    override val quoted: Embed.Record.ViewRecord? = embed?.quoted()
    override val links: List<Link> = record.links(embed)
    override val mediaEntities: List<Media> = embed?.mediaEntities() ?: emptyList()
    override val hashtagEntities: List<Hashtag> = record.hashtags()
    override val userMentionEntities: List<UserMention> = record.userMentions()
    override val polls: List<Poll> = emptyList()
    override val emojis: List<Emoji> = emptyList()
    override val url: URL? = null
}
