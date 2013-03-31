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

package Rendering.Info;
import Core.*;
import Rendering.*;

public final class MarkerInfo implements IObjectInfo {
    private Marker marker_;
    private ILayer layer_;
    private IRenderer parent_;
    private Coordinates coordinates_;
    private Point position_;
    private Node nearestNode_;

    /*
     ** Constructors.
     */
    public MarkerInfo(Marker marker, ILayer layer, IRenderer parent) {
        marker_ = marker;
        layer_ = layer;
        parent_ = parent;
    }

    public MarkerInfo(Marker marker, ILayer layer, IRenderer parent,
                      Point position, Coordinates coord, Node nearest) {
        this(marker, layer, parent);
        position_ = position;
        coordinates_ = coord;
        nearestNode_ = nearest;
    }

    /*
     ** Public methods.
     */
    public InfoType Type() { 
        return InfoType.Marker; }
    
    
    public IRenderer Parent() { 
        return parent_; 
    }     
    
    public Marker Marker() { 
        return marker_; 
    }
    
    public ILayer Layer() { 
        return layer_; 
    }

    public Point Position() { 
        return position_; 
    }
    
    public void SetPosition(Point value) { 
        position_ = value;
    }
    
    public Coordinates Coordinates() { 
        return coordinates_; 
    }
    
    public void SetPosition(Coordinates value) { 
        coordinates_ = value; 
    }

    public Node NearestNode() { 
        return nearestNode_; 
    }
    
    public void SetNearestNode(Node value) { 
        nearestNode_ = value; 
    }
}
