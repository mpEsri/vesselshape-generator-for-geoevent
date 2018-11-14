package com.esri.geoevent.processor.vesselshapegenerator.provider;

import com.esri.geoevent.processor.vesselshapegenerator.model.Parser;
import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data in folder provider.
 */
public class FolderProvider implements Provider {

  private static final Log LOG = LogFactory.getLog(FolderProvider.class);
  private static final Parser parser = new Parser();

  private final String path;

  private File folder;
  private Map<String, Shape> cache;
  private Thread thread;

  public FolderProvider(String path) {
    this.path = path;
  }

  @Override
  public Map<String, Shape> readShapes() throws ProviderException {
    return getCache();
  }

  public void init() {
    folder = new File(System.getProperty("user.home"), path);
    try {
      setCache(scanFolder(folder));
      thread = new Thread(new FolderObserver(), "Folder observer");
      thread.start();
    } catch (IOException ex) {
      LOG.error(String.format("Error scanning folder: %s", folder), ex);
    }
  }

  public void destroy() {
    if (thread != null) {
      thread.interrupt();
    }
  }

  private synchronized void setCache(Map<String, Shape> cache) {
    this.cache = cache;
  }

  private synchronized Map<String, Shape> getCache() {
    return cache;
  }

  private Map<String, Shape> scanFolder(File root) throws IOException {
    Map<String, Shape> shapes = new HashMap<>();
    for (File file : root.listFiles()) {
      if (file.getName().toLowerCase().endsWith(".json")) {
        shapes.putAll(readShapes(file));
      }
    }
    return shapes;
  }

  private Map<String, Shape> readShapes(File file) throws IOException {
    try (InputStream inputStream = new FileInputStream(file);) {
      return parser.parse(inputStream);
    }
  }

  private class FolderObserver implements Runnable {

    private final WatchService watcher;
    private WatchKey regKey;

    public FolderObserver() throws IOException {
      watcher = FileSystems.getDefault().newWatchService();
      regKey = Paths.get(folder.toURI()).register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    @Override
    public void run() {
      WatchKey key;
      try {
        LOG.info(String.format("Watching %s folder for changes has started.", folder));
        while (!Thread.currentThread().isInterrupted()) {
          key = watcher.take();
          for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            if (kind == OVERFLOW) {
              continue;
            }
            try {
              setCache(scanFolder(folder));
            } catch (IOException ex) {
              LOG.error(String.format("Error scanning folder: %s", folder), ex);
            }
          }
          boolean valid = key.reset();
          if (!valid) {
            return;
          }
        }
      } catch (InterruptedException x) {
      } finally {
        LOG.info(String.format("Watching %s folder for changes has ended.", folder));
        regKey.reset();
        regKey = null;
      }
    }

  }

}
