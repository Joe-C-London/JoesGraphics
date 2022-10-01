package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.EventQueue
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.concurrent.Executors
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CountdownFrame(
    headerPublisher: Flow.Publisher<out String>,
    timePublisher: Flow.Publisher<out Temporal>,
    private val labelFunc: (Duration) -> String,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
    additionalInfoPublisher: Flow.Publisher<out String?>? = null,
    countdownColorPublisher: Flow.Publisher<out Color>? = null
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    borderColorPublisher = borderColorPublisher
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

        val onTimeUpdate: (Temporal) -> Unit = {
            time = it
            refresh()
        }
        timePublisher.subscribe(Subscriber(onTimeUpdate))

        val onAdditionalInfoUpdate: (String?) -> Unit = {
            additionalInfoLabel.text = it ?: ""
            additionalInfoLabel.isVisible = it != null
        }
        if (additionalInfoPublisher != null) {
            additionalInfoPublisher.subscribe(Subscriber(eventQueueWrapper(onAdditionalInfoUpdate)))
        } else {
            onAdditionalInfoUpdate(null)
        }

        val onCountdownColorBinding: (Color) -> Unit = {
            timeRemainingLabel.foreground = it
            additionalInfoLabel.foreground = it
        }
        if (countdownColorPublisher != null) {
            countdownColorPublisher.subscribe(Subscriber(eventQueueWrapper(onCountdownColorBinding)))
        } else {
            onCountdownColorBinding(Color.BLACK)
        }

        val executor = Executors.newScheduledThreadPool(1) { r: Runnable ->
            val t = Executors.defaultThreadFactory().newThread(r)
            t.isDaemon = true
            t
        }
        executor.scheduleAtFixedRate(
            { this.refresh() },
            0,
            100,
            TimeUnit.MILLISECONDS
        )
    }

    internal fun getTimeRemaining(): Duration {
        return Duration.between(clock.instant().truncatedTo(ChronoUnit.SECONDS), time)
    }

    private fun refresh() {
        EventQueue.invokeLater {
            timeRemainingLabel.text = labelFunc(getTimeRemaining())
            this.repaint()
        }
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
        fun formatDDHHMMSS(duration: Duration): String {
            return String.format(
                "%d:%02d:%02d:%02d",
                duration.toHours() / 24,
                duration.toHours() % 24,
                duration.toMinutesPart(),
                duration.toSecondsPart()
            )
        }

        fun formatHHMMSS(duration: Duration): String {
            return String.format(
                "%d:%02d:%02d",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart()
            )
        }

        fun formatMMSS(duration: Duration): String {
            return String.format("%d:%02d", duration.toMinutes(), duration.toSecondsPart())
        }
    }
}
