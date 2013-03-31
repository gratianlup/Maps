// Copyright (c) 2010 Elena Bebeselea. All rights reserved.
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

package Data;
import Core.Marker;
import java.util.*;
import Core.*;
import java.io.*;

public class MarkerLayer implements ILayer, Serializable{
    private ObjectId ID_;
    private String name_;
    private String icon_;
    private int markerNumber_;
    private java.util.Map<ObjectId, Marker> markers_;

    /*
     ** Constructors.
     */
     public MarkerLayer(){
        ID_ = new ObjectId(0);
        name_ = "";
        icon_ = "";
        markerNumber_ = 0;
        markers_ = new HashMap<ObjectId, Marker>();
     }

     public MarkerLayer(ObjectId ID, String name, String icon){
        ID_ = ID;
        name_ = name;
        icon_ = icon;
        markerNumber_ = 0;
        markers_ = new HashMap<ObjectId, Marker>();
     }

     /*
     ** Public methods.
     */
     public ObjectId ID(){ return ID_; }
     public void SetID(ObjectId ID){ ID_ = ID; }

     public String Name(){ return name_; }
     public void SetName(String name){ name_ = name; }

     public LayerType Type(){ return LayerType.Marker; }

     public String Icon(){ return icon_; }
     public void SetIcon(String icon){ icon_ = icon; }

     public int MarkerNumber(){ return markerNumber_; }
     public void SetMarkerNumber(int markerNumber){ markerNumber_ = markerNumber; }

     //Metode ce ofera acces la HashMap-ul markers_.
     public Marker GetMarker(ObjectId ID){ return markers_.get(ID); }
     public void Add(ObjectId ID, Marker marker){
         markers_.put(ID, marker );
         markerNumber_++;
     }
     public boolean Contains(ObjectId ID){ return  markers_.containsKey(ID); }
     public void Remove(ObjectId ID){
         markers_.remove(ID);
         markerNumber_ --;
     }
     public void Clear(){ 
         markers_.clear();
         markerNumber_ = 0;
     }

     public Iterator<Marker> Iterator(){
         Collection c = markers_.values();
         Iterator i = c.iterator();
         return i;
     }
}
