package com.joecollins.models.general.social.twitter

import java.net.URL

class User(
    override val screenName: String,
    override val name: String,
    override val profileImageURL: URL,
    override val isVerified: Boolean,
    override val isProtected: Boolean
) : com.joecollins.models.general.social.generic.User {

    companion object {
        fun fromV1(user: twitter4j.User): User {
            return User(
                "@${user.screenName}",
                user.name,
                URL(user.profileImageURL),
                user.isVerified,
                user.isProtected
            )
        }

        fun fromV2(user: com.twitter.clientlib.model.User): User {
            return User(
                "@${user.username}",
                user.name,
                user.profileImageUrl!!,
                user.verified!!,
                user.protected!!
            )
        }
    }
}
