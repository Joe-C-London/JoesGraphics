package com.joecollins.models.general.social.twitter

import com.joecollins.models.general.social.generic.Post
import com.twitter.clientlib.model.Photo
import java.net.URL
import java.time.Instant

class Tweet(
    id: Long,
    override val text: String,
    override val user: User,
    override val createdAt: Instant,
    override val quoted: Tweet?,
    override val links: List<Link>,
    override val mediaEntities: List<Media>,
    override val hashtagEntities: List<Hashtag>,
    override val userMentionEntities: List<UserMention>
) : Post<Tweet> {

    override val url: URL = URL("https://twitter.com/${user.screenName}/status/$id")

    companion object {
        fun fromV1(status: twitter4j.Status): Tweet {
            return Tweet(
                status.id,
                status.text,
                User.fromV1(status.user),
                status.createdAt.toInstant(),
                status.quotedStatus?.let { fromV1(it) },
                status.urlEntities.map { Link.fromV1(it) },
                status.mediaEntities.map { Media.fromV1(it) },
                status.hashtagEntities.map { Hashtag.fromV1(it) },
                status.userMentionEntities.map { UserMention.fromV1(it) }
            )
        }

        fun fromV2(tweet: com.twitter.clientlib.model.Tweet, expansions: com.twitter.clientlib.model.Expansions, quotedTweet: Tweet?): Tweet {
            val user = expansions.users
                ?.firstOrNull { it.id == tweet.authorId }
                ?: throw NoSuchElementException("No user with ID ${tweet.authorId}")
            val media = expansions.media
                ?.filterIsInstance(Photo::class.java)
                ?.associate { it.mediaKey!! to it.url!! }
                ?: emptyMap()
            return Tweet(
                tweet.id.toLong(),
                tweet.text,
                User.fromV2(user),
                tweet.createdAt!!.toInstant(),
                quotedTweet,
                tweet.entities?.urls?.mapNotNull { Link.fromV2(it) } ?: emptyList(),
                tweet.entities?.urls?.mapNotNull { Media.fromV2(it, media) } ?: emptyList(),
                tweet.entities?.hashtags?.map { Hashtag.fromV2(it) } ?: emptyList(),
                tweet.entities?.mentions?.map { UserMention.fromV2(it) } ?: emptyList()
            )
        }
    }
}
