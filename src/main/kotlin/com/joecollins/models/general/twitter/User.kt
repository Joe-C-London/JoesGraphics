package com.joecollins.models.general.twitter

import java.net.URL

class User(
    val screenName: String,
    val name: String,
    val profileImageURL: URL,
    val isVerified: Boolean,
    val isProtected: Boolean
) {

    companion object {
        fun fromV1(user: twitter4j.User): User {
            return User(
                user.screenName,
                user.name,
                URL(user.profileImageURL),
                user.isVerified,
                user.isProtected
            )
        }
    }
}
