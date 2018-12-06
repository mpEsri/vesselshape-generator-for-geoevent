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
package com.esri.geoevent.processor.vesselshapegenerator;

import java.util.ArrayList;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Point2D;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.geoevent.processor.vesselshapegenerator.model.Factor;
import com.esri.geoevent.processor.vesselshapegenerator.model.Shape;

public class GeometryUtility {

	public GeometryUtility() {}
	
	public static double Geo2Arithmetic(double inAngle)
	{
		return _geo2Arithmetic(inAngle);
	}
	
	private static double _geo2Arithmetic(double inAngle)
	{
		return 360.0-(inAngle+270.0)%360.0;
	}
	
	public static Point Rotate(Point center, Point inPt, double rotationAngle)
	{
		return _rotate(center, inPt, rotationAngle);
	}
	
	public static String parseGeometryType(Geometry.Type t)
	{
		return _parseGeometryType(t);
	}
	
	private static String _parseGeometryType(Geometry.Type t)
	{
		String type = null;
		if(t == Geometry.Type.Point)
		{
			type = "esriGeometryPoint";
		}
		else if (t==Geometry.Type.Polyline)
		{
			type = "esriGeometryPolyline";
		}
		else if (t==Geometry.Type.Polygon)
		{
			type = "esriGeometryPolygon";
		}
		else if (t==Geometry.Type.MultiPoint)
		{
			type = "esriGeometryMultiPoint";
		}
		return type;
	}
	
	private static Point _rotate(Point center, Point inPt, double radians)
	{
		double x = inPt.getX();
		double y = inPt.getY();
		double cx = center.getX();
		double cy = center.getY();
		double cosra = Math.cos(radians);
		double sinra = Math.sin(radians);
		double rx = cx + cosra * (x - cx) - sinra * (y - cy);
		double ry = cy + sinra * (x - cx) + cosra * (y-cy);
		Point rPt = new Point(rx,ry);
		return rPt;
	}
	
	public Polygon GenerateEllipse(Point center, double majorAxis, double minorAxis, double ra)
	{
		Polygon ellipse = new Polygon();
		for (int i = 0; i < 360; ++i)
		{
			double theta = Math.toRadians(i);
			Point p = ellipsePtFromAngle(center, majorAxis, minorAxis, theta);
			p = GeometryUtility.Rotate(center, p, ra);
			if (i == 0) {
				ellipse.startPath(p);
			}
			else{
				ellipse.lineTo(p);
			}
		}
		ellipse.closeAllPaths();
		return ellipse;
	}
	
	private Point ellipsePtFromAngle(Point center, double rh, double rv, double angle)
	{
		double x = center.getX();
		double y = center.getY();
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double ta = s/c;
		double tt = ta * (rh/rv);
		double d = 1.0 / Math.sqrt(1.0 + Math.pow(tt, 2));
		double ex = x + Math.copySign(rh*d, c);
		double ey = y + Math.copySign(rv * tt * d, s);
		return new Point(ex,ey);
		
	}
	
	private static Polyline drawLine(Point pt1, Point pt2)
	{
	  Polyline line = new Polyline();
	  Point2D[] points = new Point2D[2];
	  points[0] = Point2D.construct(pt1.getX(), pt1.getY());
	  points[1] = Point2D.construct(pt2.getX(), pt2.getY());
	  line.addPath(points, 2, true);
    return line;	  
	}

	public static Polygon _generateVesselShape(Point center, double shipWidth, double shipLength, double headingDegrees)
	{
		// this generate vessel shape pointing west at 0 degree.
		// will need to make it point north first then rotate by heading
	    Point pt = new Point(center.getX() + shipLength, center.getY());
	    Polyline centerline = drawLine(center, pt);
	    Geometry geom = centerline;
	    SpatialReference spatialReference = SpatialReference.create(102100);
	    LinearUnit unit = (LinearUnit)LinearUnit.create(LinearUnit.Code.METER);
	    
	    double lineGeodesicLength = GeometryEngine.geodesicLength(geom, spatialReference, unit); //(centerline, "meters");  
	    double ratio = shipLength / lineGeodesicLength;
	    Polygon polygon = new Polygon();
	    ArrayList<Point2D> path = new ArrayList<Point2D>();
	    double centerX = center.getX();
	    double centerY = center.getY();
	    
	    double aHeading = GeometryUtility.Geo2Arithmetic(headingDegrees);
	    double angleRadians = Math.toRadians(aHeading); 
	    
	    path.add(Point2D.construct(centerX, centerY));
	
	    /////Top side
	    path.add(Point2D.construct(centerX + (shipLength * 0.005 * ratio), centerY + (shipWidth * 0.088 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.005, 0.008));    
	    
	    path.add(Point2D.construct(centerX + (shipLength * 0.008 * ratio), centerY + (shipWidth * 0.123 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.008, 0.123));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.022 * ratio), centerY + (shipWidth * 0.203 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.022, 0.203));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.045 * ratio), centerY + (shipWidth * 0.281 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.045, 0.281));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.073 * ratio), centerY + (shipWidth * 0.357 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.073, 0.357));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.108 * ratio), centerY + (shipWidth * 0.431 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.108, 0.431));
	    
	    path.add(Point2D.construct(centerX + (shipLength * 0.150 * ratio), centerY + (shipWidth * 0.5 * ratio)  ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.150, 0.5));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.99 * ratio),  centerY + (shipWidth * 0.5 * ratio)  ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.99, 0.5));
	    
	    path.add(Point2D.construct(centerX + (shipLength * 0.995 * ratio), centerY + (shipWidth * 0.35 * ratio) ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.995, 0.35));
	    
	    path.add(Point2D.construct(centerX + (shipLength * 1 * ratio),     centerY + (shipWidth * 0.2 * ratio)  ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 1.0, 0.2));
	
	    ////// Bottom side
	    path.add(Point2D.construct(centerX + (shipLength * 1 * ratio),     centerY - (shipWidth * 0.2 * ratio)  ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 1.0, 0.2));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.995 * ratio), centerY - (shipWidth * 0.35 * ratio) ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.995, 0.35));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.99 * ratio),  centerY - (shipWidth * 0.5 * ratio)  ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.99, 0.5));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.150 * ratio), centerY - (shipWidth * 0.5 * ratio)  ));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.150, 0.5));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.108 * ratio), centerY - (shipWidth * 0.431 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.108, 0.431));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.073 * ratio), centerY - (shipWidth * 0.357 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.073, 0.357));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.045 * ratio), centerY - (shipWidth * 0.281 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.045, 0.281));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.022 * ratio), centerY - (shipWidth * 0.203 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.022, 0.203));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.008 * ratio), centerY - (shipWidth * 0.123 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.008, 0.123));
	
	    path.add(Point2D.construct(centerX + (shipLength * 0.005 * ratio), centerY - (shipWidth * 0.088 * ratio)));
	    //path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.005, 0.088));
	
	    path.add(Point2D.construct(centerX,                                centerY));
	    
	    Point2D[] points = new Point2D[path.size()];
	    path.toArray(points);
	    polygon.addPath(points, path.size(), true);
	    polygon.closeAllPaths();
	    return polygon;
	}
  
  public static double calculateRation(Point center, double shipLength) {
    Point pt = new Point(center.getX() + shipLength, center.getY());
    Polyline centerline = drawLine(center, pt);
    Geometry geom = centerline;
    SpatialReference spatialReference = SpatialReference.create(102100);
    LinearUnit unit = (LinearUnit)LinearUnit.create(LinearUnit.Code.METER);
    double lineGeodesicLength = GeometryEngine.geodesicLength(geom, spatialReference, unit);
    double ratio = shipLength / lineGeodesicLength;
    return ratio;
  }
  
	public static Polygon generateVesselShape(Point center, double shipWidth, double shipLength, double headingDegrees, Shape shape)
	{
		// this generate vessel shape pointing west at 0 degree.
		// will need to make it point north first then rotate by heading
    /*
	    Point pt = new Point(center.getX() + shipLength, center.getY());
	    Polyline centerline = drawLine(center, pt);
	    Geometry geom = centerline;
	    SpatialReference spatialReference = SpatialReference.create(102100);
	    LinearUnit unit = (LinearUnit)LinearUnit.create(LinearUnit.Code.METER);
	    
	    double lineGeodesicLength = GeometryEngine.geodesicLength(geom, spatialReference, unit); //(centerline, "meters");  
	    double ratio = shipLength / lineGeodesicLength;
    */
      double ratio = calculateRation(center, shipLength);
	    Polygon polygon = new Polygon();
	    ArrayList<Point2D> path = new ArrayList<Point2D>();
	    double centerX = center.getX();
	    double centerY = center.getY();
	    
	    double aHeading = GeometryUtility.Geo2Arithmetic(headingDegrees);
	    double angleRadians = Math.toRadians(aHeading - 180); //add 90 degree to make it point east
	    
	    /////Top side
	    final double positiveDir = 1.0;
	    path.add(createVertex(shipWidth, shipLength, 1.0, centerX, centerY, angleRadians, 0.0, 0.0, positiveDir));  
      for (Factor f: shape.starboardSide) {
        path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, f.x, f.y, positiveDir));    
      }
	
	    ////// Bottom side
	    final double negativeDir = -1.0;
      for (Factor f: shape.portSide) {
        path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, f.x, f.y, negativeDir));    
      }
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.0, 0.0, positiveDir));    
    
	    Point2D[] points = new Point2D[path.size()];
	    path.toArray(points);

	    polygon.addPath(points, path.size(), true);
	    polygon.closeAllPaths();
	    return polygon;
	}

	public static Polygon generateVesselShape(Point center, double shipWidth, double shipLength, double headingDegrees)
	{
		// this generate vessel shape pointing west at 0 degree.
		// will need to make it point north first then rotate by heading
	    Point pt = new Point(center.getX() + shipLength, center.getY());
	    Polyline centerline = drawLine(center, pt);
	    Geometry geom = centerline;
	    SpatialReference spatialReference = SpatialReference.create(102100);
	    LinearUnit unit = (LinearUnit)LinearUnit.create(LinearUnit.Code.METER);
	    
	    double lineGeodesicLength = GeometryEngine.geodesicLength(geom, spatialReference, unit); //(centerline, "meters");  
	    double ratio = shipLength / lineGeodesicLength;
	    Polygon polygon = new Polygon();
	    ArrayList<Point2D> path = new ArrayList<Point2D>();
	    double centerX = center.getX();
	    double centerY = center.getY();
	    
	    double aHeading = GeometryUtility.Geo2Arithmetic(headingDegrees);
	    double angleRadians = Math.toRadians(aHeading - 180); //add 90 degree to make it point east
	    
	    /////Top side
	    final double positiveDir = 1.0;

	    //path.add(Point2D.construct(centerX, centerY));
	    path.add(createVertex(shipWidth, shipLength, 1.0, centerX, centerY, angleRadians, 0.0, 0.0, positiveDir));    
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.005 * ratio), centerY + (shipWidth * 0.088 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.005, 0.008, positiveDir));    
	    
	    //path.add(Point2D.construct(centerX + (shipLength * 0.008 * ratio), centerY + (shipWidth * 0.123 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.008, 0.123, positiveDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.022 * ratio), centerY + (shipWidth * 0.203 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.022, 0.203, positiveDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.045 * ratio), centerY + (shipWidth * 0.281 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.045, 0.281, positiveDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.073 * ratio), centerY + (shipWidth * 0.357 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.073, 0.357, positiveDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.108 * ratio), centerY + (shipWidth * 0.431 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.108, 0.431, positiveDir));
	    
	    //path.add(Point2D.construct(centerX + (shipLength * 0.150 * ratio), centerY + (shipWidth * 0.5 * ratio)  ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.150, 0.5, positiveDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.99 * ratio),  centerY + (shipWidth * 0.5 * ratio)  ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.99, 0.5, positiveDir));
	    
	    //path.add(Point2D.construct(centerX + (shipLength * 0.995 * ratio), centerY + (shipWidth * 0.35 * ratio) ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.995, 0.35, positiveDir));
	    
	    //path.add(Point2D.construct(centerX + (shipLength * 1 * ratio),     centerY + (shipWidth * 0.2 * ratio)  ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 1.0, 0.2, positiveDir));
	
	    ////// Bottom side
	    final double negativeDir = -1.0;
	    //path.add(Point2D.construct(centerX + (shipLength * 1 * ratio),     centerY - (shipWidth * 0.2 * ratio)  ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 1.0, 0.2, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.995 * ratio), centerY - (shipWidth * 0.35 * ratio) ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.995, 0.35, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.99 * ratio),  centerY - (shipWidth * 0.5 * ratio)  ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.99, 0.5, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.150 * ratio), centerY - (shipWidth * 0.5 * ratio)  ));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.150, 0.5, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.108 * ratio), centerY - (shipWidth * 0.431 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.108, 0.431, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.073 * ratio), centerY - (shipWidth * 0.357 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.073, 0.357, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.045 * ratio), centerY - (shipWidth * 0.281 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.045, 0.281, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.022 * ratio), centerY - (shipWidth * 0.203 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.022, 0.203, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.008 * ratio), centerY - (shipWidth * 0.123 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.008, 0.123, negativeDir));
	
	    //path.add(Point2D.construct(centerX + (shipLength * 0.005 * ratio), centerY - (shipWidth * 0.088 * ratio)));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.005, 0.088, negativeDir));
	
	    //path.add(Point2D.construct(centerX,                                centerY));
	    path.add(createVertex(shipWidth, shipLength, ratio, centerX, centerY, angleRadians, 0.0, 0.0, positiveDir));    
    
	    Point2D[] points = new Point2D[path.size()];
	    path.toArray(points);
	    
	    /*
	    Transformation2D rotate2D = new Transformation2D(); //create identity matrix
	    rotate2D.setRotate(angleRadians + (Math.PI * 0.5));
	    	    
	    Point2D[] rotatedPoints = new Point2D[path.size()];
	    rotate2D.transform(points, path.size(), rotatedPoints);

	    Transformation2D shift2D = new Transformation2D();
	    shift2D.shift(centerX - (shipLength * 0.5), centerY);
	    Point2D[] shiftedPoints = new Point2D[path.size()];
	    shift2D.transform(rotatedPoints, path.size(), shiftedPoints);

	    polygon.addPath(shiftedPoints, path.size(), true);
	    */

	    polygon.addPath(points, path.size(), true);
	    polygon.closeAllPaths();
	    return polygon;
	}
	
  /**
   * @param shipWidth
   * @param shipLength
   * @param ratio
   * @param centerX
   * @param centerY
   * @param angleRadians
   * @param xFactor
   * @param yFactor
   * @param direction //either 1.0 or -1.0
   * @return
   */
  private static Point2D createVertex(double shipWidth, double shipLength, 
      double ratio, double centerX, double centerY, 
      double angleRadians, double xFactor, double yFactor, double direction)
  {
    Point point = new Point(centerX + (shipLength * xFactor * ratio), centerY + (direction * (shipWidth * yFactor * ratio)));

    // translate to negative X-Axis 0.5 * shipLength
    double x = point.getX();
    point.setX(x-(shipLength * 0.5));

    //Rotate point around the center
    Point center = new Point(centerX, centerY);
        
    Point rotated = GeometryUtility.Rotate(center, point, angleRadians);
    Point2D vertex = Point2D.construct(rotated.getX(), rotated.getY());
    
    return vertex;
  }

	/*
	public Polygon rotatePolygon(double degrees)
	{
	}
	*/
}

/*
 *          function drawLine(pt1, pt2){
            var pt1Cor = [pt1[0], pt1[1]];
            var pt2Cor = [pt2[0], pt2[1]];
            var lineJSON = {
              paths: [[pt1Cor, pt2Cor]],
              spatialReference: {
                                    "wkid": 102100
                                }
            };
            var line = new polyline(lineJSON);
            return line;
            }
          
                    function addGraphic(evt) {
                        //deactivate the toolbar and clear existing graphics 
                        tb.deactivate();
                        self.map.enableMapNavigation();

                        var symbol;
                        symbol = fillSymbol;

                        var centerX = self.map.extent.getCenter().x;
                        var centerY = self.map.extent.getCenter().y;
            
            var centerline = drawLine([centerX,centerY],[centerX + Number(self.shipLength),centerY]);
            var lineGeodesicLength = geometryEngine.geodesicLength(centerline, "meters");  
            var ratio = self.shipLength / lineGeodesicLength;
            
                        var myPolygon = {
                            "geometry": {
                                "rings": [
                                    [
                                        [centerX, centerY],
                                        [centerX + (self.shipLength * 0.005 * ratio), centerY + (self.shipWidth * 0.088 * ratio)],
                                        [centerX + (self.shipLength * 0.008 * ratio), centerY + (self.shipWidth * 0.123 * ratio)],
                                        [centerX + (self.shipLength * 0.022 * ratio), centerY + (self.shipWidth * 0.203 * ratio)],
                                        [centerX + (self.shipLength * 0.045 * ratio), centerY + (self.shipWidth * 0.281 * ratio)],
                                        [centerX + (self.shipLength * 0.073 * ratio), centerY + (self.shipWidth * 0.357 * ratio)],
                                        [centerX + (self.shipLength * 0.108 * ratio), centerY + (self.shipWidth * 0.431 * ratio)],
                                        [centerX + (self.shipLength * 0.150 * ratio), centerY + (self.shipWidth * 0.5 * ratio)],
                                        [centerX + (self.shipLength * 0.99 * ratio), centerY + (self.shipWidth * 0.5 * ratio)],
                                        [centerX + (self.shipLength * 0.995 * ratio), centerY + (self.shipWidth * 0.35 * ratio)],
                                        [centerX + (self.shipLength * 1 * ratio), centerY + (self.shipWidth * 0.2 * ratio)],
                                        [centerX + (self.shipLength * 1 * ratio), centerY - (self.shipWidth * 0.2 * ratio)],
                                        [centerX + (self.shipLength * 0.995 * ratio), centerY - (self.shipWidth * 0.35 * ratio)],
                                        [centerX + (self.shipLength * 0.99 * ratio), centerY - (self.shipWidth * 0.5 * ratio)],
                                        [centerX + (self.shipLength * 0.150 * ratio), centerY - (self.shipWidth * 0.5 * ratio)],
                                        [centerX + (self.shipLength * 0.108 * ratio), centerY - (self.shipWidth * 0.431 * ratio)],
                                        [centerX + (self.shipLength * 0.073 * ratio), centerY - (self.shipWidth * 0.357 * ratio)],
                                        [centerX + (self.shipLength * 0.045 * ratio), centerY - (self.shipWidth * 0.281 * ratio)],
                                        [centerX + (self.shipLength * 0.022 * ratio), centerY - (self.shipWidth * 0.203 * ratio)],
                                        [centerX + (self.shipLength * 0.008 * ratio), centerY - (self.shipWidth * 0.123 * ratio)],
                                        [centerX + (self.shipLength * 0.005 * ratio), centerY - (self.shipWidth * 0.088 * ratio)],
                                        [centerX, centerY],
                                    ]
                                ],
                                "spatialReference": {
                                    "wkid": 102100
                                }
                            },
                            "symbol": {
                                "color": [255, 255, 255, 64],
                                "outline": {
                                    "color": [0, 0, 0, 255],
                                    "width": 1,
                                    "type": "esriSLS",
                                    "style": "esriSLSSolid"
                                },
                                "type": "esriSFS",
                                "style": "esriSFSSolid"
                            }
                        };
                   
                                    self.map.graphics.add(new Graphic(myPolygon));
                                }
                            },

                            // onOpen: function(){
                            //   console.log('ShipWidget::onOpen');
                            // },

                            // onClose: function(){
                            //   console.log('ShipWidget::onClose');
                            // },

                            // onMinimize: function(){
                            //   console.log('ShipWidget::onMinimize');
                            // },

                            // onMaximize: function(){
                            //   console.log('ShipWidget::onMaximize');
                            // },

                            // onSignIn: function(credential){
                            //   console.log('ShipWidget::onSignIn', credential);
                            // },

                            // onSignOut: function(){
                            //   console.log('ShipWidget::onSignOut');
                            // }

                            // onPositionChange: function(){
                            //   console.log('ShipWidget::onPositionChange');
                            // },

                            // resize: function(){
                            //   console.log('ShipWidget::resize');
                            // }


                    });

            });
 * 
 */ 
