package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.models.general.social.twitter.TweetLoader
import com.joecollins.pubsub.asOneTimePublisher
import com.twitter.clientlib.api.TweetsApi
import com.twitter.clientlib.api.TweetsApi.APIfindTweetByIdRequest
import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.api.UsersApi
import com.twitter.clientlib.api.UsersApi.APIfindUserByIdRequest
import com.twitter.clientlib.model.Expansions
import com.twitter.clientlib.model.FullTextEntities
import com.twitter.clientlib.model.Get2TweetsIdResponse
import com.twitter.clientlib.model.Get2UsersIdResponse
import com.twitter.clientlib.model.HashtagEntity
import com.twitter.clientlib.model.Media
import com.twitter.clientlib.model.MentionEntity
import com.twitter.clientlib.model.Photo
import com.twitter.clientlib.model.Poll
import com.twitter.clientlib.model.PollOption
import com.twitter.clientlib.model.ResourceUnauthorizedProblem
import com.twitter.clientlib.model.Tweet
import com.twitter.clientlib.model.TweetAttachments
import com.twitter.clientlib.model.TweetReferencedTweets
import com.twitter.clientlib.model.UrlEntity
import com.twitter.clientlib.model.UrlImage
import com.twitter.clientlib.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.awt.Dimension
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.ZoneId
import java.util.Scanner

class TweetFrameTest {

    private lateinit var mockTwitter: TwitterApi
    private lateinit var mockTweetsApi: TweetsApi
    private lateinit var mockUsersApi: UsersApi

    @BeforeEach
    fun setup() {
        mockTwitter = Mockito.mock(TwitterApi::class.java)
        mockTweetsApi = Mockito.mock(TweetsApi::class.java)
        Mockito.`when`(mockTwitter.tweets()).thenReturn(mockTweetsApi)
        mockUsersApi = Mockito.mock(UsersApi::class.java)
        Mockito.`when`(mockTwitter.users()).thenReturn(mockUsersApi)
    }

    private fun setupTweetBuilder(id: String, authorId: String): TweetBuilder {
        val tweetBuilder = TweetBuilder(id, authorId)
        val mockTweetRequest =
            Mockito.mock(APIfindTweetByIdRequest::class.java)
        val mockTweetResponse = Mockito.mock(Get2TweetsIdResponse::class.java)
        Mockito.`when`(mockTweetsApi.findTweetById(id)).thenReturn(mockTweetRequest)
        Mockito.`when`(mockTweetRequest.expansions(Mockito.anySet())).thenAnswer { inv ->
            @Suppress("UNCHECKED_CAST")
            tweetBuilder.expansions = inv.arguments[0] as Set<String>
            inv.mock
        }
        Mockito.`when`(mockTweetRequest.tweetFields(Mockito.anySet())).thenAnswer { inv ->
            @Suppress("UNCHECKED_CAST")
            tweetBuilder.fields = inv.arguments[0] as Set<String>
            inv.mock
        }
        Mockito.`when`(mockTweetRequest.userFields(Mockito.anySet())).thenAnswer { inv ->
            @Suppress("UNCHECKED_CAST")
            tweetBuilder.user.fields = inv.arguments[0] as Set<String>
            inv.mock
        }
        Mockito.`when`(mockTweetRequest.mediaFields(Mockito.anySet())).thenAnswer { inv ->
            @Suppress("UNCHECKED_CAST")
            tweetBuilder.media.fields = inv.arguments[0] as Set<String>
            inv.mock
        }
        Mockito.`when`(mockTweetRequest.execute()).thenReturn(mockTweetResponse)
        Mockito.`when`(mockTweetResponse.data).thenAnswer { tweetBuilder.tweet }
        Mockito.`when`(mockTweetResponse.includes).thenAnswer { tweetBuilder.includes }
        return tweetBuilder
    }

    private fun setupUserBuilder(userId: String): UserBuilder {
        val userBuilder = UserBuilder(userId)
        val mockUserRequest =
            Mockito.mock(APIfindUserByIdRequest::class.java)
        val mockUserResponse = Mockito.mock(Get2UsersIdResponse::class.java)
        Mockito.`when`(mockUsersApi.findUserById(userId)).thenReturn(mockUserRequest)
        Mockito.`when`(mockUserRequest.userFields(Mockito.anySet())).thenAnswer { inv ->
            @Suppress("UNCHECKED_CAST")
            userBuilder.fields = inv.arguments[0] as Set<String>
            inv.mock
        }
        Mockito.`when`(mockUserRequest.execute()).thenReturn(mockUserResponse)
        Mockito.`when`(mockUserResponse.data).thenAnswer { userBuilder.user }
        return userBuilder
    }

    private class TweetBuilder(private val id: String, private val authorId: String) {
        private val possibleFields = "attachments,author_id,context_annotations,conversation_id,created_at,edit_controls,edit_history_tweet_ids,entities,geo,id,in_reply_to_user_id,lang,non_public_metrics,organic_metrics,possibly_sensitive,promoted_metrics,public_metrics,referenced_tweets,reply_settings,source,text,withheld"
            .split(",").toSet()

        private val possibleExpansions = "author_id,referenced_tweets.id,referenced_tweets.id.author_id,entities.mentions.username,attachments.poll_ids,attachments.media_keys,in_reply_to_user_id,geo.place_id,edit_history_tweet_ids"
            .split(",").toSet()

        val tweet: Tweet
            get() {
                val ret = Tweet()
                ret.id = id
                ret.editHistoryTweetIds = listOf(id)
                ret.text = text
                if (fields.contains("author_id") || expansions.contains("author_id")) {
                    ret.authorId = authorId
                }
                if (fields.contains("created_at")) {
                    ret.createdAt = createdAt.atZone(ZoneId.systemDefault()).toOffsetDateTime()
                }
                if (fields.contains("entities")) {
                    val entities = FullTextEntities()
                    entities.hashtags = hashtags
                    entities.mentions = mentions
                    entities.urls = urls
                    ret.entities = entities
                }
                if (fields.contains("referenced_tweets") || expansions.any { it.startsWith("referenced_tweets") }) {
                    ret.referencedTweets = referencedTweets
                }
                if (fields.contains("attachments") || expansions.any { it.startsWith("attachments") }) {
                    ret.attachments = TweetAttachments().also {
                        if (media.mediaKey != null) {
                            it.mediaKeys = listOf(media.mediaKey!!)
                        }
                    }
                }
                return ret
            }

        val includes: Expansions?
            get() {
                if (expansions.isEmpty()) return null
                val ret = Expansions()
                if (expansions.contains("author_id")) {
                    ret.users = listOf(user.user)
                }
                if (expansions.contains("attachments.media_keys")) {
                    ret.media = media.media
                }
                if (expansions.contains("attachments.poll_ids")) {
                    ret.polls = poll?.let { listOf(it) } ?: emptyList()
                }
                return ret
            }

        var fields: Set<String> = emptySet()
            set(value) {
                val invalid = value.filter { !possibleFields.contains(it) }
                if (invalid.isNotEmpty()) {
                    throw IllegalArgumentException("Unrecognised fields: $invalid")
                }
                field = value
            }

        var expansions: Set<String> = emptySet()
            set(value) {
                val invalid = value.filter { !possibleExpansions.contains(it) }
                if (invalid.isNotEmpty()) {
                    throw IllegalArgumentException("Unrecognised fields: $invalid")
                }
                field = value
            }

        lateinit var text: String
        lateinit var createdAt: Instant
        var hashtags: List<HashtagEntity>? = null
        var mentions: List<MentionEntity>? = null
        var urls: List<UrlEntity>? = null
        var referencedTweets: List<TweetReferencedTweets>? = null

        var user = UserBuilder(authorId)
        var media = MediaBuilder()

        var poll: Poll? = null
    }

    private class UserBuilder(private val id: String) {
        private val possibleFields = "created_at,description,entities,id,location,name,pinned_tweet_id,profile_image_url,protected,public_metrics,url,username,verified,withheld"
            .split(",").toSet()

        val user: User
            get() {
                val ret = User()
                ret.id = id
                ret.name = name
                ret.username = username
                if (fields.contains("profile_image_url")) {
                    ret.profileImageUrl = profileImageUrl
                }
                if (fields.contains("protected")) {
                    ret.protected = protected
                }
                if (fields.contains("verified")) {
                    ret.verified = verified
                }
                return ret
            }

        var fields: Set<String> = emptySet()
            set(value) {
                val invalid = value.filter { !possibleFields.contains(it) }
                if (invalid.isNotEmpty()) {
                    throw IllegalArgumentException("Unrecognised fields: $invalid")
                }
                field = value
            }

        lateinit var username: String
        lateinit var name: String
        lateinit var profileImageUrl: URL
        var protected = false
        var verified = false
    }

    private class MediaBuilder() {
        private val possibleFields = "alt_text,duration_ms,height,media_key,non_public_metrics,organic_metrics,preview_image_url,promoted_metrics,public_metrics,type,url,variants,width"
            .split(",").toSet()

        var fields: Set<String> = emptySet()
            set(value) {
                val invalid = value.filter { !possibleFields.contains(it) }
                if (invalid.isNotEmpty()) {
                    throw IllegalArgumentException("Unrecognised fields: $invalid")
                }
                field = value
            }

        val media: List<Media>
            get() {
                if (mediaKey == null) return emptyList()
                val media: Media = when (type) {
                    "photo" -> Photo().also {
                        if (fields.contains("url")) {
                            it.url = url
                        }
                    }
                    else -> return emptyList()
                }
                media.mediaKey = mediaKey
                media.type = type
                return listOf(media)
            }

        var mediaKey: String? = null
        lateinit var type: String
        lateinit var url: URL
    }

    @Test
    fun testBasicTweet() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "This tweet will test whether this frame renders.  It will be long enough to wrap to the next line."
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Basic", frame)
    }

    @Test
    fun testMultiLineTweet() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "This tweet will test whether this frame renders.\n\nIt will contain deliberate line breaks."
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "MultiLine", frame)
    }

    @Test
    fun testVerifiedTweetMeansNothing() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "This tweet will test whether this frame renders.  It will be long enough to wrap to the next line."
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!
        userBuilder.verified = true

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Verified", frame)
    }

    @Test
    fun testProtectedTweet() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "This tweet should not be rendered in any way."
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!
        userBuilder.protected = true

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Protected", frame)
    }

    @Test
    fun testMentionsAndHashtags() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "Will @NicolaSturgeon win another term as FM?  Will @Douglas4Moray propel the Tories to a historic victory?  Or will @AnasSarwar lead Labour back to power?  Only three weeks to the #ScotParl election!  #Election2021"
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")
        tweetBuilder.hashtags = listOf(
            HashtagEntity().also {
                it.start = 180
                it.end = 189
                it.tag = "ScotParl"
            },
            HashtagEntity().also {
                it.start = 201
                it.end = 214
                it.tag = "Election2021"
            },
        )
        tweetBuilder.mentions = listOf(
            MentionEntity().also {
                it.start = 5
                it.end = 20
                it.id = "111"
                it.username = "NicolaSturgeon"
            },
            MentionEntity().also {
                it.start = 51
                it.end = 65
                it.id = "222"
                it.username = "Douglas4Moray"
            },
            MentionEntity().also {
                it.start = 116
                it.end = 127
                it.id = "333"
                it.username = "AnasSarwar"
            },
        )

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "MentionsAndHashtags", frame)
    }

    @Test
    fun testLinks() {
        val imageFile = File.createTempFile("croissant", ".jpg")
        imageFile.deleteOnExit()
        Files.copy(
            javaClass.classLoader.getResourceAsStream("com/joecollins/graphics/twitter-inputs/croissant.jpg"),
            imageFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
        val htmlFile = File.createTempFile("head", ".html")
        val htmlString = run {
            val scanner =
                Scanner(javaClass.classLoader.getResourceAsStream("com/joecollins/graphics/twitter-inputs/head.html"))
            scanner.useDelimiter("\\A")
            scanner.next()
        }.replace("\${imageurl}", imageFile.toURI().toURL().toString())
        Files.write(htmlFile.toPath(), htmlString.toByteArray())
        htmlFile.deleteOnExit()

        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "Go to https://t.co/XRFkC6ITop for an amusing story."
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")
        tweetBuilder.urls = listOf(
            UrlEntity().also {
                it.start = 6
                it.end = 29
                it.description = "Polish animal welfare officers responding to a call discover the creature is in fact a pastry."
                it.displayUrl = "bbc.co.uk/news/world-eur…"
                it.expandedUrl = URL("https://t.co/XRFkC6ITop")

                it.images = listOf(
                    UrlImage().also { img ->
                        img.url = imageFile.toURI().toURL()
                        img.height = 576
                        img.width = 1024
                    },
                )
                it.status = 200
                it.title = "Mystery tree beast turns out to be croissant"
                it.unwoundUrl = URL("https://www.bbc.co.uk/news/world-europe-56757956")
                it.url = URL("https://t.co/XRFkC6ITop")
            },
        )

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 512)
        RenderTestUtils.compareRendering("TweetFrame", "Links", frame)
    }

    @Test
    fun testImages() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "We should be able to render an image or two.\n\nLook!  A croissant!  https://t.co/VTphhEAioO"
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")
        tweetBuilder.urls = listOf(
            UrlEntity().also {
                it.start = 69
                it.end = 92
                it.displayUrl = "pic.twitter.com/VTphhEAioO"
                it.url = URL("https://t.co/VTphhEAioO")
                it.expandedUrl = URL("https://twitter.com/Joe_C_London/status/123/photo/1")
                it.mediaKey = "3_456"
            },
        )
        tweetBuilder.media.mediaKey = "3_456"
        tweetBuilder.media.type = "photo"
        tweetBuilder.media.url = TweetFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 512)
        RenderTestUtils.compareRendering("TweetFrame", "Images", frame)
    }

    @Test
    fun testQuoteTweet() {
        val tweetBuilder = setupTweetBuilder("321", "654")
        tweetBuilder.text = "Amateurs!  https://t.co/zgM6sqOuwW"
        tweetBuilder.createdAt = Instant.parse("2021-04-19T08:24:53+01:00")
        tweetBuilder.urls = listOf(
            UrlEntity().also {
                it.start = 11
                it.end = 34
                it.displayUrl = "twitter.com/Joe_C_London/st…"
                it.url = URL("https://t.co/zgM6sqOuwW")
                it.expandedUrl = URL("https://twitter.com/Joe_C_London/status/123")
            },
        )
        tweetBuilder.referencedTweets = listOf(
            TweetReferencedTweets().also {
                it.id = "123"
                it.type = TweetReferencedTweets.TypeEnum.QUOTED
            },
        )

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Frenchman"
        userBuilder.name = "A Frenchman"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/tower.png")

        val quotedTweetBuilder = setupTweetBuilder("123", "456")
        quotedTweetBuilder.text = "We should be able to render an image or two.\n\nLook!  A croissant!  https://t.co/VTphhEAioO"
        quotedTweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")
        quotedTweetBuilder.urls = listOf(
            UrlEntity().also {
                it.start = 69
                it.end = 92
                it.displayUrl = "pic.twitter.com/VTphhEAioO"
                it.url = URL("https://t.co/VTphhEAioO")
                it.expandedUrl = TweetFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")
                it.mediaKey = "3_456"
            },
        )
        quotedTweetBuilder.media.mediaKey = "3_456"
        quotedTweetBuilder.media.type = "photo"
        quotedTweetBuilder.media.url = TweetFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")

        val quotedUserBuilder = quotedTweetBuilder.user
        quotedUserBuilder.username = "Joe_C_London"
        quotedUserBuilder.name = "Joe C"
        quotedUserBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!
        quotedUserBuilder.verified = true

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(321L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 512)
        RenderTestUtils.compareRendering("TweetFrame", "QuoteTweet", frame)
    }

    @Test
    fun testTweetWithEmoji() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "How do you get emojis to appear in your tweet? \uD83E\uDDF5"
        tweetBuilder.createdAt = Instant.parse("2022-07-21T19:26:57Z")

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Emoji", frame, 15)
    }

    @Test
    fun testErrors() {
        val mockTweetRequest =
            Mockito.mock(APIfindTweetByIdRequest::class.java)
        val mockTweetResponse = Mockito.mock(Get2TweetsIdResponse::class.java)
        Mockito.`when`(mockTweetsApi.findTweetById("123")).thenReturn(mockTweetRequest)
        Mockito.`when`(mockTweetRequest.expansions(Mockito.anySet())).thenReturn(mockTweetRequest)
        Mockito.`when`(mockTweetRequest.tweetFields(Mockito.anySet())).thenReturn(mockTweetRequest)
        Mockito.`when`(mockTweetRequest.userFields(Mockito.anySet())).thenReturn(mockTweetRequest)
        Mockito.`when`(mockTweetRequest.mediaFields(Mockito.anySet())).thenReturn(mockTweetRequest)
        Mockito.`when`(mockTweetRequest.execute()).thenReturn(mockTweetResponse)
        Mockito.`when`(mockTweetResponse.errors).thenAnswer {
            val error = ResourceUnauthorizedProblem()
            error.parameter = "id"
            error.value = "123"
            error.resourceId = "123"
            error.resourceType = ResourceUnauthorizedProblem.ResourceTypeEnum.TWEET
            error.section = ResourceUnauthorizedProblem.SectionEnum.DATA
            error.detail = "Sorry, you are not authorized to see the Tweet with id: [123]."
            error.title = "Authorization Error"
            error.type = "https://api.twitter.com/2/problems/not-authorized-for-resource"
            listOf(error)
        }

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Errors", frame)
    }

    @Test
    fun testPoll() {
        val tweetBuilder = setupTweetBuilder("123", "456")
        tweetBuilder.text = "Is this something that should render?"
        tweetBuilder.createdAt = Instant.parse("2021-04-15T21:34:17Z")
        tweetBuilder.poll = Poll().also { poll ->
            poll.options = listOf(
                PollOption().also { it.label = "Yes"; it.votes = 100; it.position = 1 },
                PollOption().also { it.label = "No"; it.votes = 150; it.position = 2 },
            )
        }

        val userBuilder = tweetBuilder.user
        userBuilder.username = "Joe_C_London"
        userBuilder.name = "Joe C"
        userBuilder.profileImageUrl = javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!

        val frame = TweetFrame(
            TweetLoader.loadTweetV2(123L, mockTwitter).asOneTimePublisher(),
        )
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Poll", frame)
    }
}
