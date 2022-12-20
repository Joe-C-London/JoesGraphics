package com.joecollins.models.general.social.twitter

class Poll(override val options: Map<String, Int>) : com.joecollins.models.general.social.generic.Poll {
    companion object {
        fun fromV2(poll: com.twitter.clientlib.model.Poll): Poll {
            val votes = poll.options
                .sortedBy { it.position }
                .associate { it.label to it.votes }
            return Poll(votes)
        }
    }
}
