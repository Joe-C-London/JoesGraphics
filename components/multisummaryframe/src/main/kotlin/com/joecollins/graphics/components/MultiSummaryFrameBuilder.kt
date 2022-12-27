package com.joecollins.graphics.components

import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

class MultiSummaryFrameBuilder private constructor() {

    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var rowsPublisher: Flow.Publisher<out List<MultiSummaryFrame.Row>>? = null

    fun withHeader(header: Flow.Publisher<out String?>): MultiSummaryFrameBuilder {
        this.headerPublisher = header
        return this
    }

    fun build(): MultiSummaryFrame {
        return MultiSummaryFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            rowsPublisher = rowsPublisher ?: emptyList<MultiSummaryFrame.Row>().asOneTimePublisher()
        )
    }

    companion object {
        fun <T> dynamicallyFiltered(
            items: List<T>,
            display: (T) -> Flow.Publisher<out Boolean>,
            sortFunc: (T) -> Flow.Publisher<out Number>,
            rowHeaderFunc: (T) -> Flow.Publisher<out String>,
            rowLabelsFunc: (T) -> Flow.Publisher<out List<Pair<Color, String>>>,
            limit: Int
        ): MultiSummaryFrameBuilder {
            class Row(val display: Boolean, val sort: Number, val row: MultiSummaryFrame.Row)

            val displayedRows =
                items.map {
                    val meta = display(it).merge(sortFunc(it)) { d, s -> Pair(d, s) }
                    val row = rowHeaderFunc(it).merge(rowLabelsFunc(it)) { h, v -> MultiSummaryFrame.Row(h, v) }
                    meta.merge(row) { m, r -> Row(m.first, m.second, r) }
                }
                    .combine().map { rows ->
                        rows.filter { it.display }
                            .sortedBy { it.sort.toDouble() }
                            .map { it.row }
                            .take(limit)
                    }
            val builder = MultiSummaryFrameBuilder()
            builder.rowsPublisher = displayedRows
            return builder
        }
    }
}
