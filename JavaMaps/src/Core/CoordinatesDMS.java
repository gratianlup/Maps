// Copyright (c) 2010 Ramona Maris. All rights reserved.
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

//clasa ce reprezinta o coordonata in grade,minute si secunde
public class CoordinatesDMS implements Serializable{
    private int degrees_;
    private int minutes_;
    private int seconds_;

    public static final double Epsilon = 0.0001; // Eroare max. admisa.

    /*
     ** Constructors.
     */
    public CoordinatesDMS(){
        degrees_ = 0;
        minutes_ = 0;
        seconds_ = 0;
    }

    public CoordinatesDMS(CoordinatesDMS coordinatesDMS){
        degrees_ = coordinatesDMS.degrees_;
        minutes_ = coordinatesDMS.minutes_;
        seconds_ = coordinatesDMS.seconds_;
    }

    public CoordinatesDMS(int degrees, int minutes, int seconds){
        degrees_ = degrees;
        minutes_ = minutes;
        seconds_ = seconds;
    }

     /*
     ** Public methods.
     */

    public int Degrees(){ return degrees_; }
    public void SetDegrees(int value){degrees_ = value;}

    public int Minutes(){ return minutes_; }
    public void SetMinutes(int value){minutes_ = value;}

    public int Seconds(){ return seconds_; }
    public void SetSeconds(int value){seconds_ = value;}

    public double ConvertToDegrees(){
        double coordinate = (double)((double)(seconds_)/60+minutes_)/60 + degrees_;
        return coordinate;
    }

    @Override
    public boolean equals(Object obj) {
       if(obj == null) return false;
       if(this == obj) return true;
       if(this.getClass() != obj.getClass()) return false;

       CoordinatesDMS coordinatesDMS1 = (CoordinatesDMS)obj;
       return (Math.abs(coordinatesDMS1.Degrees() - degrees_ ) < Epsilon) &&
              (Math.abs(coordinatesDMS1.Minutes() - minutes_) < Epsilon ) &&
              (Math.abs(coordinatesDMS1.Seconds() -  seconds_) < Epsilon );
    }

    @Override
    public int hashCode() {
       int hash = 7;
       hash = 59 * hash + degrees_;
       hash = 59 * hash + minutes_;
       hash = 59 * hash + seconds_;
       return hash;
    }

    // Pentru debugging.
    @Override
    public String toString() {
        return "Degrees=" + degrees_ + ", Minutes=" + minutes_ + ", Seconds" + seconds_;
    }
}
