package com.joecollins.models.general.twitter

import io.webfolder.cdp.Launcher
import java.awt.Image
import java.net.URL
import javax.imageio.ImageIO

class Link(
    val shortURL: URL,
    val expandedURL: URL,
    val displayURL: String,
    val image: Image?,
    val title: String,
    val domain: String
) {

    companion object {
        fun fromV1(urlEntity: twitter4j.URLEntity): Link {
            var image: Image?
            var title: String
            var domain: String

            val launcher = Launcher()
            launcher.launch().use { sessionFactory ->
                sessionFactory.create().use { session ->
                    session.navigate(urlEntity.expandedURL.toString()).waitDocumentReady().wait(5000)
                    val imageURL = session.getAttribute("//meta[@name='twitter:image:src']", "content")
                    image = imageURL?.let { ImageIO.read(URL(it)) }
                    title = session.getAttribute("//meta[@name='twitter:title']", "content")
                        ?: session.getText("//title")
                    domain = session.getAttribute("//meta[@name='twitter:domain']", "content")
                        ?: urlEntity.expandedURL.toString().let { it.substring(it.indexOf("//") + 2) }.let { it.substring(0, it.indexOf("/")) }
                }
            }
            return Link(
                URL(urlEntity.url),
                URL(urlEntity.expandedURL),
                urlEntity.displayURL,
                image,
                title,
                domain
            )
        }

        fun fromV2(urlEntity: com.twitter.clientlib.model.UrlEntity): Link? {
            if (urlEntity.mediaKey != null) return null
            val fullUrl = urlEntity.unwoundUrl ?: urlEntity.expandedUrl ?: urlEntity.url
            return Link(
                urlEntity.url,
                fullUrl,
                urlEntity.displayUrl!!,
                urlEntity.images?.takeIf { it.isNotEmpty() }?.let { it[0].url }?.let { ImageIO.read(it) },
                urlEntity.title ?: "",
                fullUrl.host
            )
        }
    }
}
