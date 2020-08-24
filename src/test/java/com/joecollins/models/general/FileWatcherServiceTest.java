package com.joecollins.models.general;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileWatcherServiceTest {

  private static final Charset UTF8 = StandardCharsets.UTF_8;
  private Path tempPath;

  @Before
  public void setup() throws IOException {
    tempPath = Files.createTempDirectory("fws-test");
  }

  @After
  public void teardown() throws IOException {
    Files.walk(tempPath).map(Path::toFile).forEach(File::delete);
    Files.deleteIfExists(tempPath);
  }

  @Test
  public void testFirstRead() throws IOException {
    String file1Contents = "File 1 Contents";
    String file2Contents = "File 2 Contents";

    Files.writeString(tempPath.resolve("file1.txt"), file1Contents, UTF8);
    Files.writeString(tempPath.resolve("file2.txt"), file2Contents, UTF8);

    Map<Path, String> contents = new HashMap<>();
    try (FileWatcherService service =
        FileWatcherService.createService(
            tempPath, p -> contents.put(p, Files.readString(p, UTF8)))) {}

    assertEquals(file1Contents, contents.get(tempPath.resolve("file1.txt")));
    assertEquals(file2Contents, contents.get(tempPath.resolve("file2.txt")));
  }

  @Test
  public void testUpdate() throws IOException, InterruptedException {
    String file1Contents = "File 1 Contents";
    String file1NewContents = "File 1 New Contents";
    String file2Contents = "File 2 Contents";
    String file3Contents = "File 3 Contents";

    Files.writeString(tempPath.resolve("file1.txt"), file1Contents, UTF8);
    Files.writeString(tempPath.resolve("file2.txt"), file2Contents, UTF8);

    Map<Path, String> contents = new HashMap<>();
    try (FileWatcherService service =
        FileWatcherService.createService(
            tempPath, p -> contents.put(p, Files.readString(p, UTF8)))) {
      {
        Path tmpFile = Files.createTempFile("file", ".txt");
        Files.writeString(tmpFile, file1NewContents, UTF8);
        Files.move(tmpFile, tempPath.resolve("file1.txt"), StandardCopyOption.REPLACE_EXISTING);
      }
      {
        Path tmpFile = Files.createTempFile("file", ".txt");
        Files.writeString(tmpFile, file3Contents, UTF8);
        Files.move(tmpFile, tempPath.resolve("file3.txt"), StandardCopyOption.REPLACE_EXISTING);
      }
      Thread.sleep(100);
    }
    assertEquals(file1NewContents, contents.get(tempPath.resolve("file1.txt")));
    assertEquals(file2Contents, contents.get(tempPath.resolve("file2.txt")));
    assertEquals(file3Contents, contents.get(tempPath.resolve("file3.txt")));
  }

  @Test(expected = IOException.class)
  public void testIOExceptionThrownOnFirstAttempt() throws IOException {
    String file1Contents = "File 1 Contents";

    Files.writeString(tempPath.resolve("file1.txt"), file1Contents, UTF8);

    try (FileWatcherService service =
        FileWatcherService.createService(
            tempPath,
            p -> {
              throw new IOException("KABOOM!");
            })) {}
  }

  @Test
  public void testIOExceptionOnUpdateIgnored() throws IOException, InterruptedException {
    String file1Contents = "File 1 Contents";
    String file1NewContents = "File 1 New Contents";
    String file2Contents = "File 2 Contents";
    String file3Contents = "File 3 Contents";

    Files.writeString(tempPath.resolve("file1.txt"), file1Contents, UTF8);
    Files.writeString(tempPath.resolve("file2.txt"), file2Contents, UTF8);

    Map<Path, String> contents = new HashMap<>();
    try (FileWatcherService service =
        FileWatcherService.createService(
            tempPath,
            p -> {
              if (p.toString().contains("file3.txt")) {
                throw new IOException("KABOOM!");
              }
              contents.put(p, Files.readString(p, UTF8));
            })) {
      {
        Path tmpFile = Files.createTempFile("file", ".txt");
        Files.writeString(tmpFile, file3Contents, UTF8);
        Files.move(tmpFile, tempPath.resolve("file3.txt"), StandardCopyOption.REPLACE_EXISTING);
      }
      {
        Path tmpFile = Files.createTempFile("file", ".txt");
        Files.writeString(tmpFile, file1NewContents, UTF8);
        Files.move(tmpFile, tempPath.resolve("file1.txt"), StandardCopyOption.REPLACE_EXISTING);
      }
      Thread.sleep(200);
    }
    assertEquals(file1NewContents, contents.get(tempPath.resolve("file1.txt")));
    assertEquals(file2Contents, contents.get(tempPath.resolve("file2.txt")));
  }
}
