package com.joecollins.models.general.twitter

class UserMention(val text: String) {

    companion object {
        fun fromV1(userMention: twitter4j.UserMentionEntity): UserMention {
            return UserMention(userMention.text)
        }
    }
}
