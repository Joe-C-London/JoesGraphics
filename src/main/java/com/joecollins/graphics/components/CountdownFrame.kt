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

class CountdownFrame : GraphicsFrame() {

    internal var clock: Clock = Clock.systemDefaultZone()

    private var timeBinding: Binding<Temporal> = Binding.fixedBinding(Instant.now())
    private var labelFunc: (Duration) -> String = { it.toString() }
    private var additionalInfoBinding: Binding<String?> = Binding.fixedBinding(null)
    private var countdownColorBinding: Binding<Color> = Binding.fixedBinding(Color.BLACK)

    private var time: Temporal = Instant.now()

    private val timeRemainingLabel: JLabel = JLabel()
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

    fun setTimeBinding(timeBinding: Binding<Temporal>) {
        this.timeBinding.unbind()
        this.timeBinding = timeBinding
        this.timeBinding.bind {
            time = it
            refresh()
        }
    }

    private fun refresh() {
        timeRemainingLabel.text = labelFunc(getTimeRemaining())
        this.repaint()
    }

    fun setLabelFunction(labelFunc: (Duration) -> String) {
        this.labelFunc = labelFunc
        refresh()
    }

    internal fun getTimeRemainingString(): String {
        return timeRemainingLabel.text
    }

    internal fun getAdditionalInfo(): String? {
        return if (additionalInfoLabel.isVisible) additionalInfoLabel.text else null
    }

    fun setAdditionalInfoBinding(additionalInfoBinding: Binding<String?>) {
        this.additionalInfoBinding.unbind()
        this.additionalInfoBinding = additionalInfoBinding
        this.additionalInfoBinding.bind {
            additionalInfoLabel.text = it ?: ""
            additionalInfoLabel.isVisible = it != null
        }
    }

    internal fun getCountdownColor(): Color {
        return timeRemainingLabel.foreground
    }

    fun setCountdownColorBinding(countdownColorBinding: Binding<Color>) {
        this.countdownColorBinding.unbind()
        this.countdownColorBinding = countdownColorBinding
        this.countdownColorBinding.bind {
            timeRemainingLabel.foreground = it
            additionalInfoLabel.foreground = it
        }
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
