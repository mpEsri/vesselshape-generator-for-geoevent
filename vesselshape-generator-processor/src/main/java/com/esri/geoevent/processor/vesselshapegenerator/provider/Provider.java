package com.esri.geoevent.processor.vesselshapegenerator.provider;

import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;
import java.util.Map;

/**
 * Data provider.
 */
public interface Provider {
  Map<String, Shape> provide() throws ProviderException;
}
