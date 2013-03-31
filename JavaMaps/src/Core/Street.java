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

public final class Street implements Serializable {
    private ObjectId id_;
    private String name_;
    private StreetType type_;
    private Node startNode_;
    private Node endNode_;
    private ArrayList<Coordinates> coordinatesList_;

    /*
     ** Constructors.
     */
    public Street(ObjectId id, StreetType type) {
        id_ = id;
        coordinatesList_ = new ArrayList<Coordinates>();
        type_ = type;
    }

    public Street(ObjectId id, StreetType type, String name,
                  Node startNode, Node endNode){
        this(id, type);
        name_ = name;
        startNode_ = startNode;
        endNode_ = endNode;
    }

    /*
     ** Public methods.
     */
    public ObjectId Id(){ return id_;}
    public void SetId( ObjectId id ){ id_ = id; }

    public String Name(){ return name_;}
    public void SetName( String name ) { name_ = name; }

    public StreetType Type(){ return type_; }
    public void SetType( StreetType type ){ type_ = type; }

    public Node StartNode(){ return startNode_;}
    public void SetStartNode( Node startNode ) { startNode_ = startNode; }

    public Node EndNode(){ return endNode_;}
    public void SetEndNode( Node endNode ) { endNode_ = endNode; }

    // Metode ce acceseaza lista de coordonate.
    public List<Coordinates> Coordinates() { return coordinatesList_; }

    // Se adauga o coordonata in ArrayList-ul coordinatesList_.
    public void AddCoordinate(Coordinates coordinate1) {
        coordinatesList_.add(coordinate1);
    }

    // Se verifica daca o coordonata apartine listei de coordonate
    public void ContainsCoordinate(Coordinates coordinate) {
        coordinatesList_.contains(coordinate);
    }
    
    // Se sterge o coordonata din ArrayList-ul coordinatesList_.
    public void DeleteCoordinate(Coordinates coordinate) {
        coordinatesList_.remove(coordinate);
    }

    // Se sterge lista de coordonate
    public void ClearCoordinates() {
        coordinatesList_.clear();
    }
}
