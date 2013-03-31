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

package Rendering;
import Core.*;

public final class Action {
    ActionType type_;
    IObjectInfo objectInfo_;
    boolean valid_;

    /*
     ** Constructors.
     */
    public Action(ActionType type) {
        type_ = type;
        valid_ = true;
    }

    public Action(ActionType type, IObjectInfo info) {
        this(type);
        objectInfo_ = info;
    }

    public static Action ObjectSelected(IObjectInfo info) {
        return new Action(ActionType.ObjectSelected, info);
    }

    public static Action ObjectMoved(IObjectInfo info) {
        return new Action(ActionType.ObjectMoved, info);
    }

    public static Action ObjectHovered(IObjectInfo info) {
        return new Action(ActionType.ObjectHovered, info);
    }

    public static Action ObjectAdded(IObjectInfo info) {
        return new Action(ActionType.ObjectAdded, info);
    }
    
    public static Action ObjectRemoved(IObjectInfo info) {
        return new Action(ActionType.ObjectRemoved, info);
    }

    public static Action MapPanned(IObjectInfo info) {
        return new Action(ActionType.MapPanned, info);
    }

    public static Action MapZoomed(IObjectInfo info) {
        return new Action(ActionType.MapZoomed, info);
    }

    /*
     ** Public methods.
     */
    public ActionType Type() { 
        return type_; 
    }
    
    public IObjectInfo ObjectInfo() { 
        return objectInfo_; 
    }
    
    public void SetObjectInfo(IObjectInfo value) { 
        objectInfo_ = value; 
    }

    public boolean Valid() { 
        return valid_; 
    }
    
    public void SetValid(boolean value) { 
        valid_ = value; 
    }

    @Override
    public String toString() {
        return "Action: " + type_.toString() + " - " + 
                objectInfo_.Type().toString();
    }
}
