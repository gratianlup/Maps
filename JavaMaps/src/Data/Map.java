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

public class Map implements Serializable {
    private String name_;
    private ProjectionType projectionType_;
    private int layerNumber_;
    private List<ILayer> layers_;

    /*
     ** Constructors.
     */
    public Map(){
        name_ = "test";
        projectionType_ = ProjectionType.Mercator;
        layerNumber_ = 0;
        layers_ = new ArrayList<ILayer>();
    }
    public Map(String name, ProjectionType projectionType){
        name_ = name;
        projectionType_ = projectionType;
        layerNumber_ = 0;
        layers_ = new ArrayList<ILayer>();
    }

     /*
     ** Public methods.
     */
    public String Name(){ return name_; }
    public void SetName(String name){ name_ = name; }

    public ProjectionType ProjectionType(){ return projectionType_; }
    public void SetProjectionType(ProjectionType projectionType){
        projectionType_ = projectionType;
    }

    public int LayerNumber(){ return layerNumber_; }
    public void SetLayerNumber(int layerNumber){ layerNumber_ = layerNumber; }

    public List<ILayer> Layers(){ return layers_; }

    //Metode ce ofera acces la elementele listei layers_.
    public void AddLayer(ILayer element){
        layers_.add(element);
        layerNumber_ ++;
    }
    public void SetLayer(int index, ILayer element){
        layers_.set(index, element);
    }

    public boolean Contains(ILayer element){ return layers_.contains(element); }

    public void Clear(){
        layers_.clear();
        layerNumber_ = 0;
    }
    public void RemoveLayer(ILayer element){
        layers_.remove(element);
        layerNumber_ --;
    }

    public ILayer Layer(int index){ return layers_.get(index); }

    //Se returneaza un iterator pentru lista de link-uri.
    public Iterator<ILayer> Iterator(){
        Iterator i = layers_.iterator();
        return i;
    }
}
