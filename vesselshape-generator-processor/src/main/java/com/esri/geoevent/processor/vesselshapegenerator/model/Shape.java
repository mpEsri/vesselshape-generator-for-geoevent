package com.esri.geoevent.processor.vesselshapegenerator.model;

import java.util.List;

/**
 * Single shape.
 */
public final class Shape {
  public String type;
  public List<Factor> starboardSide;
  public List<Factor> portSide;
}
