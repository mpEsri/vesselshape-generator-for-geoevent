package com.esri.geoevent.processor.vesselshapegenerator.model;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Model parser.
 */
public class Parser {
  
  public Map<String,Shape> parse(InputStream jsonStream) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Shape> shapeMap = new HashMap<>();
    
    Shape [] shapes = mapper.readValue(jsonStream, Shape[].class);
    
    for (Shape shape: shapes) {
      shapeMap.put(shape.type, shape);
    }
    
    return shapeMap;
  }
}
