package com.joecollins.models.general;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileWatcherService implements AutoCloseable {

  private final ExecutorService executor;
  private final Future<Void> task;

  private FileWatcherService(ExecutorService executor, Future<Void> task) {
    this.executor = executor;
    this.task = task;
  }

  @FunctionalInterface
  public interface IOConsumer<T> {
    void accept(T t) throws IOException;
  }

  public static FileWatcherService createService(Path path, IOConsumer<Path> onUpdate)
      throws IOException {
    ExecutorService executor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = Executors.defaultThreadFactory().newThread(r);
              t.setDaemon(true);
              return t;
            });
    try {
      Files.walk(path)
          .filter(Files::isRegularFile)
          .forEach(
              t -> {
                try {
                  onUpdate.accept(t);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
    WatchService watchService = FileSystems.getDefault().newWatchService();
    path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
    Callable<Void> r =
        () -> {
          while (true) {
            WatchKey key;
            try {
              key = watchService.take();
            } catch (InterruptedException x) {
              Thread.currentThread().interrupt();
              return null;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
              try {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                  continue;
                }

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                try {
                  onUpdate.accept(path.resolve(filename));
                } catch (IOException | UncheckedIOException e) {
                  e.printStackTrace();
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }

            boolean valid = key.reset();
            if (!valid) {
              System.err.println("Key reset not valid");
            }
          }
        };
    Future<Void> future = executor.submit(r);
    return new FileWatcherService(executor, future);
  }

  @Override
  public void close() throws IOException {
    task.cancel(true);
    executor.shutdownNow();
  }
}
