/*
  Copyright 1995-2018 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
*/
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
