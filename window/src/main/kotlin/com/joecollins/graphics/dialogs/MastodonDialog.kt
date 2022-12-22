package com.joecollins.graphics.dialogs

import com.fasterxml.jackson.databind.json.JsonMapper
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.BasicHttpEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.json.JSONObject
import org.json.JSONArray
import java.awt.Color
import java.io.File
import java.nio.charset.Charset
import java.util.Properties
import javax.swing.JPanel

class MastodonDialog(panel: JPanel) : GenericSocialDialog(panel) {

    override val siteColor: Color
        get() = Color(99, 100, 255)

    override val action: String
        get() = "Publish!"

    override val actionInProgress: String
        get() = "Publishing"

    override val maxLength: Int
        get() = 500

    init {
        title = "Mastodon"
    }

    override fun send(post: String, image: File, altText: String?) {
        val mastodonPropertiesFile = this.javaClass.classLoader.getResourceAsStream("mastodon.properties")
            ?: throw IllegalStateException("Unable to find mastodon.properties")
        val properties = Properties()
        properties.load(mastodonPropertiesFile)

        val server = properties["instance.server"].toString()
        val token = properties["instance.token"].toString()

        HttpClients.createDefault().use { client: CloseableHttpClient ->
            val imageId = uploadImage(client, server, token, image, altText)
            sendPost(client, server, token, post, imageId)
        }
    }

    private fun uploadImage(client: CloseableHttpClient, server: String, token: String, image: File, altText: String?): String {
        val post = HttpPost("https://$server/api/v2/media")
        post.addHeader("Authorization", "Bearer $token")
        post.entity = MultipartEntityBuilder.create()
            .addBinaryBody("file", image)
            .also { if (altText != null) it.addTextBody("description", altText) }
            .build()
        client.execute(post).use { response ->
            val json = JsonMapper().readTree(response.entity.content)
            return json["id"].asText()
        }
    }

    private fun sendPost(client: CloseableHttpClient, server: String, token: String, text: String, imageId: String) {
        val post = HttpPost("https://$server/api/v1/statuses")
        post.addHeader("Authorization", "Bearer $token")
        val params = JSONObject()
        params.put("status", text)
        params.put("media_ids", JSONArray().also { it.put(imageId) })
        post.entity = BasicHttpEntity(
            params.toString().byteInputStream(Charset.defaultCharset()),
            ContentType.APPLICATION_JSON
        )
        client.execute(post).use { response ->
            EntityUtils.consume(response.entity)
        }
    }
}
