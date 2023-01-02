package com.joecollins.models.general.social.twitter

import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse
import com.google.gson.JsonParser
import com.twitter.clientlib.ApiClientCallback
import com.twitter.clientlib.ApiException
import com.twitter.clientlib.TwitterCredentialsOAuth2
import com.twitter.clientlib.api.TwitterApi
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.message.BasicNameValuePair
import java.awt.Desktop
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.URI
import java.util.Base64
import java.util.Properties
import javax.swing.JOptionPane
import kotlin.random.Random

object TwitterV2InstanceFactory {

    private val tokensFile = File(System.getProperty("user.home") + "/.politics.twitter")
    private val twitterPropertiesFile = this::class.java.classLoader.getResourceAsStream("twitter.properties")
        ?: throw IllegalStateException("Unable to find twitter.properties")

    val instance: TwitterApi

    init {
        instance = createInstance(false)
    }

    private fun createInstance(isRetry: Boolean): TwitterApi {
        synchronized(this) {
            if (!tokensFile.exists()) {
                authenticate()
            }
            val properties = Properties()
            properties.load(twitterPropertiesFile)
            properties.load(FileReader(tokensFile))
            val instance = TwitterApi(
                TwitterCredentialsOAuth2(
                    properties["oauth2ClientId"].toString(),
                    properties["oauth2ClientSecret"].toString(),
                    properties["oauth2AccessToken"].toString(),
                    properties["oauth2RefreshToken"].toString(),
                    true
                )
            )
            instance.addCallback(object : ApiClientCallback {
                override fun onAfterRefreshToken(accessToken: OAuth2AccessToken) {
                    saveTokens(accessToken.accessToken, accessToken.refreshToken)
                }
            })
            try {
                println("Logged in as ${instance.users().findMyUser().execute().data?.username}")
            } catch (e: ApiException) {
                if (e.cause is OAuth2AccessTokenErrorResponse && !isRetry) {
                    tokensFile.delete()
                    return createInstance(true)
                } else {
                    throw e
                }
            }
            return instance
        }
    }

    private fun authenticate() {
        val properties = Properties()
        properties.load(twitterPropertiesFile)
        val challenge = Base64.getEncoder().encodeToString(Random.Default.nextBytes(16))
        val clientId = properties["oauth2ClientId"].toString()
        val redirectUri = properties["oauth2RedirectUri"].toString()
        val authUrl = "https://twitter.com/i/oauth2/authorize?response_type=code&client_id=$clientId&redirect_uri=$redirectUri&scope=tweet.read+tweet.write+users.read+offline.access&state=state&code_challenge=$challenge&code_challenge_method=plain"
        Desktop.getDesktop().browse(URI(authUrl))
        val code = JOptionPane.showInputDialog("Please enter the authentication code")

        val client = HttpClients.createDefault()
        val post = HttpPost("https://api.twitter.com/2/oauth2/token")
        post.addHeader("Content-Type", "application/x-www-form-urlencoded")
        post.entity = UrlEncodedFormEntity(
            listOf(
                BasicNameValuePair("code", code),
                BasicNameValuePair("grant_type", "authorization_code"),
                BasicNameValuePair("client_id", clientId),
                BasicNameValuePair("redirect_uri", redirectUri),
                BasicNameValuePair("code_verifier", challenge)
            )
        )
        client.execute(post) { response ->
            val entity = response.entity
            val jsonObject = JsonParser.parseReader(InputStreamReader(entity.content)).asJsonObject
            saveTokens(jsonObject["access_token"].asString, jsonObject["refresh_token"].asString)
        }
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        PrintWriter(tokensFile).use { pw ->
            pw.println("oauth2AccessToken=$accessToken")
            pw.println("oauth2RefreshToken=$refreshToken")
        }
        println("Tokens saved to $tokensFile")
    }
}
