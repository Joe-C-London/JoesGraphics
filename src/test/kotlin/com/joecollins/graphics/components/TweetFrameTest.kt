package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import org.mockito.Mockito
import twitter4j.HashtagEntity
import twitter4j.MediaEntity
import twitter4j.Status
import twitter4j.URLEntity
import twitter4j.User
import twitter4j.UserMentionEntity
import java.awt.Dimension
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.Date
import java.util.Scanner

class TweetFrameTest {

    @Test
    fun testBasicTweet() {
        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(status.text).thenReturn("This tweet will test whether this frame renders.  It will be long enough to wrap to the next line.")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        Mockito.`when`(status.urlEntities).thenReturn(emptyArray())
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Basic", frame)
    }

    @Test
    fun testMultiLineTweet() {
        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(status.text).thenReturn("This tweet will test whether this frame renders.\n\nIt will contain deliberate line breaks.")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        Mockito.`when`(status.urlEntities).thenReturn(emptyArray())
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "MultiLine", frame)
    }

    @Test
    fun testVerifiedTweet() {
        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(true)
        Mockito.`when`(status.text).thenReturn("This tweet will test whether this frame renders.  It will be long enough to wrap to the next line.")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        Mockito.`when`(status.urlEntities).thenReturn(emptyArray())
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Verified", frame)
    }

    @Test
    fun testProtectedTweet() {
        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(user.isProtected).thenReturn(true)
        Mockito.`when`(status.text).thenReturn("This tweet should not be rendered in any way.")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        Mockito.`when`(status.urlEntities).thenReturn(emptyArray())
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Protected", frame)
    }

    @Test
    fun testMentionsAndHashtags() {
        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(status.text).thenReturn("Will @NicolaSturgeon win another term as FM?  Will @Douglas4Moray propel the Tories to a historic victory?  Or will @AnasSarwar lead Labour back to power?  Only three weeks to the #ScotParl election!  #Election2021")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        val userMentions = arrayOf(
            run {
                val userMention = Mockito.mock(UserMentionEntity::class.java)
                Mockito.`when`(userMention.screenName).thenReturn("NicolaSturgeon")
                Mockito.`when`(userMention.name).thenReturn("Nicola Sturgeon")
                Mockito.`when`(userMention.start).thenReturn(5)
                Mockito.`when`(userMention.end).thenReturn(20)
                Mockito.`when`(userMention.text).thenReturn("NicolaSturgeon")
                userMention
            },
            run {
                val userMention = Mockito.mock(UserMentionEntity::class.java)
                Mockito.`when`(userMention.screenName).thenReturn("Douglas4Moray")
                Mockito.`when`(userMention.name).thenReturn("Douglas Ross")
                Mockito.`when`(userMention.start).thenReturn(51)
                Mockito.`when`(userMention.end).thenReturn(65)
                Mockito.`when`(userMention.text).thenReturn("Douglas4Moray")
                userMention
            },
            run {
                val userMention = Mockito.mock(UserMentionEntity::class.java)
                Mockito.`when`(userMention.screenName).thenReturn("AnasSarwar")
                Mockito.`when`(userMention.name).thenReturn("Anas Sarwar")
                Mockito.`when`(userMention.start).thenReturn(116)
                Mockito.`when`(userMention.end).thenReturn(127)
                Mockito.`when`(userMention.text).thenReturn("AnasSarwar")
                userMention
            }
        )
        Mockito.`when`(status.userMentionEntities).thenReturn(userMentions)
        Mockito.`when`(status.urlEntities).thenReturn(emptyArray())
        val hashTags = arrayOf(
            run {
                val hashtag = Mockito.mock(HashtagEntity::class.java)
                Mockito.`when`(hashtag.start).thenReturn(180)
                Mockito.`when`(hashtag.end).thenReturn(189)
                Mockito.`when`(hashtag.text).thenReturn("ScotParl")
                hashtag
            },
            run {
                val hashtag = Mockito.mock(HashtagEntity::class.java)
                Mockito.`when`(hashtag.start).thenReturn(201)
                Mockito.`when`(hashtag.end).thenReturn(214)
                Mockito.`when`(hashtag.text).thenReturn("Election2021")
                hashtag
            }
        )
        Mockito.`when`(status.hashtagEntities).thenReturn(hashTags)
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "MentionsAndHashtags", frame)
    }

    @Test
    fun testLinks() {
        val imageFile = File.createTempFile("croissant", ".jpg")
        imageFile.deleteOnExit()
        Files.copy(javaClass.classLoader.getResourceAsStream("com/joecollins/graphics/twitter-inputs/croissant.jpg"), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        val htmlFile = File.createTempFile("head", ".html")
        val htmlString = run {
            val scanner = Scanner(javaClass.classLoader.getResourceAsStream("com/joecollins/graphics/twitter-inputs/head.html"))
            scanner.useDelimiter("\\A")
            scanner.next()
        }.replace("\${imageurl}", imageFile.toURI().toURL().toString())
        Files.write(htmlFile.toPath(), htmlString.toByteArray())
        htmlFile.deleteOnExit()

        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(status.text).thenReturn("Go to https://t.co/XRFkC6ITop for an amusing story.")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        val urlEntities = arrayOf(
            run {
                val urlEntity = Mockito.mock(URLEntity::class.java)
                Mockito.`when`(urlEntity.url).thenReturn("https://t.co/XRFkC6ITop")
                Mockito.`when`(urlEntity.text).thenReturn("https://t.co/XRFkC6ITop")
                Mockito.`when`(urlEntity.expandedURL).thenReturn(htmlFile.toURI().toURL().toString())
                Mockito.`when`(urlEntity.displayURL).thenReturn("bbc.co.uk/news/world-eur…")
                Mockito.`when`(urlEntity.start).thenReturn(6)
                Mockito.`when`(urlEntity.end).thenReturn(29)
                urlEntity
            }
        )
        Mockito.`when`(status.urlEntities).thenReturn(urlEntities)
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 512)
        RenderTestUtils.compareRendering("TweetFrame", "Links", frame, 15)
    }

    @Test
    fun testImages() {
        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(status.text).thenReturn("We should be able to render an image or two.\n\nLook!  A croissant!  https://t.co/VTphhEAioO")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        Mockito.`when`(status.urlEntities).thenReturn(emptyArray())
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        val images = arrayOf(
            run {
                val entity = Mockito.mock(MediaEntity::class.java)
                Mockito.`when`(entity.mediaURL).thenReturn(
                    TweetFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")
                        .toString()
                )
                Mockito.`when`(entity.displayURL).thenReturn("https://t.co/VTphhEAioO")
                Mockito.`when`(entity.type).thenReturn("photo")
                Mockito.`when`(entity.start).thenReturn(69)
                Mockito.`when`(entity.end).thenReturn(92)
                entity
            }
        )
        Mockito.`when`(status.mediaEntities).thenReturn(images)
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 512)
        RenderTestUtils.compareRendering("TweetFrame", "Images", frame)
    }

    @Test
    fun testQuoteTweet() {
        val quotedStatus = Mockito.mock(Status::class.java)
        val quotedUser = Mockito.mock(User::class.java)
        Mockito.`when`(quotedStatus.id).thenReturn(123456789)
        Mockito.`when`(quotedStatus.user).thenReturn(quotedUser)
        Mockito.`when`(quotedUser.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(quotedUser.name).thenReturn("Joe C")
        Mockito.`when`(quotedUser.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(quotedUser.isVerified).thenReturn(true)
        Mockito.`when`(quotedStatus.text).thenReturn("We should be able to render an image or two.\n\nLook!  A croissant!  https://t.co/VTphhEAioO")
        Mockito.`when`(quotedStatus.createdAt).thenReturn(Date.from(Instant.parse("2021-04-15T21:34:17Z")))
        Mockito.`when`(quotedStatus.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(quotedStatus.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(quotedStatus.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(quotedStatus.contributors).thenReturn(LongArray(0))
        Mockito.`when`(quotedStatus.userMentionEntities).thenReturn(emptyArray())
        Mockito.`when`(quotedStatus.urlEntities).thenReturn(emptyArray())
        Mockito.`when`(quotedStatus.hashtagEntities).thenReturn(emptyArray())
        val images = arrayOf(
            run {
                val entity = Mockito.mock(MediaEntity::class.java)
                Mockito.`when`(entity.mediaURL).thenReturn(
                    TweetFrameTest::class.java.classLoader.getResource("com/joecollins/graphics/twitter-inputs/croissant.jpg")
                        .toString()
                )
                Mockito.`when`(entity.displayURL).thenReturn("https://t.co/VTphhEAioO")
                Mockito.`when`(entity.type).thenReturn("photo")
                Mockito.`when`(entity.start).thenReturn(69)
                Mockito.`when`(entity.end).thenReturn(92)
                entity
            }
        )
        Mockito.`when`(quotedStatus.mediaEntities).thenReturn(images)
        Mockito.`when`(quotedStatus.symbolEntities).thenReturn(emptyArray())

        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Frenchman")
        Mockito.`when`(user.name).thenReturn("A Frenchman")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/tower.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(status.text).thenReturn("Amateurs!  https://t.co/zgM6sqOuwW")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2021-04-19T08:24:53+01:00")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        val quotedUrl = arrayOf(
            run {
                val url = Mockito.mock(URLEntity::class.java)
                Mockito.`when`(url.url).thenReturn("https://t.co/zgM6sqOuwW")
                Mockito.`when`(url.expandedURL).thenReturn("https://twitter.com/Joe_C_London/status/123456789")
                Mockito.`when`(url.displayURL).thenReturn("twitter.com/Joe_C_Lond…")
                Mockito.`when`(url.start).thenReturn(11)
                Mockito.`when`(url.end).thenReturn(34)
                url
            }
        )
        Mockito.`when`(status.urlEntities).thenReturn(quotedUrl)
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())
        Mockito.`when`(status.quotedStatus).thenReturn(quotedStatus)
        Mockito.`when`(status.quotedStatusId).thenReturn(123456789)

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 512)
        RenderTestUtils.compareRendering("TweetFrame", "QuoteTweet", frame)
    }

    @Test
    fun testTweetWithEmoji() {
        val status = Mockito.mock(Status::class.java)
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(status.user).thenReturn(user)
        Mockito.`when`(user.screenName).thenReturn("Joe_C_London")
        Mockito.`when`(user.name).thenReturn("Joe C")
        Mockito.`when`(user.profileImageURL).thenReturn(javaClass.classLoader.getResource("com/joecollins/graphics/twitter-inputs/letter-j.png")!!.toString())
        Mockito.`when`(user.isVerified).thenReturn(false)
        Mockito.`when`(status.text).thenReturn("How do you get emojis to appear in your tweet? \uD83E\uDDF5")
        Mockito.`when`(status.createdAt).thenReturn(Date.from(Instant.parse("2022-07-21T19:26:57Z")))
        Mockito.`when`(status.inReplyToStatusId).thenReturn(-1)
        Mockito.`when`(status.inReplyToUserId).thenReturn(-1)
        Mockito.`when`(status.inReplyToScreenName).thenReturn("null")
        Mockito.`when`(status.contributors).thenReturn(LongArray(0))
        Mockito.`when`(status.userMentionEntities).thenReturn(emptyArray())
        Mockito.`when`(status.urlEntities).thenReturn(emptyArray())
        Mockito.`when`(status.hashtagEntities).thenReturn(emptyArray())
        Mockito.`when`(status.mediaEntities).thenReturn(emptyArray())
        Mockito.`when`(status.symbolEntities).thenReturn(emptyArray())

        val frame = TweetFrame(status.asOneTimePublisher())
        frame.size = Dimension(512, 256)
        RenderTestUtils.compareRendering("TweetFrame", "Emoji", frame)
    }
}
