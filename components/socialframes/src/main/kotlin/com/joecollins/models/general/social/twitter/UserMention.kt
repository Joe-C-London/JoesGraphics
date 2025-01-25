package com.joecollins.models.general.social.twitter

class UserMention(override val text: String) : com.joecollins.models.general.social.generic.UserMention {

    companion object {
        fun fromV1(userMention: twitter4j.UserMentionEntity): UserMention = UserMention("@${userMention.text}")

        fun fromV2(userMention: com.twitter.clientlib.model.MentionEntity): UserMention = UserMention("@${userMention.username}")
    }
}
