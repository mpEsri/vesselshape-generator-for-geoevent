package com.esri.geoevent.processor.vesselshapegenerator.provider;

import com.esri.geoevent.processor.vesselshapegenerator.model.Parser;
import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Resource provider.
 */
public class ResourceProvider implements Provider {
  private final String uri;

  public ResourceProvider(String uri) {
    this.uri = uri;
  }

  @Override
  public Map<String, Shape> provide() throws ProviderException {
    try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(uri);) {
      Parser parser = new Parser();
      return parser.parse(inputStream);
    } catch (IOException ex) {
      throw new ProviderException(String.format("Error reading shapes definitions from: %s", uri), ex);
    }
  }
}
