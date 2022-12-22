package com.joecollins.models.general.social.generic

interface UserMention {
    val text: String
    val display: String
        get() = text
}
