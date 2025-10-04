package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.models.general.social.bluesky.Embed
import com.joecollins.models.general.social.bluesky.Facet
import com.joecollins.models.general.social.bluesky.Post
import com.joecollins.models.general.social.bluesky.User
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Dimension
import java.net.URL
import java.time.Instant
import java.time.ZoneId

class BlueskyFrameTest {

    @Test
    fun testBasicPost() {
        val post = Post(
            record = Post.Record(
                text = "This tweet will test whether this frame renders.  It will be long enough to wrap to the next line.",
                createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            ),
            author = User(
                handle = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
            ),
        )

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "Basic", frame)
    }

    @Test
    fun testMultiLinePost() {
        val post = Post(
            record = Post.Record(
                text = "This tweet will test whether this frame renders.\nIt will contain deliberate line breaks.",
                createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            ),
            author = User(
                handle = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
            ),
        )

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "MultiLine", frame)
    }

    @Test
    fun testMentionsAndHashtags() {
        val post = Post(
            record = Post.Record(
                text = "Will @NicolaSturgeon win another term as FM?  Will @Douglas4Moray propel the Tories to a historic victory?  Or will @AnasSarwar lead Labour back to power?  Only three weeks to the #ScotParl election!  #Election2021",
                createdAt = Instant.parse("2021-04-15T21:34:17Z"),
                facets = listOf(
                    Facet(listOf(Facet.Mention("did:plc:1")), Facet.ByteSlice(5, 20)),
                    Facet(listOf(Facet.Mention("did:plc:2")), Facet.ByteSlice(51, 65)),
                    Facet(listOf(Facet.Mention("did:plc:3")), Facet.ByteSlice(116, 127)),
                    Facet(listOf(Facet.Tag("ScotParl")), Facet.ByteSlice(180, 189)),
                    Facet(listOf(Facet.Tag("Election2021")), Facet.ByteSlice(201, 214)),
                ),
            ),
            author = User(
                handle = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
            ),
        )

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "MentionsAndHashtags", frame)
    }

    @Test
    fun testLinks() {
        val post = Post(
            record = Post.Record(
                text = "Go to https://www.bbc.co.uk/news/world-europ... for an amusing story.",
                createdAt = Instant.parse("2021-04-15T21:34:17Z"),
                facets = listOf(
                    Facet(listOf(Facet.Link(URL("https://www.bbc.co.uk/news/world-europe-56757956"))), Facet.ByteSlice(6, 47)),
                ),
            ),
            author = User(
                handle = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
            ),
            embed = Embed.External(
                Embed.External.ViewExternal(
                    uri = URL("https://www.bbc.co.uk/news/world-europe-56757956"),
                    title = "Mystery tree beast turns out to be croissant",
                    description = "Polish animal welfare officers responding to a call discover the creature is in fact a pastry.",
                    thumb = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")!!,
                ),
            ),
        )

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "Links", frame)
    }

    @Test
    fun testLinkWithoutCard() {
        val post = Post(
            record = Post.Record(
                text = "Go to https://www.bbc.co.uk/news/world-europ... for an amusing story.",
                createdAt = Instant.parse("2021-04-15T21:34:17Z"),
                facets = listOf(
                    Facet(listOf(Facet.Link(URL("https://www.bbc.co.uk/news/world-europe-56757956"))), Facet.ByteSlice(6, 47)),
                ),
            ),
            author = User(
                handle = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
            ),
        )

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "LinksWithoutCard", frame)
    }

    @Test
    fun testImages() {
        val post = Post(
            record = Post.Record(
                text = "We should be able to render an image or two.\n\nLook!  A croissant!",
                createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            ),
            author = User(
                handle = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
            ),
            embed = Embed.Images(
                listOf(
                    Embed.Images.Image(
                        thumb = BlueskyFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg"),
                        fullsize = BlueskyFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg"),
                        alt = "A croissant",
                    ),
                ),
            ),
        )

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "Images", frame)
    }

    @Test
    fun testPostWithEmoji() {
        val post = Post(
            record = Post.Record(
                text = "How do you get emojis to appear in your post? \uD83E\uDDF5",
                createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            ),
            author = User(
                handle = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
            ),
        )

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "Emoji", frame)
    }

    @Test
    fun testQuotePost() {
        val post = Post(
            record = Post.Record(
                text = "Amateurs!",
                createdAt = Instant.parse("2021-04-19T08:24:53+01:00"),
            ),
            author = User(
                handle = "Frenchman",
                displayName = "A Frenchman",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/tower.png"),
            ),
            embed = Embed.Record(
                record = Embed.Record.ViewRecord(
                    value = Post.Record(
                        text = "We should be able to render an image or two.\n\nLook!  A croissant!",
                        createdAt = Instant.parse("2021-04-15T21:34:17Z"),
                    ),
                    author = User(
                        handle = "Joe_C_London",
                        displayName = "Joe C",
                        avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                    ),
                    embeds = listOf(
                        Embed.Images(
                            listOf(
                                Embed.Images.Image(
                                    thumb = BlueskyFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg"),
                                    fullsize = BlueskyFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg"),
                                    alt = "A croissant",
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 512)
        RenderTestUtils.compareRendering("BlueskyFrame", "QuotedPost", frame)
    }

    @Test
    fun testError() {
        val post = BlueskyFrame.fromError("Record not found")

        val frame = BlueskyFrame(post.asOneTimePublisher(), timezone = ZoneId.of("Europe/London"))
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("BlueskyFrame", "Errors", frame)
    }
}
