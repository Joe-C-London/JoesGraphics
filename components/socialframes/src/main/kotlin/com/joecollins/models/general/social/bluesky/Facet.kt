package com.joecollins.models.general.social.bluesky

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.joecollins.models.general.social.generic.Hashtag
import com.joecollins.models.general.social.generic.Link
import com.joecollins.models.general.social.generic.UserMention
import java.net.URL

@JsonIgnoreProperties(ignoreUnknown = true)
data class Facet(
    val features: List<Feature>,
    val index: ByteSlice,
) {

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "\$type",
    )
    @JsonSubTypes(
        JsonSubTypes.Type(value = Tag::class, name = "app.bsky.richtext.facet#tag"),
        JsonSubTypes.Type(value = Mention::class, name = "app.bsky.richtext.facet#mention"),
        JsonSubTypes.Type(value = Link::class, name = "app.bsky.richtext.facet#link"),
    )
    open class Feature

    data class Tag(val tag: String) :
        Feature(),
        Hashtag {
        override val text: String = tag
    }

    data class Mention(val did: String) :
        Feature(),
        UserMention {
        override var text: String = did
    }

    data class Link(val uri: URL) :
        Feature(),
        com.joecollins.models.general.social.generic.Link {
        override var shortURL: String = uri.toString()
        override val expandedURL: URL = uri
        override var displayURL: String = uri.toString()
        override var preview: com.joecollins.models.general.social.generic.Link.Preview? = null
        override val isFromSocialNetwork: Boolean = false
    }

    data class ByteSlice(val byteStart: Int, val byteEnd: Int)

    fun displayText(text: String) = text.encodeToByteArray()
        .toList()
        .subList(index.byteStart, index.byteEnd)
        .toByteArray()
        .decodeToString()
}
