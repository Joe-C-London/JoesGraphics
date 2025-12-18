package com.joecollins.graphics.dialogs

import com.fasterxml.jackson.databind.json.JsonMapper
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.BasicHttpEntity
import org.apache.hc.core5.util.Timeout
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Color
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

class MastodonDialog(panel: JPanel, private val server: String, private val token: String) : GenericSocialDialog(panel) {

    class MastodonException(message: String) : RuntimeException(message)

    override val siteColor: Color get() = Color(99, 100, 255)

    override val action: String get() = "Publish!"

    override val actionInProgress: String get() = "Publishing"

    override val maxLength: Int get() = 500

    override val maxAltTextLength: Int get() = 1500

    init {
        title = "Mastodon ($server)"
    }

    override fun send(post: String, image: File, altText: String?) {
        val timeout = 30L
        HttpClientBuilder.create()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.of(timeout, TimeUnit.SECONDS))
                    .setResponseTimeout(Timeout.of(timeout, TimeUnit.SECONDS))
                    .build(),
            )
            .build()
            .use { client: CloseableHttpClient ->
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
        return client.execute(post) { response ->
            if (!(200..299).contains(response.code)) {
                throw MastodonException("HTTP ${response.code}: ${response.entity.content.bufferedReader().readText()}")
            }
            val json = JsonMapper().readTree(response.entity.content)
            json["id"].asText()
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
            ContentType.APPLICATION_JSON,
        )
        client.execute(post) { response ->
            if (!(200..299).contains(response.code)) {
                throw MastodonException("HTTP ${response.code}: ${response.entity.content.bufferedReader().readText()}")
            }
        }
    }
}
