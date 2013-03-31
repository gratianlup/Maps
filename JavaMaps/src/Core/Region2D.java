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

package Core;
import java.io.*;
import java.lang.*;

public final class Region2D implements Serializable, Cloneable {
    private double x_;
    private double y_;
    private double width_;
    private double height_;

    /*
     * Constructori si metode asemanatoare.
     */
    public Region2D() {
        x_ = y_ = 0;
        width_ = height_ = 0;
    }

    public Region2D(double left, double top, double width, double height) {
        x_ = left;
        y_ = top;
        width_ = width;
        height_ = height;
    }

    public Region2D(Point start, double width, double height) {
        x_ = start.X();
        y_ = start.Y();
        width_ = width;
        height_ = height;
    }

    public Region2D(Region2D other) {
        this(other.x_, other.y_, other.width_, other.height_);
    }

    public static Region2D FromPoints(double left, double top,
                                      double right, double bottom) {
        return new Region2D(left, top, right - left, bottom - top);
    }

    public static Region2D FromPoints(Point a, Point b) {
        return FromPoints(a.X(), a.Y(), b.X(), b.Y());
    }

    /*
     ** Public methods.
     */
     public double Left() { return x_; }
     public void SetLeft(double value) { x_ = value; }
     
     public double Top() { return y_; }
     public void SetTop(double value) { y_ = value; }
     
     public double Width() { return width_; }
     public void SetWidth(double value) { width_ = value; }
     
     public double Height() { return height_; }
     public void SetHeight(double value) { height_ = value; }

     public double Right() { return x_ + width_; }
     public double Bottom() { return y_ + height_; }

     // Verifica daca regiunea este vida.
     public boolean IsEmpty() {
         return (width_ == 0) && (height_ == 0);
     }

     // Deplaseaza regiunea cu valorile date.
     public void Offset(double dx, double dy) {
         x_ += dx;
         y_ += dy;
     }

     // Deplaseaza regiunea cu valorile din punctul dat.
     public void Offset(Point amount) {
         Offset(amount.X(), amount.Y());
     }

     public void Inflate(double amountX, double amountY) {
         x_ -= amountX;
         y_ -= amountY;
         width_ += 2 * amountX;
         height_ += 2 * amountY;
     }

     // Verifica daca punctul format din coordonatele date se afla in regiune.
     public boolean Contains(double x, double y) {
         return (x_ <= x) && (y_ <= y) &&
                ((x_ + width_) >= x) && ((y_ + height_) >= y);
     }

     // Verifica daca punctul dat se afla in regiune.
     public boolean Contains(Point point) {
         return Contains(point.X(), point.Y());
     }

     // Verifica daca regiunea data este continuta in totalitate.
     public boolean Contains(Region2D region) {
         return (region.x_ >= x_) && (region.y_ >= y_) &&
                (((region.x_ + region.width_) <= (x_ + width_))) &&
                (((region.y_ + region.height_) <= (y_ + height_)));
     }

     // Verifica daca regiunea formata din coordonatele date
     // se intersecteaza cu actuala regiune.
     public boolean IntersectsWith(double left, double top,
                                   double width, double height) {
         return (left < (x_ + width_)) &&
                (x_ < (left + width)) &&
                (top < (y_ + height_)) &&
                (y_ < (top + height));
     }

     // Verifica daca regiunea data se intersecteaza cu actuala regiune.
     public boolean IntersectsWith(Region2D region) {
         return IntersectsWith(region.x_, region.y_,
                               region.width_, region.height_);
     }

     @Override
     public boolean equals(Object obj) {
         if(obj == null) return false;
         if(this == obj) return true;
         if(this.getClass() != obj.getClass()) return false;

         Region2D other = (Region2D)obj;
         return (Math.abs(other.x_ - x_) < Point.Epsilon) &&
                (Math.abs(other.y_ - y_) < Point.Epsilon) &&
                (Math.abs(other.width_ - width_) < Point.Epsilon) &&
                (Math.abs(other.height_ - height_) < Point.Epsilon);
     }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (int)(Double.doubleToLongBits(this.x_) ^
                                (Double.doubleToLongBits(this.x_) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(this.y_) ^
                                (Double.doubleToLongBits(this.y_) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(this.width_) ^
                                (Double.doubleToLongBits(this.width_) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(this.height_) ^
                                (Double.doubleToLongBits(this.height_) >>> 32));
        return hash;
    }

    // Pentru debugging.
    @Override
    public String toString() {
        return x_ + ", " + y_ + "; " + width_ + "x" + height_;
    }
}
