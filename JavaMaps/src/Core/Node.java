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
import java.util.*;
import java.io.*;

public class Node implements Serializable {
    private ObjectId id_;
    private Coordinates coordinates_;
    private Map<ObjectId, Link> links_;

    public static final double Epsilon = 0.0001; // Eroare max. admisa.

     /*
      ** Constructors.
      */
    public Node(ObjectId id, Coordinates coordinates){
        id_ = id;
        coordinates_ = coordinates;
        links_ = new HashMap<ObjectId,Link>();
    }

   public Node(ObjectId id, double latitude, double longitude){
        id_ = id;
        coordinates_ = new Coordinates(latitude, longitude);
        links_ = new HashMap<ObjectId,Link>();
   }
   
    /*
     ** Public methods.
     */

   public ObjectId Id(){ return id_; }
   public void SetId( ObjectId nodeId ){
       id_ = nodeId;
   }
   
   public Coordinates Coordinates(){ return coordinates_;}
   public void SetCoordinates( Coordinates coordinates1 ){
       coordinates_ = coordinates1;
   }

   //Metode ce ofera acces la HashMap-ul links_.
   //Se returneaza HashMap-ul links_.
   public Map<ObjectId,Link> Links(){ return links_;}

   //Se returneaza link-ul cu id-ul primit ca parametru.
   public Link Link(ObjectId id){ return links_.get(id);}

   //Se adauga un link in HashMap-ul links_.
   public void AddLink( Link element ){
        links_.put(element.LinkId(), element);
   }

   //Se verifica daca un link apartine HashMap-ului .
   public boolean ContainsLink(Link element){
        return links_.containsKey(element.LinkId());
   }

   //Se sterge un link din HashMap-ul links_.
   public void DeleteLink( ObjectId id){
        links_.remove(id);
       }

   //Se sterg link-urile din HashMap.
   public void ClearLinks(){
        links_.clear();
   }

   //Se returneaza un iterator pentru HashMap-ul links_.
   public Iterator<Link> Iterator(){
        Collection collection = links_.values();
        Iterator i = collection.iterator();
        return i;
   }

   //Pentru debugging.
   @Override
   public String toString(){
        return id_ + "; " + coordinates_;
  }
}
