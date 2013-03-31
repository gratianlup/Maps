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

package Rendering.Renderers;
import Core.*;
import Rendering.Utils.*;
import java.awt.Image;
import java.util.ArrayList;

public class ImageCache {
    private static int MAX_IMAGES = 128;

    /*
     ** Members.
     */
    private ArrayList<Cache<Image>> cache_;

    /*
     ** Constructors.
     */
    public ImageCache(IMapProvider provider) {
        // The first zoom level is cached entirely.
        cache_ = new ArrayList<Cache<Image>>(2);
        cache_.add(new Cache<Image>(provider.TileCount(0)));
        cache_.add(new Cache<Image>(MAX_IMAGES - provider.TileCount(0)));
    }

    /*
     ** Public methods.
     */
    public void Add(Image image, ObjectId id, int zoom) {
        cache_.get(Select(zoom)).Add(image, id);
    }

    public Image Get(ObjectId id, int zoom) {
        return cache_.get(Select(zoom)).Get(id);
    }

    public boolean Contains(ObjectId id, int zoom) {
        return cache_.get(Select(zoom)).Contains(id);
    }

    public void Remove(ObjectId id, int zoom) {
        cache_.get(Select(zoom)).Remove(id);
    }

    public void Clear(int zoom) {
        cache_.get(Select(zoom)).Clear();
    }

    public void Clear() {
        for(int i = 0; i < cache_.size(); i++) {
            cache_.get(i).Clear();
        }
    }

    public int Capacity(int zoom) {
        return cache_.get(Select(zoom)).Capacity();
    }

    public int Capacity() {
        int ct = 0;
        
        for(int i = 0; i < cache_.size(); i++) {
            ct += cache_.get(i).Capacity();
        }

        return ct;
    }

    public int Count() {
        int ct = 0;
        for(int i = 0; i < cache_.size(); i++) {
            ct += cache_.get(i).Count();
        }

        return ct;
    }

    /*
     ** Private methods.
     */
    private int Select(int zoom) {
        return zoom == 0 ? 0 : 1;
    }
}
