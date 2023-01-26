package com.joecollins.models.general.social.twitter

import io.webfolder.cdp.Launcher
import java.awt.Image
import java.net.URL
import javax.imageio.ImageIO

class Link(
    override val shortURL: URL,
    override val expandedURL: URL,
    override val displayURL: String,
    image: Image?,
    title: String,
    domain: String,
) : com.joecollins.models.general.social.generic.Link {

    class Preview(
        override val image: Image?,
        override val title: String,
        override val domain: String,
    ) : com.joecollins.models.general.social.generic.Link.Preview

    override val preview = Preview(image, title, domain)

    override val isFromSocialNetwork: Boolean = expandedURL.toString().startsWith("https://twitter.com/")

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
                domain,
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
                fullUrl.host,
            )
        }
    }
}
