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

package Data;
import Core.*;
import java.io.Serializable;
import java.util.*;

public class RoadLayer implements ILayer, Serializable {
    private ObjectId id_;
    private Color streetColor_;
    private Color avenueColor_;
    private Color boulevardColor_;
    private int nodeCount_;
    private java.util.Map<ObjectId,Node> nodes_;
    private int streetCount_;
    private java.util.Map<ObjectId,Street> streets_;

    /*
     ** Constructors.
     */
    public RoadLayer(ObjectId id, Color streetColor, Color avenueColor, Color boulevardColor){
        id_ = id;
        streetColor_ = streetColor;
        avenueColor_ = avenueColor;
        boulevardColor_ = boulevardColor;
        nodeCount_ = 0;
        nodes_ = new HashMap<ObjectId,Node>();
        streetCount_ = 0;
        streets_ = new HashMap<ObjectId,Street>();
    }

    /*
     ** Public methods.
     */

    public void SetId( ObjectId id ){ id_ = id;}
    public Color StreetColor(){ return streetColor_;}
    public void GetStreetColor( Color color1 ){ streetColor_ = color1;}
    public Color AvenueColor(){ return avenueColor_;}
    public void GetAvenueColor( Color color1 ){ avenueColor_ = color1;}
    public Color BoulevardColor(){ return boulevardColor_;}
    public void GetBoulevardColor( Color color1 ){ boulevardColor_ = color1;}
    public int NodeCount(){ return nodeCount_; }
    public void SetNodeCount( int nodeCount ){ nodeCount_ = nodeCount;}
    public int StreetCount(){ return streetCount_; }
    public void SetStreetCount( int streetCount ){ streetCount_ = streetCount;}

    //Metode ce ofera acces la HashMap-ul nodes_.
    //Se adauga un nod in HashMap-ul nodes_.
    public void AddNode( Node element ){
        nodes_.put(element.Id(), element);
        nodeCount_++;
    }

    //Se verifica daca un nod se afla in HashMap-ul nodes_.
    public boolean ContainsNode( ObjectId id){
        return nodes_.containsKey(id);
    }


    //Se sterge un nod din HashMap-ul nodes_.
    public void DeleteNode( ObjectId id ){
        nodes_.remove(id);
        nodeCount_--;
    }

    //Se sterg elementele HashMap-ului nodes_.
    public void ClearNodes(){
        nodes_.clear();
        nodeCount_ = 0;
    }

    //Se returneaza un iterator pentru HashMap-ul nodes_.
    public Iterator<Node> IteratorNode(){
        Collection collection = nodes_.values();
        Iterator i = collection.iterator();
        return i;
    }

    
    //Metode ce ofera acces la HashMap-ul streets_.
     //Se adauga o strada in HashMap-ul streets_.
    public void AddStreet( Street element ){
        streets_.put(element.Id(), element);
        streetCount_++;
    }

    //Se verifica daca o strada se afla in HashMap-ul streets_.
    public boolean ContainsStreet( ObjectId id ){
        return streets_.containsKey(id);
    }

    //Se sterge o strada din HashMap-ul streets_.
    public void DeleteStreet( ObjectId id ){
        streets_.remove(id);
        streetCount_--;
    }

    //Se sterg elementele HashMap-ului streets_.
    public void ClearStreets(){
        streets_.clear();
        streetCount_ = 0;
    }

    //Se returneaza un iterator pentru HashMap-ul streets_.
    public Iterator<Street> IteratorStreet(){
        Collection collection = streets_.values();
        Iterator i = collection.iterator();
        return i;
    }


    //Se implementeaza metode pentru a inlocui metodele abstracte din ILayer.
    public ObjectId ID(){ return id_;}
    public String Name(){ return "RoadLayer";}
    public LayerType Type(){ return LayerType.Street; }



}
