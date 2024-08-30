package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import java.awt.Shape
import java.util.concurrent.Flow

sealed class AbstractMap<T> {
    lateinit var shapes: Flow.Publisher<out Map<T, Shape>>
    var focus: Flow.Publisher<out List<T>?>? = null
    var additionalHighlight: Flow.Publisher<out List<T>?>? = null
    lateinit var header: Flow.Publisher<out String?>

    internal val additionalHighlightOrFocus by lazy { additionalHighlight ?: focus }

    abstract val mapFrame: MapFrame
}
