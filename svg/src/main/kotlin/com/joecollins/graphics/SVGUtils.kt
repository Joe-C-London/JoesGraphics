package com.joecollins.graphics

import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.DocumentLoader
import org.apache.batik.bridge.GVTBuilder
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.gvt.GraphicsNode
import org.apache.batik.parser.AWTPathProducer
import org.apache.batik.parser.PathParser
import org.apache.batik.util.XMLResourceDescriptor
import java.awt.Shape
import java.net.URI

object SVGUtils {

    fun createShape(path: String): Shape {
        val parser = PathParser()
        val handler = AWTPathProducer()
        parser.pathHandler = handler
        parser.parse(path)
        return handler.shape
    }

    fun parseSvg(uri: URI): GraphicsNode {
        val svg = SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName()).createSVGDocument(uri.toString())
        val ctx = UserAgentAdapter().let { userAgent -> BridgeContext(userAgent, DocumentLoader(userAgent)) }.apply { isDynamic = true }
        return GVTBuilder().build(ctx, svg)
    }
}
