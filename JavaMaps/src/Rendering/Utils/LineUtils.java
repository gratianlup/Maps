// Copyright (c) 2010 Gratian Lup. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution.
//
// * The name "JavaMaps" must not be used to endorse or promote
// products derived from this software without prior written permission.
//
// * Products derived from this software may not be called "JavaMaps" nor
// may "JavaMaps" appear in their names without prior written
// permission of the author.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package Rendering.Utils;
import Core.*;

public class LineUtils {
    private static int Rotation(double x1, double y1,
                                double x2, double y2,
                                double x3, double y3) {
        // Compute the dot product.
        double value = ((y3 - y1) * (x2 - x1)) - ((x3 - x1) * (y2 - y1));
        
        if(value > 0) {
            return 1;   
        }        
        else if(value < 0) {
            return -1;
        }
        return 0;
    }

    public static boolean LinesIntersect(double x1, double y1,
                                         double x2, double y2,
                                         double x3, double y3,
                                         double x4, double y4) {
        int test1 = Rotation(x1, y1, x2, y2, x3, y3);
        int test2 = Rotation(x1, y1, x2, y2, x4, y4);

        if(test1 != test2) {
            int test3 = Rotation(x3, y3, x4, y4, x1, y1);
            int test4 = Rotation(x3, y3, x4, y4, x2, y2);
            return test3 != test4;
        }
        
        return false;
    }
    
    public static boolean LinesIntersect(Line line, double x1, double y1,
                                                    double x2, double y2) {
        return LinesIntersect(line.XA(), line.YA(), line.XB(), line.YB(),
                              x1, y1, x2, y2);
    }

    public static boolean LinesIntersect(Line line1, Line line2) {
        return LinesIntersect(line1.XA(), line1.YA(), line1.XB(), line1.YB(),
                              line2.XA(), line2.YA(), line2.XB(), line2.YB());
    }

    public static boolean LineIntersectsRect(Line line, double left, double top,
                                             double right, double bottom) {
        // First check if the rectangle around the line
        // intersect with the region.
        if((left <= Math.min(line.XA(), line.XB()) &&
           (top <= Math.min(line.YA(), line.YB())) &&
           (right >= Math.max(line.XA(), line.XA())) &&
           (bottom >= Math.max(line.YA(), line.YB())))) {
            return true;
        }

        // Check if the line intersects with the regions edges.
        return LineUtils.LinesIntersect(line, left, top, right, top)     ||
               LineUtils.LinesIntersect(line, left, top, left, bottom)   ||
               LineUtils.LinesIntersect(line, right, top, right, bottom) ||
               LineUtils.LinesIntersect(line, left, bottom, right, bottom);
    }

    public static boolean LineIntersectsRect(Line line, Region2D rect) {
        return LineIntersectsRect(line, rect.Left(), rect.Top(),
                                  rect.Right(), rect.Bottom());
    }

    public static double PointLineDistanceSq(double pointX, double pointY,
                                             double lineX1, double lineY1,
                                             double lineX2, double lineY2) {
        // Compute the position of the point found
        // at the intersection between the line and the 
        // perpendicular line from the point to the line.
        double ldx = lineX2 - lineX1;
        double ldy = lineY2 - lineY1;
        double u = (((pointX - lineX1) * ldx) +
                   ((pointY - lineY1) * ldy)) / ((ldx * ldx) + (ldy * ldy));

        // Limit to the interior of the line segment.
        if(u > 1) {
            u = 1;
        }
        else if(u < 0) {
            u = 0;
        }

        // The returned value is the square of the distance.
        double x = lineX1 + (u * ldx);
        double y = lineY1 + (u * ldy);
        double dx = x - pointX;
        double dy = y - pointY;
        return (dx * dx) + (dy * dy);
    }

    public static double PointLineDistanceSq(Point point, 
                                             double lineX1, double lineY1,
                                             double lineX2, double lineY2) {
        return PointLineDistanceSq(point.X(), point.Y(), lineX1, lineY1,
                                             lineX2, lineY2);
    }

    public static double PointLineDistanceSq(Point point, Line line) {
        return PointLineDistanceSq(point.X(), point.Y(), line.XA(), line.YA(),
                                   line.XB(), line.YB());
    }

    public static double PointLineDistance(double pointX, double pointY,
                                           double lineX1, double lineY1,
                                           double lineX2, double lineY2) {
        return Math.sqrt(PointLineDistanceSq(pointX, pointY, lineX1, lineY1,
                                             lineX2, lineY2));
    }
    
    public static double PointLineDistance(Point point, Line line) {
        return Math.sqrt(PointLineDistanceSq(point.X(), point.Y(), line.XA(),
                                             line.YA(), line.XB(), line.YB()));
    }
}
