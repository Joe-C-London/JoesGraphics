package com.joecollins.models.general.social.bluesky

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.apache.hc.client5.http.ClientProtocolException
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.StringEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.PrintWriter
import java.util.Properties
import javax.swing.JOptionPane
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object BlueSkyInstance {

    private val tokensFile = File(System.getProperty("user.home") + "/.politics.bluesky")
    private val blueskyPropertiesFile = this::class.java.classLoader.getResourceAsStream("bluesky.properties")
        ?: throw IllegalStateException("Unable to find bluesky.properties")

    private val blueskyProperties = Properties().apply {
        load(blueskyPropertiesFile)
    }

    val server = blueskyProperties["instance.server"]

    val did: String get() {
        lateinit var ret: String
        execute(
            {
                HttpGet("https://$server/xrpc/com.atproto.server.getSession")
            },
            { res ->
                ret = res.asJsonObject["did"].asString
            },
        )
        return ret
    }

    fun execute(request: () -> HttpUriRequestBase) {
        execute(request) { }
    }

    @OptIn(ExperimentalContracts::class)
    fun <T> execute(request: () -> HttpUriRequestBase, response: (JsonElement) -> T): T {
        contract {
            callsInPlace(request, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(response, InvocationKind.AT_MOST_ONCE)
        }

        if (!tokensFile.exists()) {
            login()
        }

        data class Wrapper<T>(val item: T)

        val accessToken = Properties().apply { load(FileReader(tokensFile)) }.getProperty("accessJwt")
        val client = HttpClients.createDefault()

        val req = request().apply { addHeader("Authorization", "Bearer $accessToken") }
        val (newToken, returnValue) = client.execute(req) { res ->
            val jsonString = String(res.entity.content.readAllBytes())
            val json = JsonParser.parseString(jsonString).asJsonObject
            if (res.code == 400 && json.asJsonObject["error"].asString == "ExpiredToken") {
                refreshToken() to null
            } else if (res.code != 200) {
                throw ClientProtocolException(json.toString())
            } else {
                null to Wrapper(response(json))
            }
        }
        if (returnValue != null) return returnValue.item

        val retried = request().apply { addHeader("Authorization", "Bearer $newToken") }
        return client.execute(retried) { res ->
            val jsonString = String(res.entity.content.readAllBytes())
            val json = JsonParser.parseString(jsonString).asJsonObject
            if (res.code != 200) {
                throw ClientProtocolException(json.toString())
            }

            response(json)
        }
    }

    private fun login() {
        val username = JOptionPane.showInputDialog("Enter user name:")
        val password = JOptionPane.showInputDialog("Enter password:")

        val client = HttpClients.createDefault()
        val post = HttpPost("https://$server/xrpc/com.atproto.server.createSession")
        post.addHeader("Content-Type", "application/json")
        post.entity = StringEntity("{\"identifier\": \"$username\", \"password\": \"$password\"}", ContentType.APPLICATION_JSON)
        client.execute(post) { response ->
            val entity = response.entity
            val jsonString = String(entity.content.readAllBytes())
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            if (response.code != 200) {
                throw ClientProtocolException(jsonObject.toString())
            }
            saveTokens(jsonObject["accessJwt"].asString, jsonObject["refreshJwt"].asString)
        }
    }

    private fun refreshToken(): String {
        val tokens = Properties().apply { load(FileInputStream(tokensFile)) }
        val refreshToken = tokens["refreshJwt"]!!

        val client = HttpClients.createDefault()
        val post = HttpPost("https://$server/xrpc/com.atproto.server.refreshSession")
        post.addHeader("Content-Type", "application/json")
        post.addHeader("Authorization", "Bearer $refreshToken")
        return client.execute(post) { response ->
            val entity = response.entity
            val jsonString = String(entity.content.readAllBytes())
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            if (response.code != 200) {
                throw ClientProtocolException(jsonObject.toString())
            }
            saveTokens(jsonObject["accessJwt"].asString, jsonObject["refreshJwt"].asString)
            jsonObject["accessJwt"].asString
        }
    }

    private fun saveTokens(accessToken: String, refreshToken: String) {
        PrintWriter(tokensFile).use { pw ->
            pw.println("accessJwt=$accessToken")
            pw.println("refreshJwt=$refreshToken")
        }
        println("Tokens saved to $tokensFile")
    }

    @JvmStatic fun main(args: Array<String>) {
        execute({
            HttpGet("https://$server/xrpc/com.atproto.server.getSession")
        }, { json ->
            println(json)
        })
    }
}
