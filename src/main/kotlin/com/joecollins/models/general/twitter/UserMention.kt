package com.joecollins.models.general.twitter

class UserMention(val text: String) {

    companion object {
        fun fromV1(userMention: twitter4j.UserMentionEntity): UserMention {
            return UserMention(userMention.text)
        }

        fun fromV2(userMention: com.twitter.clientlib.model.MentionEntity): UserMention {
            return UserMention(userMention.username)
        }
    }
}
