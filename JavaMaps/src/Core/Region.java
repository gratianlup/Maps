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

public final class Region implements Serializable {
    private Coordinates topLeft_;
    private Coordinates bottomRight_;

     public static final double Epsilon = 0.0001; // Eroare max. admisa.
     
    /*
     ** Constructors.
     */
    public Region(){
        topLeft_ = new Coordinates(0,0);
        bottomRight_ = new Coordinates(0,0);
    }

    public Region(Coordinates topLeft, Coordinates bottomRight){
        topLeft_ = topLeft;
        bottomRight_ = bottomRight;
    }

    public Region(double lat1, double long1, double lat2, double long2){
        topLeft_ = new Coordinates(lat1, long1);
        bottomRight_ = new Coordinates(lat2, long2);
    }

    public Region(Region region1){
        topLeft_ = new Coordinates(region1.topLeft_.Latitude(), region1.topLeft_.Longitude());
        bottomRight_ =  new Coordinates(region1.bottomRight_.Latitude(), region1.bottomRight_.Longitude());
    }

    /*
     ** Public methods.
     */

    public Coordinates TopLeft() { return topLeft_; }
    public void SettopLeft(Coordinates topLeft) { topLeft_ = topLeft; }
    public Coordinates BottomRight() { return bottomRight_; }
    public void SetbottomRight(Coordinates bottomRight) { topLeft_ = bottomRight; }

    //Verifica daca regiunea este vida.
    public boolean IsEmpty(){
        return (topLeft_.latitude_ == 0 && topLeft_.longitude_ == 0) &&
                (bottomRight_.latitude_ == 0 && bottomRight_.longitude_ == 0);
    }

    //Deplaseaza regiunile cu valorile date.
    public void Offset(double latitude, double longitude){
        topLeft_.latitude_ += latitude;
        topLeft_.longitude_ += longitude;
        bottomRight_.latitude_ += latitude;
        bottomRight_.longitude_ += longitude;
    }

    //Deplaseaza regiunea cu valorile din variabila de tip Coordinates.
    public void Offset(Coordinates coordinate){
        Offset(coordinate.latitude_, coordinate.longitude_);
    }

    //Verifica daca coordonatele date se afla in regiune.
    public boolean Contains(double latitude, double longitude){
        return (topLeft_.latitude_ <= latitude && latitude <= bottomRight_.latitude_) &&
               (topLeft_.longitude_ <= longitude && longitude <= bottomRight_.longitude_);
    }

    //Verifica daca o structura de date Coordinates se afla in regiune.
    public boolean Contains(Coordinates coordinates1){
        return Contains(coordinates1.latitude_, coordinates1.longitude_);
    }

    // Verifica daca regiunea data este continuta in totalitate.
    public boolean Contains(Region region1){
        return (topLeft_.Latitude() <= region1.TopLeft().Latitude() && 
                topLeft_.Longitude() <= region1.topLeft_.Longitude()) &&
               (region1.BottomRight().Latitude() <= bottomRight_.Latitude() && 
                region1.BottomRight().Longitude() <= bottomRight_.Longitude());
    }

    // Verifica daca regiunea formata din coordonatele date
    // se intersecteaza cu actuala regiune.
    public boolean IntersectsWith(double lat1, double long1, double lat2, double long2){
       return Contains(lat1, long1)|| Contains(lat2, long2);
    }

    // Verifica daca regiunea data se intersecteaza cu actuala regiune.
    public boolean IntersectsWith(Region region2){
        return Contains(region2.TopLeft().Latitude(), region2.TopLeft().Longitude()) ||
               Contains(region2.BottomRight().Latitude(), region2.BottomRight().Longitude());
    }


    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(this == obj) return true;
        if(this.getClass() != obj.getClass()) return false;

        Region region1 = (Region)obj;
        return (Math.abs(region1.TopLeft().Latitude() - topLeft_.Latitude()) < Epsilon ) &&
               (Math.abs(region1.TopLeft().Longitude() - topLeft_.Longitude()) < Epsilon) &&
               (Math.abs(region1.BottomRight().Latitude() - bottomRight_.Latitude()) < Epsilon ) &&
               (Math.abs(region1.BottomRight().Longitude() - bottomRight_.Longitude()) < Epsilon);
   }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.topLeft_.Latitude()) ^
                                (Double.doubleToLongBits(this.topLeft_.Latitude()) >>> 32));
        hash = 59 * hash + (int)(Double.doubleToLongBits(this.bottomRight_.Longitude()) ^
                                (Double.doubleToLongBits(this.bottomRight_.Longitude()) >>> 32));
        return hash;
    }

    // Pentru debugging.
    @Override
    public String toString() {
        return topLeft_.Latitude() + ", " + topLeft_.Longitude() + "; " +
               bottomRight_.Latitude() + "," + bottomRight_.Longitude();
    }

}
