package com.joecollins.models.general.twitter

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
    }
}
