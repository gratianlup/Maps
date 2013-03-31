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

public final class Line {
    private double xa_;
    private double ya_;
    private double xb_;
    private double yb_;
    private Object value_;
    private short nameCount_;
    private short length_;
    private short nameWidth_;
    private short nameHeight_;

    // ------------------------------------------------
    public Line(double xa, double ya, double xb, double yb, Object value) {
        value_ = value;
        xa_ = xa;
        ya_ = ya;
        xb_ = xb;
        yb_ = yb;
    }

    public Line(Point a, Point b, Object value) {
        this(a.X(), a.Y(), b.X(), b.Y(), value);
    }

    // ------------------------------------------------
    public double XA() { 
        return xa_; 
    }
    
    public void SetXA(double value) { 
        xa_ = value;
    }

    public double YA() {
        return ya_; 
    }
    
    public void SetYA(double value) { 
        ya_ = value; 
    }

    public double XB() { 
        return xb_;
    }
    
    public void SetXB(double value) { 
        xb_ = value; 
    }

    public double YB() { 
        return yb_; 
    }
    
    public void SetYB(double value) { 
        xb_ = value; 
    }

    public Object Value() { 
        return value_; 
    }
    
    public void SetValue(Object value) { 
        value_ = value; 
    }

    public short NameCount() { 
        return nameCount_; 
    }
    
    public void SetNameCount(short value) { 
        nameCount_ = value; 
    }

    public short NameWidth() { 
        return nameWidth_; 
    }
    
    public void SetNameWidth(short value) { 
        nameWidth_ = value; 
    }

    public short NameHeight() { 
        return nameHeight_; 
    }
    
    public void SetNameHeight(short value) { 
        nameHeight_ = value; 
    }

    public short Length() { 
        return length_; 
    }
    
    public void SetLength(short value) { 
        length_ = value;
    }

    // Pentru debugging.
    @Override
    public String toString() {
        return xa_ + "," + ya_ + "; " + xb_ + "," + yb_;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.xa_) ^
                                (Double.doubleToLongBits(this.xa_) >>> 32));
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.ya_) ^
                                (Double.doubleToLongBits(this.ya_) >>> 32));
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.xb_) ^
                                (Double.doubleToLongBits(this.xb_) >>> 32));
        hash = 53 * hash + (int)(Double.doubleToLongBits(this.yb_) ^
                                (Double.doubleToLongBits(this.yb_) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        Line other = (Line)obj;
        return (value_.equals(other.value_)) &&
               (Math.abs(xa_ - other.xa_) < Point.Epsilon) &&
               (Math.abs(ya_ - other.ya_) < Point.Epsilon) &&
               (Math.abs(xb_ - other.xb_) < Point.Epsilon) &&
               (Math.abs(yb_ - other.yb_) < Point.Epsilon);
    }
}
