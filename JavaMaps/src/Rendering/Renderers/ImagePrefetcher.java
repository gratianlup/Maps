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
import Rendering.*;
import java.awt.Image;
import java.util.ArrayList;
import java.util.PriorityQueue;

public final class ImagePrefetcher implements IPrefetcher {
    enum RequestPriority {
        Normal,
        High
    }

    // Describes a request from the ImageRenderer (high priority)
    // or from the MapViewer (low priority).
    final class Request implements Comparable<Object> {
        private TileInfo tile_;
        private RequestPriority priority_;
        private boolean notifyParent_;

        Request(TileInfo tile) {
            tile_ = tile;
            priority_ = RequestPriority.Normal;
        }

        Request(TileInfo tile, RequestPriority priority, boolean notify) {
            tile_ = tile;
            priority_ = priority;
            notifyParent_ = notify;
        }

        public TileInfo Tile() { 
            return tile_; 
        }
        
        public RequestPriority Priority() { 
            return priority_; 
        }
        
        public boolean NotifyParent() { 
            return notifyParent_; 
        }

        public int compareTo(Object o) {
            Request other = (Request)o;
            if(priority_ == other.priority_) return 0;
            else if(priority_ == RequestPriority.High) return 1;
            else return -1;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) return false;
            if(this == obj) return true;
            if(this.getClass() != obj.getClass()) return false;

            return tile_.Id().equals(((Request)obj).tile_.Id());
        }

        @Override
        public int hashCode() {
            return tile_.hashCode();
        }
    }

    // Event to synchronize the threads when new images should be loaded.
    final class Event {
        private final Object lock_;
        private boolean set_;

        public Event() {
            lock_ = new Object();
            set_ = false;
        }

        public void Set() {
            synchronized(lock_) {
                set_ = true;
                lock_.notifyAll();
            }
        }

        public void Wait() throws InterruptedException {
            synchronized(lock_) {
                while(!set_) {
                    lock_.wait();
                }

                set_ = false;
            }
        }
    }

    // Loads the images on a different thread than the GUI.
    final class Loader implements Runnable {
        public void run() {
            Request request;
            Image image;

            while(true) {
                try {
                    // Wait until a load request arrives.
                    available_.Wait();

                    // Extrage cererea din coada.
                    synchronized(queuelock_) {
                        request = requests_.poll();
                        
                        if(requests_.size() > 0) {
                            // There are more images to load.
                            available_.Set();
                        }
                    }

                    // Load the image and insert it into the cache.
                    TileInfo tile = request.Tile();
                    
                    if(cache_.Contains(tile.Id(), tile.ZoomLevel()) == false) {
                        image = provider_.LoadTile(tile.Id());
                        cache_.Add(image, tile.Id(), tile.ZoomLevel());
                    }
                    else {
                        image = cache_.Get(tile.Id(), tile.ZoomLevel());
                    }
                    
                    //Thread.sleep(800); // Can be used to simulate network delays...

                    if(request.NotifyParent()) {
                        // Notify the parent that the image has been loaded.
                        parent_.TileLoaded(image, tile);
                    }
                }
                catch(InterruptedException e) {
                    return; // Execution completed.
                }
            }
        }
    }

    /*
     ** Members.
     */
    private boolean enabled_;
    private ImageRenderer parent_;
    private IMapProvider provider_;
    private ImageCache cache_;
    private PriorityQueue<Request> requests_;
    private final Object queuelock_;
    private final Event available_;
    private Thread loaderThread_;
    private double tileSize_;
    private double prevLeft_;
    private double prevTop_;
    private ArrayList<TileInfo> tiles_;

    /*
     ** Constructors.
     */
    ImagePrefetcher(ImageRenderer parent, IMapProvider provider, ImageCache cache) {
        assert(parent != null);
        assert(provider != null);
        assert(cache != null);
        // ------------------------------------------------
        parent_ = parent;
        provider_ = provider;
        enabled_ = true;
        cache_ = cache;
        queuelock_ = new Object();
        available_ = new Event();
        requests_ = new PriorityQueue<Request>();
        tileSize_ = provider_.TileSize();
        tiles_ = new ArrayList<TileInfo>();

        // Start the thread that loads the images in the background.
        CreateThread();        
    }

    /*
     ** Public methods.
     */
    public IRenderer Parent() { 
        return parent_; 
    }

    public boolean Enabled() { 
        return enabled_; 
    }
    
    public void SetEnabled(boolean value) {
        enabled_ = value; 
    }

    public void ViewChanged(View view) {
        // Find the images that should be loaded depending
        // on the direction in which the map is moving.
        if(!enabled_ || ((view.Zoom() - Math.floor(view.Zoom())) > 0)) {
            return;
        }

        tiles_.clear();
        Region2D bounds = view.Bounds();
        Region2D maxBouds = view.MaxBounds();

        if((int)view.Zoom() < (provider_.ZoomLevels() - 1)) {
            int nextZoom = (int)view.Zoom() + 1;
            Region2D nextBounds = provider_.MapBounds(nextZoom);

            double scaleXNext = nextBounds.Width() / view.MaxBounds().Width();
            double scaleYNext = nextBounds.Height() / view.MaxBounds().Height();
            nextBounds.SetLeft(bounds.Left() * scaleXNext);
            nextBounds.SetTop(bounds.Top() * scaleYNext);
            nextBounds.SetWidth(bounds.Width() * scaleXNext);
            nextBounds.SetHeight(bounds.Height() * scaleYNext);
            
            // Pre-load the images for the next zoom level.
            provider_.GetTiles(nextBounds, nextZoom, tiles_);
        }
        
        double dx = prevLeft_ - bounds.Left();
        double dy = prevTop_ - bounds.Top();

        // Don't load yet if the delta is very small.
        if((Math.abs(dx) >= (tileSize_ / 4)) ||
           (Math.abs(dy) >= (tileSize_ / 4))) {
            double angle = Math.atan2(dy, dx);
            double widthExt  = -(3 * tileSize_) * Math.cos(angle);
            double heightExt = -(3 * tileSize_) * Math.sin(angle);
            widthExt = widthExt >= 0 ?
                            Math.min(widthExt, maxBouds.Right() - bounds.Right()) :
                            Math.max(widthExt, maxBouds.Left() - bounds.Left());
            heightExt = heightExt >= 0 ?
                            Math.min(heightExt, maxBouds.Bottom() - bounds.Bottom()) :
                            Math.max(heightExt, maxBouds.Top() - bounds.Top());

            // Request the images that are found in the new region.
            Region2D predicted = new Region2D(bounds);
            predicted.Offset(widthExt, heightExt);
            provider_.GetTiles(predicted, (int)view.Zoom(), tiles_);

            prevLeft_ = bounds.Left();
            prevTop_ = bounds.Top();
        }
        
        int count = tiles_.size();
        
        for(int i = 0; i < count; i++) {
            // If the image is already in cache skip it.
            TileInfo tile = tiles_.get(i);
            
            if(cache_.Contains(tile.Id(), tile.ZoomLevel())) {
                continue;
            }

            // Create the request object for the image loading thread.
            Request request = new Request(tiles_.get(i));
            
            synchronized(queuelock_) {
                if(!requests_.contains(request)) {
                    requests_.add(request);

                    synchronized(available_) {
                        available_.Set();
                    }
                }
            }
        }
    }

    public void LoadTile(TileInfo tile) {
        synchronized(queuelock_) {
            // Create the request object for the image loading thread.
            Request request = new Request(tile, RequestPriority.High, true);

            if(!requests_.contains(request)) {
                requests_.add(request);
                available_.Set();
            }
        }
    }

    /*
     ** Private methods.
     */
    private void CreateThread() {
        loaderThread_ = new Thread(new Loader());
        loaderThread_.start();
    }

    private void DestroyThread() {
        loaderThread_.interrupt();
        loaderThread_ = null;
    }
}
