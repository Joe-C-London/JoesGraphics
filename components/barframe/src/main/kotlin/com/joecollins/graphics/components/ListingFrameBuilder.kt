package com.joecollins.graphics.components

import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

object ListingFrameBuilder {

    fun <T> of(
        list: Flow.Publisher<out List<T>>,
        leftText: T.() -> String,
        rightText: T.() -> String,
        color: T.() -> Color,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>? = null,
        notes: Flow.Publisher<out String?>? = null,
    ): BarFrame = BarFrame(
        barsPublisher = list.map { l ->
            l.map {
                BarFrame.Bar(it.leftText(), it.rightText(), null, listOf(Pair(it.color(), 1)))
            }
        },
        headerPublisher = header,
        subheadTextPublisher = subhead,
        notesPublisher = notes,
    )

    fun <T> of(
        list: List<T>,
        leftText: T.() -> Flow.Publisher<out String>,
        rightText: T.() -> Flow.Publisher<out String>,
        color: T.() -> Flow.Publisher<out Color>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>? = null,
        notes: Flow.Publisher<out String?>? = null,
    ): BarFrame = BarFrame(
        barsPublisher =
        list.map {
            it.leftText().merge(it.rightText()) { left, right -> Pair(left, right) }
                .merge(it.color()) { (left, right), color ->
                    BarFrame.Bar(left, right, listOf(Pair(color, 1)))
                }
        }
            .combine(),
        headerPublisher = header,
        subheadTextPublisher = subhead,
        notesPublisher = notes,
    )
}
