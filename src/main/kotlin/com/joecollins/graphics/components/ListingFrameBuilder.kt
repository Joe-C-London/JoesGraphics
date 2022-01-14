package com.joecollins.graphics.components

import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

class ListingFrameBuilder {

    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var subheadTextPublisher: Flow.Publisher<out String?>? = null
    private var notesPublisher: Flow.Publisher<out String?>? = null
    private var barsPublisher: Flow.Publisher<out List<BarFrame.Bar>>? = null

    fun withHeader(headerPublisher: Flow.Publisher<out String?>): ListingFrameBuilder {
        this.headerPublisher = headerPublisher
        return this
    }

    fun withSubhead(subheadPublisher: Flow.Publisher<out String?>): ListingFrameBuilder {
        this.subheadTextPublisher = subheadPublisher
        return this
    }

    fun withNotes(notesPublisher: Flow.Publisher<out String?>): ListingFrameBuilder {
        this.notesPublisher = notesPublisher
        return this
    }

    fun build(): BarFrame {
        return BarFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            subheadTextPublisher = subheadTextPublisher,
            notesPublisher = notesPublisher,
            barsPublisher = barsPublisher ?: emptyList<BarFrame.Bar>().asOneTimePublisher()
        )
    }

    companion object {
        @JvmStatic
        fun <T> of(
            list: Flow.Publisher<out List<T>>,
            leftTextFunc: (T) -> String,
            rightTextFunc: (T) -> String,
            colorFunc: (T) -> Color
        ): ListingFrameBuilder {
            val builder = ListingFrameBuilder()
            builder.barsPublisher = list.map { l ->
                l.map {
                    BarFrame.Bar(leftTextFunc(it), rightTextFunc(it), null, listOf(Pair(colorFunc(it), 1)))
                }
            }
            return builder
        }

        @JvmStatic fun <T> ofFixedList(
            list: List<T>,
            leftTextFunc: (T) -> Flow.Publisher<out String>,
            rightTextFunc: (T) -> Flow.Publisher<out String>,
            colorFunc: (T) -> Flow.Publisher<out Color>
        ): ListingFrameBuilder {
            val builder = ListingFrameBuilder()
            builder.barsPublisher =
                list.map {
                    leftTextFunc(it).merge(rightTextFunc(it)) { left, right -> Pair(left, right) }
                        .merge(colorFunc(it)) {
                            (left, right), color ->
                            BarFrame.Bar(left, right, listOf(Pair(color, 1)))
                        }
                }
                    .combine()
            return builder
        }
    }
}
