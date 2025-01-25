package com.joecollins.graphics.dialogs

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.joecollins.models.general.social.bluesky.BlueSkyInstance
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.FileEntity
import org.apache.hc.core5.http.io.entity.StringEntity
import java.awt.Color
import java.io.File
import java.time.Instant
import javax.imageio.ImageIO
import javax.swing.JPanel

class BlueSkyDialog(panel: JPanel) : GenericSocialDialog(panel) {

    override val siteColor: Color get() = Color(16, 131, 254)

    override val action: String get() = "Post!"

    override val actionInProgress: String get() = "Posting..."

    override val maxLength: Int get() = 300

    override fun send(post: String, image: File, altText: String?) {
        lateinit var blob: JsonObject
        BlueSkyInstance.execute({
            HttpPost("https://${BlueSkyInstance.server}/xrpc/com.atproto.repo.uploadBlob").apply {
                addHeader("Content-Type", "application/png")
                entity = FileEntity(image, ContentType.IMAGE_PNG)
            }
        }, { response ->
            blob = response.asJsonObject["blob"].asJsonObject
        })

        val imageTag = mapOf(
            "image" to blob,
            "alt" to (altText ?: ""),
            "aspectRatio" to ImageIO.read(image).let { img ->
                mapOf(
                    "width" to img.width,
                    "height" to img.height,
                )
            },
        )
        val imagesTag = mapOf(
            "\$type" to "app.bsky.embed.images",
            "images" to listOf(imageTag),
        )
        val recordTag = mapOf(
            "\$type" to "app.bsky.feed.post",
            "text" to post,
            "embed" to imagesTag,
            "createdAt" to Instant.now().toString(),
        )
        val postTag = mapOf(
            "collection" to "app.bsky.feed.post",
            "repo" to BlueSkyInstance.did,
            "record" to recordTag,
        )
        val json = GsonBuilder().create().toJson(postTag)
        BlueSkyInstance.execute({
            HttpPost("https://${BlueSkyInstance.server}/xrpc/com.atproto.repo.createRecord").apply {
                addHeader("Content-Type", "application/json")
                entity = StringEntity(json)
            }
        })
    }
}
