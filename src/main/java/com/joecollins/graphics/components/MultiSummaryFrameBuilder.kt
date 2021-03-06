package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import java.awt.Color

class MultiSummaryFrameBuilder private constructor() {

    private var headerBinding: Binding<String?>? = null
    private var rowsBinding: Binding<List<MultiSummaryFrame.Row>>? = null

    fun withHeader(header: Binding<String?>): MultiSummaryFrameBuilder {
        this.headerBinding = header
        return this
    }

    fun build(): MultiSummaryFrame {
        return MultiSummaryFrame(
            headerBinding = headerBinding ?: Binding.fixedBinding(null),
            rowsBinding = rowsBinding ?: Binding.fixedBinding(emptyList())
        )
    }

    companion object {
        @JvmStatic fun <T> tooClose(
            items: List<T>,
            display: (T) -> Binding<Boolean>,
            sortFunc: (T) -> Binding<Number>,
            rowHeaderFunc: (T) -> Binding<String>,
            rowLabelsFunc: (T) -> Binding<List<Pair<Color, String>>>,
            limit: Int
        ): MultiSummaryFrameBuilder {

            class Row(val display: Boolean, val sort: Number, val row: MultiSummaryFrame.Row)

            val displayedRows: Binding<List<MultiSummaryFrame.Row>> =
                Binding.listBinding(
                    items.map {
                        val meta = display(it).merge(sortFunc(it)) { d, s -> Pair(d, s) }
                        val row = rowHeaderFunc(it).merge(rowLabelsFunc(it)) { h, v -> MultiSummaryFrame.Row(h, v) }
                        meta.merge(row) { m, r -> Row(m.first, m.second, r) }
                    }
                ).map { rows ->
                    rows.filter { it.display }
                        .sortedBy { it.sort.toDouble() }
                        .map { it.row }
                        .take(limit)
                }
            val builder = MultiSummaryFrameBuilder()
            builder.rowsBinding = displayedRows
            return builder
        }
    }
}
