package com.joecollins.models.general.social.generic

import java.net.URL
import java.time.Instant

interface Post {
    val text: String
    val user: User
    val createdAt: Instant
    val quoted: Post?
    val links: List<Link>
    val mediaEntities: List<Media>
    val hashtagEntities: List<Hashtag>
    val userMentionEntities: List<UserMention>

    val url: URL
}
