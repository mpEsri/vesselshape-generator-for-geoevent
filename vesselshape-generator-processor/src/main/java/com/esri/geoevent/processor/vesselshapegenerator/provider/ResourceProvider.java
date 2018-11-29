package com.esri.geoevent.processor.vesselshapegenerator.provider;

import com.esri.geoevent.processor.vesselshapegenerator.model.Parser;
import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;

/**
 * Resource provider.
 */
public class ResourceProvider implements Provider {
	private static final Log LOG = LogFactory.getLog(ResourceProvider.class);
  private final BundleContext bundleContext;
  private final String uri;
  private Map<String, Shape> cache;

  public ResourceProvider(BundleContext bundleContext, String uri) {
    this.bundleContext = bundleContext;
    this.uri = uri;
  }

  @Override
  public Map<String, Shape> readShapes() throws ProviderException {
    if (cache == null)
      throw new ProviderException("No data available for provider.");
    return cache;
  }
  
  public void init() {
    try (InputStream inputStream = bundleContext.getBundle().getEntry(uri).openStream();) {
      Parser parser = new Parser();
      cache = parser.parse(inputStream);
    } catch (IOException ex) {
      LOG.error(String.format("Error reading shapes definitions from: %s", uri), ex);
    }
  }
}
