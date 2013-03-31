// Copyright (c) 2010 Bebeselea Elena. All rights reserved.
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

package Core;
import java.io.*;

public final class Point implements Serializable{
    private double x_;
    private double y_;
    
    public static final Point Zero = new Point(0.0, 0.0);
    public static final double Epsilon = 0.0001; // Eroare max. admisa.

    /*
     ** Constructors.
     */
    public Point() {
        x_ = 0;
        y_ = 0;
    }

    public Point(Point other) {
        x_ = other.x_;
        y_ = other.y_;
    }

    public Point(double x, double y) {
        x_ = x;
        y_ = y;
    }

    /*
     ** Public methods.
     */
    public double X() { return x_; }
    public void SetX(double value) { x_ = value; }

    public double Y() { return y_; }
    public void SetY(double value) { y_ = value; }

    public static double Distance(double x1, double y1, 
                                  double x2, double y2) {
        double distanceSq = ((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
        return Math.sqrt(distanceSq);
    }

    public static double Distance(Point a, Point b) {
        return Point.Distance(a.x_, a.y_, b.x_, b.y_);
    }

    public double Distance(double otherX, double otherY) {
        return Point.Distance(this.x_, this.y_, otherX, otherY);
    }

    public double Distance(Point other) {
        return Distance(other.x_, other.y_);
    }

    public Point Offset(double dx, double dy) {
        x_ += dx;
        y_ += dy;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(this.getClass() != obj.getClass()) return false;

        Point point = (Point)obj;
        return (Math.abs(point.x_ - x_) < Epsilon) &&
               (Math.abs(point.y_ - y_) < Epsilon);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.x_) ^
                                (Double.doubleToLongBits(this.x_) >>> 32));
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.y_) ^
                                (Double.doubleToLongBits(this.y_) >>> 32));
        return hash;
    }

    // Pentru debugging.
    @Override
    public String toString() {
        return "X=" + x_ + ", Y=" + y_;
    }
}
