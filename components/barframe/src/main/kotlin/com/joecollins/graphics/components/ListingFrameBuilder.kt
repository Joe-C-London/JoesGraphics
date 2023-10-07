package com.joecollins.graphics.components

import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

object ListingFrameBuilder {

    fun <T> of(
        list: Flow.Publisher<out List<T>>,
        leftTextFunc: (T) -> String,
        rightTextFunc: (T) -> String,
        colorFunc: (T) -> Color,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>? = null,
        notes: Flow.Publisher<out String?>? = null,
    ): BarFrame {
        return BarFrame(
            barsPublisher = list.map { l ->
                l.map {
                    BarFrame.Bar(leftTextFunc(it), rightTextFunc(it), null, listOf(Pair(colorFunc(it), 1)))
                }
            },
            headerPublisher = header,
            subheadTextPublisher = subhead,
            notesPublisher = notes,
        )
    }

    fun <T> of(
        list: List<T>,
        leftTextFunc: (T) -> Flow.Publisher<out String>,
        rightTextFunc: (T) -> Flow.Publisher<out String>,
        colorFunc: (T) -> Flow.Publisher<out Color>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>? = null,
        notes: Flow.Publisher<out String?>? = null,
    ): BarFrame {
        return BarFrame(
            barsPublisher =
            list.map {
                leftTextFunc(it).merge(rightTextFunc(it)) { left, right -> Pair(left, right) }
                    .merge(colorFunc(it)) {
                            (left, right), color ->
                        BarFrame.Bar(left, right, listOf(Pair(color, 1)))
                    }
            }
                .combine(),
            headerPublisher = header,
            subheadTextPublisher = subhead,
            notesPublisher = notes,
        )
    }
}
