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

import com.esri.ges.core.AccessType;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class VesselShapeGeneratorProcessorDefinition extends
		GeoEventProcessorDefinitionBase {

	private static final Log LOG = LogFactory
			.getLog(VesselShapeGeneratorProcessorDefinition.class);

	public VesselShapeGeneratorProcessorDefinition() throws PropertyException {
		try {

			PropertyDefinition procWKIDOut = new PropertyDefinition("wkidout",
					PropertyType.Integer, 102100, "${com.esri.geoevent.processor.vesselshape-generator-processor.LBL_OUTPUT_WKID}",
					"${com.esri.geoevent.processor.vesselshape-generator-processor.DESC_OUTPUT_WKID}", true, false);
			propertyDefinitions.put(procWKIDOut.getPropertyName(), procWKIDOut);
      
			GeoEventDefinition def = new DefaultGeoEventDefinition();
      
			def.setName("VesselFeed");
			def.setAccessType(AccessType.editable);
      
			List<FieldDefinition> topLevelFields = new ArrayList<FieldDefinition>();
			topLevelFields.add(new DefaultFieldDefinition("MMSI", FieldType.Long, "TRACK_ID"));
			topLevelFields.add(new DefaultFieldDefinition("shape", FieldType.Geometry, "GEOMETRY"));
			topLevelFields.add(new DefaultFieldDefinition("Timestamp", FieldType.Date, "TIME_START"));
			topLevelFields.add(new DefaultFieldDefinition("Name", FieldType.String, "VESSEL_NAME"));
			topLevelFields.add(new DefaultFieldDefinition("ShipType", FieldType.Integer, "VESSEL_TYPE"));
			topLevelFields.add(new DefaultFieldDefinition("TrueHeading", FieldType.Integer, "VESSEL_BEARING"));
			topLevelFields.add(new DefaultFieldDefinition("DimBow", FieldType.Integer, "VESSEL_BOW"));
			topLevelFields.add(new DefaultFieldDefinition("DimPort", FieldType.Integer, "VESSEL_PORT"));
			topLevelFields.add(new DefaultFieldDefinition("DimStarboard", FieldType.Integer, "VESSEL_STARBOARD"));
			topLevelFields.add(new DefaultFieldDefinition("DimStern", FieldType.Integer, "VESSEL_STERN"));
      
      def.setFieldDefinitions(topLevelFields);
      
      geoEventDefinitions.put(def.getName(), def);
      
		} catch (PropertyException e) {
			LOG.error(e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

	}

	@Override
	public String getName() {
		return "VesselshapeGeneratorProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.processor.geometry";
	}

	@Override
	public String getVersion() {
		return "10.6.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.processor.vesselshape-generator-processor.PROC_LBL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.processor.vesselshape-generator-processor.PROC_DESC}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}
}
