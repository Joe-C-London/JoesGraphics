package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.models.general.social.mastodon.Card
import com.joecollins.models.general.social.mastodon.Emoji
import com.joecollins.models.general.social.mastodon.MediaAttachment
import com.joecollins.models.general.social.mastodon.Mention
import com.joecollins.models.general.social.mastodon.Poll
import com.joecollins.models.general.social.mastodon.Tag
import com.joecollins.models.general.social.mastodon.Toot
import com.joecollins.models.general.social.mastodon.User
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.Dimension
import java.net.URL
import java.time.Instant
import org.junit.jupiter.api.Test

class MastodonFrameTest {

    @Test
    fun testBasicToot() {
        val toot = Toot(
            content = "<p>This tweet will test whether this frame renders.  It will be long enough to wrap to the next line.</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123")
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Basic", frame)
    }

    @Test
    fun testBasicTootDifferentServer() {
        val toot = Toot(
            content = "<p>This tweet will test whether this frame renders.  It will be long enough to wrap to the next line.</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London@mastodon.world",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123")
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Basic", frame)
    }

    @Test
    fun testMultiLineTweet() {
        val toot = Toot(
            content = "<p>This tweet will test whether this frame renders.</p><p>It will contain deliberate line breaks.</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123")
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "MultiLine", frame)
    }

    @Test
    fun testMentionsAndHashtags() {
        val toot = Toot(
            content = "<p>Will <span class=\"h-card\"><a href=\"https://mastodon.snp.scot/@NicolaSturgeon\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@<span>NicolaSturgeon</span></a></span> win another term as FM?  Will <span class=\"h-card\"><a href=\"https://mastodon.tory.scot/@Douglas4Moray\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@<span>Douglas4Moray</span></a></span> propel the Tories to a historic victory?  Or will <span class=\"h-card\"><a href=\"https://mastodon.labor.scot/@AnasSarwar\" class=\"u-url mention\" rel=\"nofollow noopener noreferrer\" target=\"_blank\">@<span>AnasSarwar</span></a></span> lead Labour back to power?  Only three weeks to the <a href=\"https://mastodon.world/tags/ScotParl\" class=\"mention hashtag\" rel=\"tag\">#<span>ScotParl</span></a> election!  <a href=\"https://mastodon.world/tags/Election2021\" class=\"mention hashtag\" rel=\"tag\">#<span>Election2021</span></a></p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123"),
            mentions = listOf(
                Mention(
                    "NicolaSturgeon",
                    URL("https://mastodon.snp.scot/@NicolaSturgeon"),
                    "NicolaSturgeon@mastodon.snp.scot"
                ),
                Mention(
                    "Douglas4Moray",
                    URL("https://mastodon.tory.scot/@Douglas4Moray"),
                    "Douglas4Moray@mastodon.tory.scot"
                ),
                Mention(
                    "AnasSarwar",
                    URL("https://mastodon.labour.scot/@AnasSarwar"),
                    "AnasSarwar@mastodon.labour.scot"
                )
            ),
            tags = listOf(
                Tag(
                    "scotparl",
                    URL("https://mastodon.social/tags/scotparl")
                ),
                Tag(
                    "election2021",
                    URL("https://mastodon.social/tags/election2021")
                )
            )
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "MentionsAndHashtags", frame)
    }

    @Test
    fun testLinks() {
        val toot = Toot(
            content = "<p>Go to <a href=\"https://www.bbc.co.uk/news/world-europe-56757956\" rel=\"nofollow noopener noreferrer\" target=\"_blank\"><span class=\"invisible\">https://</span><span class=\"ellipsis\">www.bbc.co.uk/news/world-europ</span><span class=\"invisible\">e-56757956</span></a> for an amusing story.</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123"),
            card = Card(
                url = URL("https://www.bbc.co.uk/news/world-europe-56757956"),
                title = "Mystery tree beast turns out to be croissant",
                description = "Polish animal welfare officers responding to a call discover the creature is in fact a pastry.",
                imageURL = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")!!,
                providerName = "bbc.co.uk",
                type = "link"
            )
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Links", frame)
    }

    @Test
    fun testLinkWithoutCard() {
        val toot = Toot(
            content = "<p>Go to <a href=\"https://www.bbc.co.uk/news/world-europe-56757956\" rel=\"nofollow noopener noreferrer\" target=\"_blank\"><span class=\"invisible\">https://</span><span class=\"ellipsis\">www.bbc.co.uk/news/world-europ</span><span class=\"invisible\">e-56757956</span></a> for an amusing story.</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123")
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "LinksWithoutCard", frame)
    }

    @Test
    fun testImages() {
        val toot = Toot(
            content = "<p>We should be able to render an image or two.</p><p>Look!  A croissant!</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123"),
            mediaAttachments = listOf(
                MediaAttachment(
                    type = "image",
                    url = MastodonFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")
                )
            )
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Images", frame)
    }

    @Test
    fun testPostWithEmoji() {
        val toot = Toot(
            content = "<p>How do you get emojis to appear in your post? \uD83E\uDDF5</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123")
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Emoji", frame, 5)
    }

    @Test
    fun testPostWithCustomEmoji() {
        val toot = Toot(
            content = "<p>How do you get emojis to appear in your post? :thread:</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123"),
            emojis = listOf(
                Emoji(
                    shortcode = "thread",
                    url = URL("https://images.emojiterra.com/google/noto-emoji/v2.034/512px/1f9f5.png"),
                    staticUrl = URL("https://images.emojiterra.com/google/noto-emoji/v2.034/512px/1f9f5.png")
                )
            )
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Emoji", frame, 5)
    }

    @Test
    fun testError() {
        val toot = MastodonFrame.fromError("Record not found", URL("https://mastodon.world/api/v1/statuses/123"))

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Errors", frame)
    }

    @Test
    fun testPoll() {
        val toot = Toot(
            content = "<p>Should we render this poll?</p>",
            account = User(
                username = "Joe_C_London",
                acct = "Joe_C_London",
                displayName = "Joe C",
                avatar = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!,
                url = URL("https://mastodon.world/@Joe_C_London")
            ),
            createdAt = Instant.parse("2021-04-15T21:34:17Z"),
            url = URL("https://mastodon.world/@Joe_C_London/123"),
            poll = Poll(
                opts = listOf(
                    Poll.Option("Yes", 50),
                    Poll.Option("No", 100)
                )
            )
        )

        val frame = MastodonFrame(toot.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("MastodonFrame", "Poll", frame, 5)
    }
}