package com.joecollins.models.general.social.mastodon

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Poll(
    @JsonProperty("options") val opts: List<Option>
) : com.joecollins.models.general.social.generic.Poll {

    data class Option(
        val title: String,
        @JsonProperty("votes_count") val votesCount: Int
    )

    @JsonIgnore
    override val options: Map<String, Int> = opts.associate { it.title to it.votesCount }
}
