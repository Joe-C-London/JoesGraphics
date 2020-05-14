package com.joecollins.graphics;

import com.joecollins.graphics.components.GraphicsFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class GenericWindow<T extends JPanel> extends JFrame {

  protected final T panel;

  public GenericWindow(T panel) {
    this(panel, panel.getClass().getSimpleName());
  }

  public GenericWindow(T panel, String title) {
    this.panel = panel;
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setExtendedState(MAXIMIZED_BOTH);
    setTitle(title);
    setVisible(true);

    getContentPane().setLayout(new FlowLayout());
    getContentPane().setBackground(Color.BLACK);
    JPanel p;
    if (panel instanceof GraphicsFrame) {
      p = new JPanel();
      p.setBackground(Color.WHITE);
      p.setLayout(new GridLayout(1, 1));
      p.setBorder(new EmptyBorder(5, 5, 5, 5));
      p.add(panel);
    } else {
      p = panel;
    }
    getContentPane().add(p);

    JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    JMenu imageMenu = new JMenu("Image");
    menuBar.add(imageMenu);

    JMenuItem copyItem = new JMenuItem("Copy to Clipboard");
    copyItem.addActionListener(
        evt -> {
          copyImageToClipboard(p);
        });
    imageMenu.add(copyItem);

    JMenuItem fileItem = new JMenuItem("Save to File...");
    fileItem.addActionListener(
        evt -> {
          saveImageToFile(p);
        });
    imageMenu.add(fileItem);

    JMenuItem tweetItem = new JMenuItem("Tweet...");
    tweetItem.addActionListener(
        evt -> {
          TweetFrame tweetFrame = new TweetFrame(p);
          tweetFrame.setVisible(true);
        });
    imageMenu.add(tweetItem);

    requestFocus();
  }

  public GenericWindow<T> withControlPanel(JPanel panel) {
    add(panel);
    return this;
  }

  private static void saveImageToFile(JPanel panel) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(
        new File(System.getProperty("user.home"), "Pictures/Joe's Politics"));
    fileChooser.setFileFilter(
        new FileFilter() {
          @Override
          public boolean accept(File f) {
            if (f.isDirectory()) {
              return true;
            }
            final String name = f.getName();
            return name.endsWith(".png");
          }

          @Override
          public String getDescription() {
            return "PNG file (*.png)";
          }
        });
    if (fileChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      if (!file.getName().endsWith("png")) {
        file = new File(file.getPath() + ".png");
      }
      try {
        BufferedImage img = generateImage(panel);
        ImageIO.write(img, "png", file);
      } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
            panel, e.getMessage(), "Cannot save image", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private static void copyImageToClipboard(JPanel panel) {
    BufferedImage img = generateImage(panel);
    Transferable transferableImage =
        new Transferable() {
          @Override
          public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor};
          }

          @Override
          public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.imageFlavor.equals(flavor);
          }

          @Override
          public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (DataFlavor.imageFlavor.equals(flavor)) {
              return img;
            } else {
              throw new UnsupportedFlavorException(flavor);
            }
          }
        };
    ClipboardOwner owner = (clipboard, contents) -> {};
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    c.setContents(transferableImage, owner);
  }

  private static BufferedImage generateImage(JPanel component) {
    BufferedImage img =
        new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB);
    component.print(img.getGraphics());
    return img;
  }

  public static class TweetFrame extends JDialog {

    public TweetFrame(JPanel panel) {
      setSize(new Dimension(300, 300));
      setModal(true);
      setTitle("Tweet");
      getContentPane().setLayout(new BorderLayout());
      Color twitterColor = new Color(0x00acee);

      JTextArea textArea = new JTextArea();
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      getContentPane().add(textArea, BorderLayout.CENTER);

      JPanel bottomPanel = new JPanel();
      bottomPanel.setBackground(twitterColor);
      bottomPanel.setLayout(new BorderLayout());
      getContentPane().add(bottomPanel, BorderLayout.SOUTH);

      JLabel charLabel = new JLabel("0");
      charLabel.setForeground(Color.WHITE);
      bottomPanel.add(charLabel, BorderLayout.WEST);
      textArea.addKeyListener(
          new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
              charLabel.setText(String.valueOf(textArea.getText().length()));
            }
          });

      JButton tweetButton = new JButton("Tweet");
      tweetButton.setBackground(twitterColor);
      tweetButton.setForeground(Color.WHITE);
      bottomPanel.add(tweetButton, BorderLayout.EAST);
      tweetButton.addActionListener(
          evt -> {
            File file;
            try {
              file = File.createTempFile("joes-politics", ".png");
              file.deleteOnExit();
              BufferedImage img = generateImage(panel);
              ImageIO.write(img, "png", file);
            } catch (IOException exc) {
              JOptionPane.showMessageDialog(panel, "Could not save image: " + exc.getMessage());
              exc.printStackTrace();
              return;
            }
            try {
              tweetButton.setEnabled(false);
              tweetButton.setText("Tweeting...");
              String tweet = textArea.getText();
              sendTweet(tweet, file);
              setVisible(false);
            } catch (Exception exc) {
              JOptionPane.showMessageDialog(panel, "Could not send tweet: " + exc.getMessage());
              exc.printStackTrace();
              tweetButton.setEnabled(true);
              tweetButton.setText("Tweet");
            }
          });
    }

    private void sendTweet(String tweet, File image) throws IOException, TwitterException {
      StatusUpdate status = new StatusUpdate(tweet);
      status.media(image);
      ConfigurationBuilder cb = new ConfigurationBuilder();
      InputStream twitterPropertiesFile =
          this.getClass().getClassLoader().getResourceAsStream("twitter.properties");
      if (twitterPropertiesFile == null) {
        throw new IllegalStateException("Unable to find twitter.properties");
      }
      Properties properties = new Properties();
      properties.load(twitterPropertiesFile);
      cb.setDebugEnabled(true)
          .setOAuthConsumerKey(properties.get("oauthConsumerKey").toString())
          .setOAuthConsumerSecret(properties.get("oauthConsumerSecret").toString())
          .setOAuthAccessToken(properties.get("oauthAccessToken").toString())
          .setOAuthAccessTokenSecret(properties.get("oauthAccessTokenSecret").toString());
      new TwitterFactory(cb.build()).getInstance().updateStatus(status);
    }
  }
}
