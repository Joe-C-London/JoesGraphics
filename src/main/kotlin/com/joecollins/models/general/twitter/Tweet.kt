package com.joecollins.models.general.twitter

import com.twitter.clientlib.model.Photo
import java.time.Instant

class Tweet(
    val id: Long,
    val text: String,
    val user: User,
    val createdAt: Instant,
    val quotedTweet: Tweet?,
    val links: List<Link>,
    val mediaEntities: List<Media>,
    val hashtagEntities: List<Hashtag>,
    val userMentionEntities: List<UserMention>
) {

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
