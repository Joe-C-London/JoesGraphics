package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import java.awt.Color
import java.lang.Runnable
import java.util.ArrayList
import org.apache.commons.lang3.mutable.MutableBoolean
import org.apache.commons.lang3.tuple.Pair

class MultiSummaryFrameBuilder {

    private val frame = MultiSummaryFrame()

    fun withHeader(header: Binding<String?>): MultiSummaryFrameBuilder {
        frame.setHeaderBinding(header)
        return this
    }

    fun build(): MultiSummaryFrame {
        return frame
    }

    companion object {
        @JvmStatic fun <T> tooClose(
            items: List<T>,
            display: (T) -> Binding<Boolean>,
            sortFunc: (T) -> Binding<out Number>,
            rowHeaderFunc: (T) -> Binding<String>,
            rowLabelsFunc: (T) -> Binding<List<Pair<Color, String>>>,
            limit: Int
        ): MultiSummaryFrameBuilder {

            class Row {
                var display = false
                var sort = 0.0
                var rowHeader: String = ""
                var rowLabels: List<Pair<Color, String>> = ArrayList()
            }

            val allRows: MutableList<Row> = ArrayList()
            val displayedRows = BindableList<Row>()
            val isReady = MutableBoolean(false)
            val update = Runnable {
                if (isReady.booleanValue()) displayedRows.setAll(
                        allRows
                                .filter { it.display }
                                .sortedBy { it.sort }
                                .take(limit)
                                .toList())
            }
            for (item in items) {
                val row = Row()
                display(item)
                        .bind {
                            row.display = it
                            update.run()
                        }
                sortFunc(item)
                        .bind {
                            row.sort = it.toDouble()
                            update.run()
                        }
                rowHeaderFunc(item)
                        .bind {
                            row.rowHeader = it
                            update.run()
                        }
                rowLabelsFunc(item)
                        .bind {
                            row.rowLabels = it
                            update.run()
                        }
                allRows.add(row)
            }
            isReady.setTrue()
            update.run()
            val builder = MultiSummaryFrameBuilder()
            builder.frame.setNumRowsBinding(Binding.sizeBinding(displayedRows))
            builder.frame.setRowHeaderBinding(
                    IndexedBinding.propertyBinding(displayedRows) { it.rowHeader })
            builder.frame.setValuesBinding(IndexedBinding.propertyBinding(displayedRows) { it.rowLabels })
            return builder
        }
    }
}
