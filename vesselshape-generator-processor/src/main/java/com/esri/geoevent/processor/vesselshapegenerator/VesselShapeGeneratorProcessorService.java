package com.esri.geoevent.processor.vesselshapegenerator;

/*
 * #%L
 * Esri :: AGES :: Solutions :: Processor :: Geometry
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2013 - 2014 Esri
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.esri.geoevent.processor.vesselshapegenerator.provider.Provider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessor;
import com.esri.ges.processor.GeoEventProcessorServiceBase;
import java.util.List;

public class VesselShapeGeneratorProcessorService extends GeoEventProcessorServiceBase {
	public GeoEventDefinitionManager manager;
  private List<Provider> shapeProviders;
  
	private static final Log LOG = LogFactory
			.getLog(VesselShapeGeneratorProcessorService.class);
	public VesselShapeGeneratorProcessorService() throws PropertyException {
		definition = new VesselShapeGeneratorProcessorDefinition();
	}

  public void setShapeProviders(List<Provider> shapeProviders) {
    this.shapeProviders = shapeProviders;
  }

	@Override
	public GeoEventProcessor create() {
		try {
			return new VesselShapeGeneratorProcessor(definition, shapeProviders);
		} catch (ComponentException e) {
			LOG.error("Rangefan processor");
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			return null;
		} catch (Exception e) {
			LOG.error("Rangefan processor");
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			return null;
		}

	}
}
