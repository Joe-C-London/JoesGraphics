package com.joecollins.graphics.components.lowerthird;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class LowerThird extends JPanel {

  private static final BufferedImage DEFAULT_IMAGE =
      new BufferedImage(200, 50, BufferedImage.TYPE_4BYTE_ABGR);

  private Clock clock = Clock.systemDefaultZone();

  private ImagePanel leftPanel = new ImagePanel();
  private PlaceAndTimePanel rightPanel = new PlaceAndTimePanel();

  private Binding<Image> leftImageBinding = Binding.fixedBinding(DEFAULT_IMAGE);
  private Binding<String> placeBinding = Binding.fixedBinding("UTC");
  private Binding<ZoneId> timezoneBinding = Binding.fixedBinding(ZoneOffset.UTC);

  public LowerThird() {
    setLayout(new BorderLayout());
    add(leftPanel, BorderLayout.WEST);
    add(rightPanel, BorderLayout.EAST);
    setPreferredSize(new Dimension(1024, 50));
  }

  Image getLeftImage() {
    return leftPanel.image;
  }

  public void setLeftImageBinding(Binding<Image> leftImageBinding) {
    this.leftImageBinding.unbind();
    this.leftImageBinding = leftImageBinding;
    this.leftImageBinding.bind(leftPanel::setImage);
  }

  String getPlace() {
    return rightPanel.placeLabel.getText();
  }

  public void setPlaceBinding(Binding<String> placeBinding) {
    this.placeBinding.unbind();
    this.placeBinding = placeBinding;
    this.placeBinding.bind(rightPanel::setPlace);
  }

  String getTime() {
    return rightPanel.timeLabel.getText();
  }

  public void setTimeZoneBinding(Binding<ZoneId> timezoneBinding) {
    this.timezoneBinding.unbind();
    this.timezoneBinding = timezoneBinding;
    this.timezoneBinding.bind(rightPanel::setTimezone);
  }

  void setClock(Clock clock) {
    this.clock = clock;
    this.rightPanel.updateTime();
  }

  public static Image createImage(URL url) throws IOException {
    return ImageIO.read(url);
  }

  public static Image createImage(String text, Color foreground, Color background) {
    Font font = StandardFont.readBoldFont(24);
    Rectangle2D bounds;
    {
      Graphics g = new BufferedImage(200, 50, BufferedImage.TYPE_4BYTE_ABGR).getGraphics();
      bounds = g.getFontMetrics(font).getStringBounds(text, g);
      g.dispose();
    }
    BufferedImage img =
        new BufferedImage(
            Math.max(200, (int) bounds.getWidth() + 10), 50, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics g = img.getGraphics();
    ((Graphics2D) g)
        .setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setColor(background);
    g.fillRect(0, 0, img.getWidth(), 50);
    g.setColor(foreground);
    g.setFont(font);
    g.drawString(text, (int) (img.getWidth() - bounds.getWidth()) / 2, 35);
    return img;
  }

  private class ImagePanel extends JPanel {
    private Image image;

    public ImagePanel() {
      setImage(DEFAULT_IMAGE);
    }

    public void setImage(Image image) {
      this.image = image;
      this.setPreferredSize(new Dimension(50 * image.getWidth(null) / image.getHeight(null), 50));
      LowerThird.this.revalidate();
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      g.drawImage(image, 0, 0, null);
    }
  }

  private class PlaceAndTimePanel extends JPanel {
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ScheduledExecutorService executor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r);
              t.setName("LowerThird-Timer-" + this.hashCode());
              t.setDaemon(true);
              return t;
            });

    private ZoneId timezone = ZoneOffset.UTC;
    private JLabel placeLabel =
        new JLabel("UTC") {
          {
            setFont(StandardFont.readBoldFont(12));
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setForeground(Color.BLACK);
            setBorder(new EmptyBorder(10, 0, 0, 0));
          }
        };
    private JLabel timeLabel =
        new JLabel(FORMATTER.format(ZonedDateTime.now(timezone))) {
          {
            setFont(StandardFont.readBoldFont(24));
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setForeground(Color.BLACK);
            setBorder(new EmptyBorder(0, 0, 0, 0));
          }
        };

    public PlaceAndTimePanel() {
      setPreferredSize(new Dimension(100, 50));
      setBackground(Color.YELLOW);
      setLayout(new GridBagLayout());
      add(
          placeLabel,
          new GridBagConstraints() {
            {
              fill = GridBagConstraints.BOTH;
              gridx = 0;
              gridy = 0;
              gridwidth = 1;
              gridheight = 2;
              weightx = 1;
              weighty = 1;
            }
          });
      add(
          timeLabel,
          new GridBagConstraints() {
            {
              fill = GridBagConstraints.BOTH;
              gridx = 0;
              gridy = 2;
              gridwidth = 1;
              gridheight = 3;
              weightx = 1;
              weighty = 1;
            }
          });
      executor.scheduleAtFixedRate(this::updateTime, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void setPlace(String place) {
      this.placeLabel.setText(place);
      repaint();
    }

    public void setTimezone(ZoneId timezone) {
      this.timezone = timezone;
      updateTime();
    }

    private void updateTime() {
      try {
        String newTime = FORMATTER.format(clock.instant().atZone(timezone));
        if (!newTime.equals(this.timeLabel.getText())) {
          this.timeLabel.setText(newTime);
          EventQueue.invokeLater(() -> this.timeLabel.repaint());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
