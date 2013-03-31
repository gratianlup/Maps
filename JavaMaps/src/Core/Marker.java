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

package Core;
import java.io.*;

public class Marker implements Serializable {
    private ObjectId id_;
    private Coordinates coordinates_;
    private String name_;
    private ObjectId nearestNode_;
    private MarkerPriority priority_;
    
    /*
     ** Constructors.
     */
    public Marker(ObjectId id, Coordinates coordinates) {
        id_ = id;
        coordinates_ = coordinates;
    }

    public Marker(ObjectId id, Coordinates coordinates, String name) {
        this(id, coordinates);
        name_ = name;
    }

    public Marker(ObjectId id, Coordinates coordinates, String name,
                  ObjectId nearestNode, MarkerPriority priority){
        this(id, coordinates, name);
        nearestNode_ = nearestNode;
        priority_ = priority;
    }
    
    /*
     ** Public methods.
     */
    public ObjectId ID(){ return id_; }
    public void SetID(ObjectId ID){ id_ = ID; }
    
    public Coordinates Coordinates(){ return coordinates_; }
    public void SetCoordinates(Coordinates coordinates){
        coordinates_ =  coordinates; 
    }
    
    public String Name(){ return name_; }
    public void SetName(String name){ name_ = name; }
    
    public ObjectId NearestNode(){ return nearestNode_; }
    public void SetNearestNode(ObjectId nearestNode){ nearestNode_ = nearestNode; }
    
    public MarkerPriority Priority(){ return priority_; }
    public void SetMarkerPriority(MarkerPriority priority){ priority_ = priority; }

     @Override
     public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(this.getClass() != obj.getClass()) return false;

        Marker marker = (Marker)obj;
        return ( marker.id_ == id_ );
    }

     @Override
     public int hashCode() { return id_.Id();}
}
