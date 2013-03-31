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

public final class Link implements Serializable {
    private ObjectId linkId_;
    private ObjectId nodeId_;
    private ObjectId streetId_;
    private int startPoint_;
    private int endPoint_;
    private double distance_;

    /*
     ** Constructors.
     */
    public Link(ObjectId linkId, ObjectId nodeId, ObjectId streetId) {
        linkId_ = linkId;
        nodeId_ = nodeId;
        streetId_ = streetId;
    }

    public Link(ObjectId linkId, ObjectId nodeId, ObjectId streetId,
                int startPoint, int endPoint, double distance) {
         this(linkId, nodeId, streetId);
         startPoint_ = startPoint;
         endPoint_ = endPoint;
         distance_ = distance;
    }
   
    /*
     ** Public methods.
     */
    public ObjectId LinkId(){ return linkId_; }
    public void SetLinkId(ObjectId id){ linkId_ = id; }

    public ObjectId NodeId(){ return nodeId_; }
    public void SetNodeId(ObjectId id){ nodeId_= id; }

    public ObjectId StreetId(){ return streetId_; }
    public void SetStreetId(ObjectId id){ streetId_= id; }

    public int StartPoint(){ return startPoint_;}
    public void SetStartPoint(int value){ startPoint_ = value;}

    public int EndPoint(){ return endPoint_;}
    public void SetEndPoint(int value){ endPoint_ = value;}

    public double Distance(){ return distance_; }
    public void SetDistance(double distance){ distance_ = distance; }

    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(this == obj) return true;
        if(this.getClass() != obj.getClass()) return false;

        Link link = (Link)obj;
        return link.linkId_.equals(linkId_);
   }

    @Override
    public int hashCode() {
        return linkId_.hashCode();
    }

    //Pentru debugging.
    @Override
    public String toString(){
        return "L=" + linkId_ +", N=" + nodeId_ +", S=" + streetId_ +
                ", Start=" + startPoint_ +", End=" + endPoint_;
    }
}
