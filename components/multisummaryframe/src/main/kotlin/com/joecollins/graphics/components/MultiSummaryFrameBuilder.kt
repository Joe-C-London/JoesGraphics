package com.joecollins.graphics.components

import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

object MultiSummaryFrameBuilder {

    fun <T> dynamicallyFiltered(
        items: List<T>,
        display: T.() -> Flow.Publisher<out Boolean>,
        orderBy: T.() -> Flow.Publisher<out Number>,
        rowHeader: T.() -> Flow.Publisher<out String>,
        rowLabels: T.() -> Flow.Publisher<out List<Pair<Color, String>>>,
        limit: Int,
        header: Flow.Publisher<out String?>,
    ): MultiSummaryFrame {
        class Row(val display: Boolean, val sort: Number, val row: MultiSummaryFrame.Row)

        val displayedRows =
            items.map {
                val meta = it.display().merge(it.orderBy()) { d, s -> Pair(d, s) }
                val row = it.rowHeader().merge(it.rowLabels()) { h, v -> MultiSummaryFrame.Row(h, v) }
                meta.merge(row) { m, r -> Row(m.first, m.second, r) }
            }
                .combine()
                .map { rows ->
                    rows.filter { it.display }
                        .sortedBy { it.sort.toDouble() }
                        .map { it.row }
                        .take(limit)
                }
        return MultiSummaryFrame(
            rowsPublisher = displayedRows,
            headerPublisher = header,
        )
    }
}
