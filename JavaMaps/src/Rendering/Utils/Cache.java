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

package Rendering.Utils;
import Core.*;
import java.util.*;

public final class Cache<T> {
    class ObjectMap<K, V> extends LinkedHashMap<K, V> {
        private final int capacity_;
        
        public ObjectMap(int capacity) {
            super(capacity, 0.75f, true /* access order */);
            capacity_ = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            // Daca cache-ul este plin ultimul element accesat este eliminat.
            return size() > capacity_;
        }
    }

    /*
     ** Members.
     */
    private ObjectMap<ObjectId, T> items_;

    /*
     ** Constructors.
     */
    public Cache(int capacity) {
        items_ = new ObjectMap(capacity);
    }

    /*
     ** Public methods.
     */
    // Adauga un obiect in cache. Daca nu mai este loc ultimul
    // obiect care a fost accesat este eliminat.
    public synchronized void Add(T item, ObjectId id) {
        if(items_.containsKey(id)) return;
        items_.put(id, item);
    }

    // Obtine un obiect din cache pe baza ID-ului.
    // Daca obiectul nu este gasit se returneaza null.
    public synchronized T Get(ObjectId id) {
        return items_.get(id);
    }

    // Verifica daca un obiect cu ID-ul dat se gaseste in cache.
    public synchronized boolean Contains(ObjectId id) {
        return items_.containsKey(id);
    }

    public synchronized void Remove(ObjectId id) {
        items_.remove(id);
    }

    // Sterge toate obiectele din cache.
    public synchronized void Clear() {
        items_.clear();
    }

    public synchronized int Capacity() { 
        return items_.capacity_; 
    }
    
    public synchronized int Count() { 
        return items_.size(); 
    }
}
