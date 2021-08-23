package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.graphics.utils.StandardFont
import java.awt.BorderLayout
import java.awt.Color
import java.awt.EventQueue
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CountdownFrame(
    headerBinding: Binding<String>,
    timeBinding: Binding<Temporal>,
    private val labelFunc: (Duration) -> String,
    borderColorBinding: Binding<Color>? = null,
    additionalInfoBinding: Binding<String?>? = null,
    countdownColorBinding: Binding<Color>? = null
) : GraphicsFrame(
    headerBinding = headerBinding,
    borderColorBinding = borderColorBinding
) {

    internal var clock: Clock = Clock.systemDefaultZone()
        set(value) {
            field = value
            refresh()
        }

    private var time: Temporal = Instant.now()

    private val timeRemainingLabel: JLabel = FontSizeAdjustingLabel()
    private val additionalInfoLabel: JLabel = JLabel()

    init {
        timeRemainingLabel.font = StandardFont.readBoldFont(24)
        timeRemainingLabel.horizontalAlignment = JLabel.CENTER
        timeRemainingLabel.verticalAlignment = JLabel.CENTER
        timeRemainingLabel.border = EmptyBorder(3, 0, -3, 0)

        additionalInfoLabel.font = StandardFont.readNormalFont(12)
        additionalInfoLabel.horizontalAlignment = JLabel.CENTER
        additionalInfoLabel.verticalAlignment = JLabel.CENTER
        additionalInfoLabel.border = EmptyBorder(2, 0, -2, 0)

        val panel = JPanel()
        panel.background = Color.WHITE
        panel.layout = BorderLayout()
        panel.add(timeRemainingLabel, BorderLayout.CENTER)
        panel.add(additionalInfoLabel, BorderLayout.SOUTH)
        add(panel, BorderLayout.CENTER)

        timeBinding.bind {
            time = it
            refresh()
        }
        (additionalInfoBinding ?: Binding.fixedBinding(null)).bind {
            additionalInfoLabel.text = it ?: ""
            additionalInfoLabel.isVisible = it != null
        }
        (countdownColorBinding ?: Binding.fixedBinding(Color.BLACK)).bind {
            timeRemainingLabel.foreground = it
            additionalInfoLabel.foreground = it
        }

        val executor = Executors.newScheduledThreadPool(1) { r: Runnable ->
            val t = Executors.defaultThreadFactory().newThread(r)
            t.isDaemon = true
            t
        }
        executor.scheduleAtFixedRate(
            { EventQueue.invokeLater { this.refresh() } }, 0, 100, TimeUnit.MILLISECONDS
        )
    }

    internal fun getTimeRemaining(): Duration {
        return Duration.between(clock.instant().truncatedTo(ChronoUnit.SECONDS), time)
    }

    private fun refresh() {
        timeRemainingLabel.text = labelFunc(getTimeRemaining())
        this.repaint()
    }

    internal fun getTimeRemainingString(): String {
        return timeRemainingLabel.text
    }

    internal fun getAdditionalInfo(): String? {
        return if (additionalInfoLabel.isVisible) additionalInfoLabel.text else null
    }

    internal fun getCountdownColor(): Color {
        return timeRemainingLabel.foreground
    }

    companion object {
        @JvmStatic fun formatDDHHMMSS(duration: Duration): String {
            return String.format(
                "%d:%02d:%02d:%02d",
                duration.toHours() / 24,
                duration.toHours() % 24,
                duration.toMinutesPart(),
                duration.toSecondsPart()
            )
        }

        @JvmStatic fun formatHHMMSS(duration: Duration): String {
            return String.format(
                "%d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()
            )
        }

        @JvmStatic fun formatMMSS(duration: Duration): String {
            return String.format("%d:%02d", duration.toMinutes(), duration.toSecondsPart())
        }
    }
}
