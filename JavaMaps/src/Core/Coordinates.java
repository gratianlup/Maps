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
import java.lang.Object.*;
import java.io.*;

// Clasa ce reprezinta o pereche de coordonate in spatiul 2D (pixeli).
public final class Coordinates implements Serializable {
    double latitude_;
    double longitude_;

    public static final double Epsilon = 0.0001; // Eroare max. admisa.

    /*
     ** Constructors.
     */
    public Coordinates(){
        latitude_ = 0;
        longitude_ = 0;
    }

    public Coordinates(Coordinates other){
        latitude_ = other.latitude_;
        longitude_ = other.longitude_;
    }

    public Coordinates(double latitude,double longitude){
        latitude_ = latitude;
        longitude_ = longitude;
    }

    /*
     ** Public methods.
     */
    public double Longitude() { return longitude_; }
    public void SetLongitude(double value) { longitude_ = value; }

    public double Latitude() { return latitude_; }
    public void SetLatitude(double value) { latitude_ = value; }

    // Distanta dintre doua coordonate in km.
    public static double Distance(double latitude1, double longitude1,
                                  double latitude2, double longitude2) {

         double a = 6378137, b = 6356752.3142,  f = 1/298.257223563; 
         double L= Math.toRadians(longitude2-longitude1);
         double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(latitude1)));
         double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(latitude2)));
         double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
         double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

         double lambda = L, lambdaP, iterLimit = 100;
         double sinSigma, cosSqAlpha, cosSigma, cos2SigmaM, sigma;
         do {
            double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) +
            (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));
            if (sinSigma==0) return 0;  

            cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha*sinAlpha;
            cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) cos2SigmaM = 0;  
            double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1-C) * f * sinAlpha *
                     (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
        } while(Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

        if(iterLimit == 0) return Double.NaN; 

        double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
        double A = 1 + uSq / 16384 * (4096+uSq*(-768+uSq*(320-175*uSq)));
        double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
        double deltaSigma = B  *sinSigma  *(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
        B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
        double s = b*A*(sigma-deltaSigma);

        s = s / 1000; //transformare din m in km
        return s;
    }

    public static double Distance(Coordinates a, Coordinates b){
        return Coordinates.Distance(a.latitude_, a.longitude_, b.latitude_, b.longitude_);
    }

    public double Distance(double otherLatitude, double otherLongitude){
        return Coordinates.Distance(latitude_, longitude_, otherLatitude, otherLongitude);
    }

    public double Distance(Coordinates other){
        return Coordinates.Distance(latitude_, longitude_, other.latitude_, other.longitude_);
    }

    // Se converteste o coordonata in DMS.
    public static CoordinatesDMS ConvertToDMS(double coordinate){
        int degrees = (int)coordinate;
        coordinate = coordinate - degrees;
        coordinate = coordinate*60;
        int minutes = (int)(coordinate);
        coordinate = coordinate - minutes;
        coordinate = coordinate*60;
        int seconds = (int)(coordinate);
        CoordinatesDMS coordinatesDMS1 = new CoordinatesDMS(degrees, minutes, seconds);
        return coordinatesDMS1;
    }

    @Override
     public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(this.getClass() != obj.getClass()) return false;

        Coordinates coordinates1 = (Coordinates)obj;
        return (Math.abs(coordinates1.Latitude() - latitude_) < Epsilon) &&
                (Math.abs(coordinates1.Longitude() - longitude_) < Epsilon );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.latitude_) ^
                                (Double.doubleToLongBits(this.latitude_) >>> 32));
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.longitude_) ^
                                (Double.doubleToLongBits(this.longitude_) >>> 32));
        return hash;
    }

    // Pentru debugging.
    @Override
    public String toString() {
        return "Lat=" + latitude_ + ", Long=" + longitude_;
    }
}
