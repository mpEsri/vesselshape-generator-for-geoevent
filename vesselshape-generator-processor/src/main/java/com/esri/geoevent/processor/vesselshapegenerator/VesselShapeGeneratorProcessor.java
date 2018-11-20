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
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.core.geometry.Geometry;
import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;
import com.esri.geoevent.processor.vesselshapegenerator.provider.Provider;
import com.esri.geoevent.processor.vesselshapegenerator.provider.ProviderException;
import org.apache.commons.lang3.StringUtils;

public class VesselShapeGeneratorProcessor extends GeoEventProcessorBase {

  private static final Log LOG = LogFactory.getLog(VesselShapeGeneratorProcessor.class);
  //public GeoEventDefinitionManager manager;
  private final Provider shapeProvider;
  private int outwkid;
  
  /*
  private SpatialReference srIn;
  private SpatialReference srBuffer;
  private SpatialReference srOut;
  private String rangeSource;
  private double rangeConstant;
  private String rangeEventFld;
  private String rangeUnits;
  private String bearingSource;
  private double bearingConstant;
  private String bearingEventFld;
  private String traversalSource;
  private double traversalConstant;
  private String traversalEventFld;
  private int inwkid;
  private int bufferwkid;
  private String geosrc = "";
  private String geometryEventFld;
  private String xfield;
  private String yfield;
  private String shapeField;
  */

  public VesselShapeGeneratorProcessor(GeoEventProcessorDefinition definition, Provider shapeProvider) throws ComponentException {
    super(definition);
    this.shapeProvider = shapeProvider;
  }

  @Override
  public boolean isGeoEventMutator() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    try {
      //rangeSource = properties.get("rangeSource").getValue().toString();
      //rangeConstant = (Double) properties.get("range").getValue();
      //rangeEventFld = properties.get("rangeEvent").getValue().toString();
      //rangeUnits = properties.get("units").getValue().toString();

      //bearingSource = properties.get("bearingSource").getValue().toString();
      //bearingConstant = (Double) properties.get("bearing").getValue();
      //bearingEventFld = properties.get("bearingEvent").getValue().toString();

      //traversalSource = properties.get("traversalSource").getValue().toString();
      //traversalConstant = (Double) properties.get("traversal").getValue();
      //traversalEventFld = properties.get("traversalEvent").getValue().toString();
      
      outwkid = (Integer) properties.get("wkidout").getValue();
      //bufferwkid = (Integer) properties.get("wkidbuffer").getValue();

      //geosrc = properties.get("geosrc").getValueAsString();
      //geometryEventFld = properties.get("geoeventfld").getValue().toString();
      //xfield = properties.get("xfield").getValueAsString();
      //yfield = properties.get("yfield").getValueAsString();
      //shapeField = properties.get("shapefield").getValueAsString();
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }

  }

  @Override
  public void validate() throws ValidationException {
    super.validate();
    /*
    if (rangeConstant <= 0) {
      throw new ValidationException(
              "A constant range must be greater than 0");
    }

    if (bearingConstant < 0 || bearingConstant >= 360) {
      throw new ValidationException(
              "A constant bearing must be >= 0 and < 360");
    }

    if (traversalConstant < 0 || bearingConstant >= 360) {
      throw new ValidationException(
              "A constant traversal must be > 0 and < 360");
    }

    try {
      srBuffer = SpatialReference.create(bufferwkid);
    } catch (Exception e) {
      throw new ValidationException(
              "The spatial processing wkid is invalid");
    }

    try {
      srOut = SpatialReference.create(outwkid);
    } catch (Exception e) {
      throw new ValidationException("The output wkid is invalid");
    }
    */
  }

  @Override
  public GeoEvent process(GeoEvent ge) throws Exception {

    try {
      if (!ge.getGeoEventDefinition().getTagNames().contains("GEOMETRY")) {
        return null;
      }
      if (!ge.getGeoEventDefinition().getTagNames().contains("TRACK_ID")) {
        return null;
      }
      if (!ge.getGeoEventDefinition().getTagNames().contains("VESSEL_TYPE")) {
        return null;
      }
      if (!ge.getGeoEventDefinition().getTagNames().contains("VESSEL_BEAR")) {
        return null;
      }
      if (!ge.getGeoEventDefinition().getTagNames().contains("VESSEL_BOW")) {
        return null;
      }
      if (!ge.getGeoEventDefinition().getTagNames().contains("VESSEL_PORT")) {
        return null;
      }
      if (!ge.getGeoEventDefinition().getTagNames().contains("VESSEL_STAR")) {
        return null;
      }
      if (!ge.getGeoEventDefinition().getTagNames().contains("VESSEL_STERN")) {
        return null;
      }
      
      MapGeometry geo = ge.getGeometry();
      SpatialReference srIn = geo.getSpatialReference();
      if (!(geo.getGeometry() instanceof Point)) {
        return null;
      }
      Point originGeo = (Point)geo.getGeometry();
      String vesselType = (String)ge.getField("VESSEL_TYPE");
      Double vesselBear = (Double)ge.getField("VESSEL_BEAR");
      Double vesselBow = (Double)ge.getField("VESSEL_BOW");
      Double vesselPort = (Double)ge.getField("VESSEL_PORT");
      Double vesselStar = (Double)ge.getField("VESSEL_STAR");
      Double vesselStern = (Double)ge.getField("VESSEL_STERN");
      
      double shipLength = vesselBow + vesselStern;
      double shipWidth = vesselPort + vesselStar;
      
      SpatialReference srBuffer = SpatialReference.create(102100);
      SpatialReference srOut = SpatialReference.create(outwkid);
      Point centerProj = (Point) GeometryEngine.project(originGeo, srIn, srBuffer);
      Shape shape = shapeProvider.readShapes().get(vesselType);
      if (shape==null) {
        shape = shapeProvider.readShapes().get("default");
      }
      Geometry vesselShape = GeometryUtility.generateVesselShape(centerProj, shipWidth, shipLength, vesselBear, shape);

      Geometry vesselShapeOut = GeometryEngine.project(vesselShape, srBuffer, srOut);
      MapGeometry outMapGeo = new MapGeometry(vesselShapeOut, srOut);
      ge.setGeometry(outMapGeo);
      
      return ge;
    } catch (Exception e) {
      LOG.error(e.getMessage());
      throw e;
    }
  }
}
