package com.joecollins.graphics.utils

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.net.URL
import kotlin.math.absoluteValue
import kotlin.math.sqrt

object HexJsonReader {

    private const val LENGTH = 10.0
    private const val GAP = 1.0

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class HexCoordinates(val q: Int, val r: Int)

    private enum class HexLayout(@JsonValue val value: String) {
        ODD_R("odd-r"),
        EVEN_R("even-r"),
        ODD_Q("odd-q"),
        EVEN_Q("even-q"),
    }

    private data class HexJsonFile(val layout: HexLayout, val hexes: Map<String, HexCoordinates>)

    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())

    private val ROOT_3 = sqrt(3.0)

    private val flatTop = Path2D.Double().also { path ->
        path.moveTo(LENGTH / 2, 0.0)
        path.lineTo(3 * LENGTH / 2, 0.0)
        path.lineTo(2 * LENGTH, ROOT_3 * LENGTH / 2)
        path.lineTo(3 * LENGTH / 2, ROOT_3 * LENGTH)
        path.lineTo(LENGTH / 2, ROOT_3 * LENGTH)
        path.lineTo(0.0, ROOT_3 * LENGTH / 2)
        path.closePath()
    }

    private val flatSide = Path2D.Double().also { path ->
        path.moveTo(0.0, LENGTH / 2)
        path.lineTo(0.0, 3 * LENGTH / 2)
        path.lineTo(ROOT_3 * LENGTH / 2, 2 * LENGTH)
        path.lineTo(ROOT_3 * LENGTH, 3 * LENGTH / 2)
        path.lineTo(ROOT_3 * LENGTH, LENGTH / 2)
        path.lineTo(ROOT_3 * LENGTH / 2, 0.0)
        path.closePath()
    }

    fun readHex(file: URL): Map<String, Shape> {
        val rootNode: HexJsonFile = objectMapper.readValue(file)
        return rootNode.hexes.mapValues { (_, hex) ->
            when (rootNode.layout) {
                HexLayout.ODD_R, HexLayout.EVEN_R -> run {
                    val shift = hex.r.absoluteValue % 2 == (if (rootNode.layout == HexLayout.ODD_R) 1 else 0)
                    AffineTransform.getTranslateInstance(
                        (hex.q + (if (shift) 0.5 else 0.0)) * (ROOT_3 * LENGTH + GAP),
                        hex.r * (1.5 * LENGTH + GAP),
                    ).createTransformedShape(flatSide)
                }
                HexLayout.ODD_Q, HexLayout.EVEN_Q -> run {
                    val shift = hex.q.absoluteValue % 2 == (if (rootNode.layout == HexLayout.ODD_Q) 1 else 0)
                    AffineTransform.getTranslateInstance(
                        hex.q * (1.5 * LENGTH + GAP),
                        (hex.r + (if (shift) 0.5 else 0.0)) * (ROOT_3 * LENGTH + GAP),
                    ).createTransformedShape(flatTop)
                }
            }.let { AffineTransform.getScaleInstance(1.0, -1.0).createTransformedShape(it) }
        }
    }
}
