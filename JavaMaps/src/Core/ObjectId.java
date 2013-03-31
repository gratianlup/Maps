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

package Core;
import java.io.*;

public class ObjectId implements Serializable {
    private static int lastId_;
    private int id_;
    
    /*
     ** Constructors.
     */
    static {
        // ID-urile permise sunt intre 0 si Integer.MAX_VALUE.
        lastId_ = 0;
    }

    public ObjectId(int id) {
        id_ = id;
    }

    public ObjectId(ObjectId other) {
        id_ = other.id_;
    }

    /*
     ** Public methods.
     */
    // Genereaza un nou ID.
    public static synchronized ObjectId NewId() {
        return new ObjectId(lastId_++);
    }

    public static synchronized void ResetId() {
        lastId_ = 0;
    }

    public static synchronized void SetStartId(int id) {
        assert(id >= 0);
        assert(id < Integer.MAX_VALUE);
        // ------------------------------------------------
        lastId_ = id;
    }

    // Nu este recomandata folosirea. Pentru a testa daca doua
    // ID-uri sunt egale se foloseste a.equals(b).
    public int Id() { return id_; }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(this.getClass() != obj.getClass()) return false;

        return id_ == ((ObjectId)obj).id_;
    }

    @Override
    public int hashCode() {
        return id_;
    }

    // Pentru debugging.
    @Override
    public String toString() {
        return "ID=" + id_;
    }
}
