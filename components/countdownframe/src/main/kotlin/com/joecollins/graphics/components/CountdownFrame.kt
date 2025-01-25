package com.joecollins.graphics.components

import com.joecollins.graphics.utils.StandardFont
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.TimePublisher
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.merge
import org.jetbrains.annotations.TestOnly
import java.awt.BorderLayout
import java.awt.Color
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CountdownFrame(
    headerPublisher: Flow.Publisher<out String>,
    timePublisher: Flow.Publisher<out Temporal>,
    private val labelFunc: (Duration) -> String,
    borderColorPublisher: Flow.Publisher<out Color>? = null,
    additionalInfoPublisher: Flow.Publisher<out String?>? = null,
    countdownColorPublisher: Flow.Publisher<out Color>? = null,
) : GraphicsFrame(
    headerPublisher = headerPublisher,
    borderColorPublisher = borderColorPublisher,
) {

    var clock: Clock = Clock.systemDefaultZone()
        @TestOnly
        set(value) {
            field = value
            now.submit(TimePublisher.forClock(value))
        }

    private val now = Publisher(TimePublisher.forClock(clock))
    private var time: Temporal = Instant.now()
    private val timeRemaining: Flow.Publisher<Duration>

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
        addCenter(panel)

        timeRemaining = now.compose { it }.merge(timePublisher) { now, later ->
            Duration.between(now.truncatedTo(ChronoUnit.SECONDS), later)
        }
        timeRemaining.subscribe(
            Subscriber(
                eventQueueWrapper {
                    timeRemainingLabel.text = labelFunc(it)
                    repaint()
                },
            ),
        )

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
    }

    @TestOnly
    internal fun getTimeRemaining(): Duration {
        var duration: Duration? = null
        timeRemaining.subscribe(Subscriber { duration = it })
        return duration!!
    }

    internal fun getTimeRemainingString(): String = timeRemainingLabel.text

    internal fun getAdditionalInfo(): String? = if (additionalInfoLabel.isVisible) additionalInfoLabel.text else null

    internal fun getCountdownColor(): Color = timeRemainingLabel.foreground

    companion object {
        fun formatDDHHMMSS(duration: Duration): String = String.format(
            "%d:%02d:%02d:%02d",
            duration.toHours() / 24,
            duration.toHours() % 24,
            duration.toMinutesPart(),
            duration.toSecondsPart(),
        )

        fun formatHHMMSS(duration: Duration): String = String.format(
            "%d:%02d:%02d",
            duration.toHours(),
            duration.toMinutesPart(),
            duration.toSecondsPart(),
        )

        fun formatMMSS(duration: Duration): String = String.format("%d:%02d", duration.toMinutes(), duration.toSecondsPart())
    }
}
