package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class CountdownFrame extends GraphicsFrame {

  private Clock clock = Clock.systemDefaultZone();
  private Binding<? extends Temporal> timeBinding = Instant::now;
  private Function<Duration, String> labelFunc = Duration::toString;
  private Binding<String> additionalInfoBinding = () -> null;
  private Binding<Color> countdownColorBinding = () -> Color.BLACK;

  private Temporal time = timeBinding.getValue();

  private JLabel timeRemainingLabel;
  private JLabel additionalInfoLabel;

  public CountdownFrame() {
    timeRemainingLabel = new JLabel();
    timeRemainingLabel.setFont(StandardFont.readBoldFont(24));
    timeRemainingLabel.setHorizontalAlignment(JLabel.CENTER);
    timeRemainingLabel.setVerticalAlignment(JLabel.CENTER);
    timeRemainingLabel.setBorder(new EmptyBorder(3, 0, -3, 0));

    additionalInfoLabel = new JLabel();
    additionalInfoLabel.setFont(StandardFont.readNormalFont(12));
    additionalInfoLabel.setHorizontalAlignment(JLabel.CENTER);
    additionalInfoLabel.setVerticalAlignment(JLabel.CENTER);
    additionalInfoLabel.setBorder(new EmptyBorder(2, 0, -2, 0));

    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    panel.setLayout(new BorderLayout());
    panel.add(timeRemainingLabel, BorderLayout.CENTER);
    panel.add(additionalInfoLabel, BorderLayout.SOUTH);
    add(panel, BorderLayout.CENTER);

    ScheduledExecutorService executor =
        Executors.newScheduledThreadPool(
            1,
            r -> {
              Thread t = Executors.defaultThreadFactory().newThread(r);
              t.setDaemon(true);
              return t;
            });
    executor.scheduleAtFixedRate(
        () -> EventQueue.invokeLater(this::refresh), 0, 100, TimeUnit.MILLISECONDS);
  }

  void setClock(Clock clock) {
    this.clock = clock;
  }

  Duration getTimeRemaining() {
    return Duration.between(clock.instant().truncatedTo(ChronoUnit.SECONDS), time);
  }

  public void setTimeBinding(Binding<? extends Temporal> timeBinding) {
    this.timeBinding.unbind();
    this.timeBinding = timeBinding;
    this.timeBinding.bind(
        time -> {
          this.time = time;
          refresh();
        });
  }

  private void refresh() {
    this.timeRemainingLabel.setText(labelFunc.apply(getTimeRemaining()));
    this.repaint();
  }

  public void setLabelFunction(Function<Duration, String> labelFunc) {
    this.labelFunc = labelFunc;
    refresh();
  }

  String getTimeRemainingString() {
    return timeRemainingLabel.getText();
  }

  String getAdditionalInfo() {
    return additionalInfoLabel.isVisible() ? additionalInfoLabel.getText() : null;
  }

  public void setAdditionalInfoBinding(Binding<String> additionalInfoBinding) {
    this.additionalInfoBinding.unbind();
    this.additionalInfoBinding = additionalInfoBinding;
    this.additionalInfoBinding.bind(
        text -> {
          additionalInfoLabel.setText(text);
          additionalInfoLabel.setVisible(text != null);
        });
  }

  Color getCountdownColor() {
    return timeRemainingLabel.getForeground();
  }

  public void setCountdownColorBinding(Binding<Color> countdownColorBinding) {
    this.countdownColorBinding.unbind();
    this.countdownColorBinding = countdownColorBinding;
    this.countdownColorBinding.bind(
        color -> {
          timeRemainingLabel.setForeground(color);
          additionalInfoLabel.setForeground(color);
        });
  }

  public static String formatDDHHMMSS(Duration duration) {
    return String.format(
        "%d:%02d:%02d:%02d",
        duration.toHours() / 24,
        duration.toHours() % 24,
        duration.toMinutesPart(),
        duration.toSecondsPart());
  }

  public static String formatHHMMSS(Duration duration) {
    return String.format(
        "%d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
  }

  public static String formatMMSS(Duration duration) {
    return String.format("%d:%02d", duration.toMinutes(), duration.toSecondsPart());
  }
}
