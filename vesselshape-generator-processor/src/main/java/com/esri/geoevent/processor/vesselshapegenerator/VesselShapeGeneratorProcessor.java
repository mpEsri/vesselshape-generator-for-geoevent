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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.core.geometry.Geometry;
import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;
import com.esri.geoevent.processor.vesselshapegenerator.provider.Provider;
import com.esri.geoevent.processor.vesselshapegenerator.provider.ProviderException;
import java.util.List;

public class VesselShapeGeneratorProcessor extends GeoEventProcessorBase {

  private static final Log LOG = LogFactory.getLog(VesselShapeGeneratorProcessor.class);
  private final List<Provider> shapeProviders;
  private int outwkid;

  public VesselShapeGeneratorProcessor(GeoEventProcessorDefinition definition, List<Provider> shapeProviders) throws ComponentException {
    super(definition);
    this.shapeProviders = shapeProviders;
  }

  @Override
  public boolean isGeoEventMutator() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      outwkid = (Integer) properties.get("wkidout").getValue();
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }
  }

  private boolean hasTag(GeoEvent ge, String tagName) {
    return ge.getGeoEventDefinition().getTagNames().contains("GEOMETRY");
  }
  
  private boolean hasAllTags(GeoEvent ge, String ... tagNames) {
    for (String tagName: tagNames) {
      if (!hasTag(ge, tagName)) {
        return false;
      }
    }
    return true;
  }
  
  private boolean validate(GeoEvent ge) {
    return true;
  }
  
  @Override
  public GeoEvent process(GeoEvent ge) throws Exception {

    LOG.debug(String.format("Received an event: %s", ge.toString()));
    try {
      if (!hasAllTags(ge, "GEOMETRY", "TRACK_ID", "VESSEL_TYPE", "VESSEL_BEARING", "VESSEL_BOW", "VESSEL_PORT", "VESSEL_STARBOARD", "VESSEL_STERN")) {
        LOG.debug(String.format("Event rejected due to missing tag(s)."));
        return null;
      }
      
      MapGeometry geo = ge.getGeometry();
      SpatialReference srIn = geo.getSpatialReference();
      if (!(geo.getGeometry() instanceof Point)) {
        LOG.debug(String.format("Event rejected due to missing geometry."));
        return null;
      }
      Point originGeo = (Point)geo.getGeometry();
      
      // read vessel properties
      String vesselType = ge.getField("VESSEL_TYPE")!=null? ge.getField("VESSEL_TYPE").toString(): null;
      Double vesselBear = ge.getField("VESSEL_BEARING") instanceof Number? ((Number)ge.getField("VESSEL_BEARING")).doubleValue(): null;
      
      Double vesselBow   = ge.getField("VESSEL_BOW") instanceof Number? ((Number)ge.getField("VESSEL_BOW")).doubleValue(): null;               // top
      Double vesselStern = ge.getField("VESSEL_STERN") instanceof Number? ((Number)ge.getField("VESSEL_STERN")).doubleValue(): null;           // bottom
      Double vesselPort  = ge.getField("VESSEL_PORT") instanceof Number? ((Number)ge.getField("VESSEL_PORT")).doubleValue(): null;             // left
      Double vesselStar  = ge.getField("VESSEL_STARBOARD") instanceof Number? ((Number)ge.getField("VESSEL_STARBOARD")).doubleValue(): null;   // right
      
      if (vesselBow==null || vesselStern == null || vesselPort == null || vesselStar == null) {
        LOG.debug(String.format("Event rejected due to missing dimensions."));
        return null;
      }
      
      // calculate vessel length and width
      double shipLength = vesselBow + vesselStern;
      double shipWidth = vesselPort + vesselStar;
      
      // calculate necessary shift considering GPS location
      double xShift = shipWidth/2.0 - vesselPort;
      double yShift = shipLength/2.0 - vesselStern;
      
      // calculate shiift considering vessel bearing
      double vesselBearRad = vesselBear * Math.PI / 180.0;
      double xShiftRot = xShift * Math.cos(-vesselBearRad) - yShift * Math.sin(-vesselBearRad);
      double yShiftRot = xShift * Math.sin(-vesselBearRad) + yShift * Math.cos(-vesselBearRad);
      
      // calculate ratio
      SpatialReference srBuffer = SpatialReference.create(102100);
      SpatialReference srOut = SpatialReference.create(outwkid);
      Point centerProj = (Point) GeometryEngine.project(originGeo, srIn, srBuffer);
      double ratio = GeometryUtility.calculateRation(centerProj, shipLength);
      
      // establish new vessel center
      centerProj.setXY(centerProj.getX() + xShiftRot*ratio, centerProj.getY() + yShiftRot*ratio);
      
      // obtain vessel shape; use default if shape unavailable
      Shape shape = readShape(vesselType);
      if (shape==null) {
        shape = readShape("0");
      }
      
      // generate vessel shape
      Geometry vesselShape = GeometryUtility.generateVesselShape(centerProj, shipWidth, shipLength, vesselBear, shape);

      // project and store vessel shape
      Geometry vesselShapeOut = GeometryEngine.project(vesselShape, srBuffer, srOut);
      MapGeometry outMapGeo = new MapGeometry(vesselShapeOut, srOut);
      ge.setGeometry(outMapGeo);
      
      return ge;
    } catch (Exception e) {
      LOG.error(e.getMessage());
      throw e;
    }
  }
  
  private Shape readShape(String vesselType) throws ProviderException{
    for (Provider sp : shapeProviders) {
      Shape shape = sp.readShapes().get(vesselType);
      if (shape!=null) {
        return shape;
      }
    }
    return null;
  }
}
