package com.esri.geoevent.processor.vesselshapegenerator.provider;

import com.esri.geoevent.processor.vesselshapegenerator.model.Parser;
import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Data in folder provider.
 */
public class FolderProvider implements Provider {

  private static final Log LOG = LogFactory.getLog(FolderProvider.class);
  private static final Parser parser = new Parser();

  private final File path;

  private Map<String, Shape> cache;
  private Thread thread;

  public FolderProvider(String path) {
    this.path = new File(StrSubstitutor.replaceSystemProperties(path));
    this.path.mkdirs();
  }

  @Override
  public Map<String, Shape> readShapes() throws ProviderException {
    return getCache();
  }

  public void init() {
    try {
      setCache(scanFolder(path));
      thread = new Thread(new FolderObserver(), "Folder observer");
      thread.start();
    } catch (IOException ex) {
      LOG.error(String.format("Error scanning folder: %s", path), ex);
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
      regKey = Paths.get(path.toURI()).register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    }

    @Override
    public void run() {
      WatchKey key;
      try {
        LOG.info(String.format("Watching %s folder vessel definitions has started.", path));
        while (!Thread.currentThread().isInterrupted()) {
          key = watcher.take();
          for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();
            if (kind == OVERFLOW) {
              continue;
            }
            try {
              setCache(scanFolder(path));
            } catch (IOException ex) {
              LOG.error(String.format("Error scanning folder: %s", path), ex);
            }
          }
          boolean valid = key.reset();
          if (!valid) {
            return;
          }
        }
      } catch (InterruptedException x) {
      } finally {
        LOG.info(String.format("Watching %s folder for changes has ended.", path));
        regKey.reset();
        regKey = null;
      }
    }

  }

}
