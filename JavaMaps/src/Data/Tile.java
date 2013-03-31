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

public class Tile implements Serializable {
    private ObjectId ID_;
    private int level_;
    private String fileName_;

    /*
     ** Constructors.
     */
    public Tile(){
    }

    public Tile(ObjectId ID, int level, String fileName){
        ID_ = ID;
        level_ = level;
        fileName_ = fileName;
    }

     /*
     ** Public methods.
     */
     public ObjectId Id(){ return ID_; }
     public void SetId(ObjectId  ID){ ID_ = ID; }

     public int Level(){ return level_; }
     public void SetLevel(int level){ level_ = level; }

     public String FileName(){ return fileName_; }
     public void SetFileName(String fileName){ fileName_ = fileName; }



     @Override
     public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(this.getClass() != obj.getClass()) return false;

        Tile tile = (Tile)obj;
        return ( tile.ID_ == ID_ );
    }

     @Override
     public int hashCode() { return ID_.Id();}

     // Pentru debugging.
     @Override
     public String toString() {
        return "Id=" + ID_ + ", lev=" + level_ + "file=" + fileName_;
    }
}
