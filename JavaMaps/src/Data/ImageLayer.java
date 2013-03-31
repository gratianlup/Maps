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
import Core.*;
import java.util.*;
import java.io.*;

public class ImageLayer implements ILayer, Serializable{
    private ObjectId ID_;
    private int zoomLevels_;
    private Coordinates start_;
    private Coordinates end_;
    private List<ZoomInfo> levels_;

    /*
     ** Constructors.
     */
    public ImageLayer(){
        ID_ = new ObjectId(0);
        zoomLevels_ = 0;
        start_ = new Coordinates(0, 0);
        end_ = new Coordinates(0, 0);
        levels_ = new ArrayList<ZoomInfo>();
    }
    
    public ImageLayer(ObjectId ID, int zoomLevels, Coordinates start, Coordinates end){
        ID_ = ID;
        zoomLevels_ = zoomLevels;
        start_ = start;
        end_ = end;
        levels_ = new ArrayList<ZoomInfo>();
    }
   
    /*
     ** Public methods.
     */
    public void SetID(ObjectId ID){ ID_ = ID; }

    public int ZoomLevels(){ return levels_.size(); }

    public Coordinates Start(){ return start_; }
    public void SetStart(Coordinates start){  start_ = start;}

    public Coordinates End(){ return end_; }
    public void SetEnd(Coordinates end){ end_ = end; }

    public List<ZoomInfo> Levels(){ return levels_; }

    //Metode ce ofera acces la elementele listei levels_.
    public void AddLevel( ZoomInfo element){
        levels_.add(element);
    }

    public void SetLevel(int index, ZoomInfo element){
        levels_.set(index, element);
    }

    public ZoomInfo Level(int index){ return levels_.get(index); }

    //Metode ce inlocuiesc metodele abstracte din interfata ILayer.
    public ObjectId ID(){ return ID_; }
    public String Name(){ return "Image Layer"; }
    public LayerType Type(){ return LayerType.Image; }
  
    // Pentru debugging.
     @Override
     public String toString() {
        return "ZoomLevels=" + zoomLevels_ + "start=" + start_ + "end=" + end_;
    }
    
}
