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
import Rendering.Utils.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.image.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import javax.swing.SwingUtilities;

public class ImageRenderer implements IRenderer {
    /*
     ** Members.
     */
    private IMapProvider provider_;
    private IRendererHost host_;
    private ILayer layer_;
    private int zoomLevels_;
    private double tileSize_;
    private ArrayList<TileInfo> tiles_;
    private ArrayList<TileInfo> nextTiles_;
    private ImagePrefetcher prefetcher_;
    private ImageCache cache_;
    private double deltaLeft_;
    private double deltaRight_;
    private double deltaTop_;
    private double deltaBottom_;
    private double opacity_;
    private double prevLeft_;
    private double prevTop_;
    private double prevRight_;
    private double prevBottom_;
    private double prevZoom_;

    /*
     ** Constructors.
     */
    public ImageRenderer(ILayer layer, IRendererHost host) {
        host_ = host;
        layer_ = layer;
        provider_ = host.MapProvider();
        zoomLevels_ = provider_.ZoomLevels();

        // Create the list with tiles for the maximum zoom level
        // (all other zoom levels definitely contain fewer tiles).
        tileSize_ = provider_.TileSize();
        tiles_ = new ArrayList<TileInfo>(provider_.TileCount(zoomLevels_ - 1));
        nextTiles_ = new ArrayList<TileInfo>(provider_.TileCount(zoomLevels_ - 1));

        // Initialize the caching system.
        cache_ = new ImageCache(provider_);
        prefetcher_ = new ImagePrefetcher(this, provider_, cache_);
        prevZoom_ = -1.0;
    }

    /*
     ** Public methods.
     */
    public boolean IsEditor() { 
        return false; 
    }
    
    public ILayer Layer() { 
        return layer_; 
    }

    public int ZIndex() { 
        return 0; // The first drawn layer.
    }
    
    public void SetZIndex(int value) {
        throw new UnsupportedOperationException("ZIndex of image layer cannot be changed.");
    }
    
    public boolean HasPrefetcher() {
        return false;
    }
    public IPrefetcher Prefetcher() { 
        return null; 
    }

    public boolean MouseDown(Point point, View view, Modifier modifier) { 
        return false; 
    }
    
    public boolean MouseUp(Point point, View view, Modifier modifier) { 
        return false; 
    }
    
    public boolean MouseMoved(Point point, View view, Modifier modifier) { 
        return false; 
    }
    
    public boolean MouseDragged(Point point, View view, Modifier modifier) { 
        return false; 
    }
    
    public boolean MouseCaptured() { 
        return false; 
    } 
    
    public IObjectInfo HitTest(Point point, View view) {
        return null; 
    }

    public double Opacity() { 
        return 1.0; 
    }
    
    public void SetOpacity(double value) {}
    
    public boolean Visible() { 
        return true; 
    }
    
    public void SetVisible(boolean value) {}

    public void Render(View view) {
        // Compute the delta relative to the previous position.
        // The images are requested from the provider only if 
        // the delta is larger than the size of an image or if
        // the zoom level has been changed.
        double dLeft = view.Bounds().Left() - prevLeft_;
        double dTop = view.Bounds().Top() - prevTop_;
        double dRight = view.Bounds().Right() - prevRight_;
        double dBottom = view.Bounds().Bottom() - prevBottom_;

        QueryTiles(view, dLeft, dTop, dRight, dBottom);
        Draw(view);
        SetPreviousParams(view);
    }

    public void TileLoaded(Image image, TileInfo tile) {
        // A requested tile has been loaded, force a redraw
        // on the GUI thread to display it.
        Runnable notify = new Runnable() {
            public void run() { host_.Repaint(); }
        };
        
        SwingUtilities.invokeLater(notify);
    }

    /*
     ** Private methods.
     */
    private void SetPreviousParams(View view) {
        // Saves the current state of the view.
        // Used to comput the delta on the next draw request.
        Region2D bounds = view.Bounds();
        prevLeft_ = bounds.Left();
        prevTop_ = bounds.Top();
        prevRight_ = bounds.Right();
        prevBottom_ = bounds.Bottom();
        prevZoom_ = view.Zoom();
    }

    private Image GetTile(TileInfo tile, boolean forceLoad) {
        // First check if the image is found in the cache.
        // If not it is requested from the provider and the map
        // will be redrawn when the image has been loaded.
        Image tileImage = cache_.Get(tile.Id(), tile.ZoomLevel());

        if((tileImage == null) && forceLoad) {
            prefetcher_.LoadTile(tile);
        }

        return tileImage;
    }

    // Obtine lista cu imagini ce trebuie desenate pentru pozitia curenta.
    private void QueryTiles(View view, double dLeft, double dTop,
                                       double dRight, double dBottom) {
        // Get the list with the images required for the current
        // view from the image provider (only if really necessary).
        deltaLeft_ += dLeft;
        deltaRight_ -= dRight;
        deltaTop_ += dTop;
        deltaBottom_ -= dBottom;

        if((deltaLeft_ < 0) || (deltaRight_ < 0) ||
           (deltaTop_ < 0) || (deltaBottom_ < 0) ||
           ((int)view.Zoom() != (int)prevZoom_)) {
            // Cere imaginile din regiune.
            tiles_.clear();
            provider_.GetTiles(view.PreviousBounds(), (int)view.Zoom(), tiles_);

            // To check the distance to the edges it is sufficient
            // to check the first (top-left) and last (bottop-right) image.
            TileInfo first = tiles_.get(0);
            TileInfo last = tiles_.get(tiles_.size() - 1);
            Region2D bounds = view.Bounds();

            deltaLeft_ = bounds.Left() - first.X();
            deltaRight_ = (last.X() + tileSize_) - bounds.Right();
            deltaTop_ = bounds.Top() - first.Y();
            deltaBottom_ = (last.Y() + tileSize_) - bounds.Bottom();
            int nextZoom = (int)Math.ceil(view.Zoom());
            
            if((int)view.Zoom() != nextZoom) {
                // Get the images for the next zoom level.
                nextTiles_.clear();
                provider_.GetTiles(view.NextBounds(), nextZoom, nextTiles_);
            }
        }
    }

    // Afiseaza imaginile din lista specificata, scalate corespunzator.
    private void DrawImpl(ArrayList<TileInfo> tiles, Graphics g,
                          View view, double scale) {
        // Draw the images from the list correctly scalled.
        double viewX = view.Bounds().Left();
        double viewY = view.Bounds().Top();
        int count = tiles.size();
        
        for(int i = 0; i < count; i++) {
            TileInfo tile = tiles.get(i);
            double destLeft = (tile.X() * scale) - viewX;
            double destTop = (tile.Y() * scale) - viewY;
            double destRight = destLeft + (tileSize_ * scale);
            double destBottom = destTop + (tileSize_ * scale);

            // Get the image from the cache. If it is not found a request
            // for it is made and the map is redrawn when available.
            Image tileImage = GetTile(tile, true);
            
            if(tileImage == null) {
                // It is better than having a blank square in the map
                // to display an image with a lower resolution, but properly
                // scaled for the current zoom level.
                // la o rezolutie mai mica care va fi ulterior scalata.
                double size = tileSize_;
                double x = tile.X();
                double y = tile.Y();
                int prevZoom = tile.ZoomLevel();

                while(prevZoom > 0) {
                    // Adjust the tile locatin to the previous level.
                    x /= 2;
                    y /= 2;
                    size /= 2;
                    prevZoom--;

                    // Check if the corresponding image is cached
                    // (if not it is NOT requested from the provider).
                    tile = provider_.GetTile(new Core.Point(x, y), prevZoom);
                    tileImage = GetTile(tile, false);
                    
                    if(tileImage != null) {
                        // Scale the image to the required isze.
                        x = x - tile.X();
                        y = y - tile.Y();
                        g.drawImage(tileImage, (int)destLeft,
                                               (int)destTop,
                                               (int)destRight,
                                               (int)destBottom,
                                               (int)x, (int)y,
                                               (int)(x + size), (int)(y + size),
                                               (Component)host_);

                        break;
                    }
                }
            }
            else {
                // The image has already been loaded, just draw it.
                g.drawImage(tileImage,
                            (int)destLeft, (int)destTop,
                            (int)destRight,(int)destBottom,
                            0, 0, (int)tileSize_, (int)tileSize_,
                            (Component)host_);
            }
        }
    }

    private void Draw(View view) {
        // Draw all images in the current view.
        VolatileImage buffer = view.GetBuffer(this);
        Graphics2D g = buffer.createGraphics();
        g.clearRect(0, 0, (int)view.ViewBounds().Width(), (int)view.ViewBounds().Height());

        // When the map is zoomed in/out during an animation
        // an interpolation between the images on the current level
        // ant the next/previous one is done.
        double position = view.Zoom() - Math.floor(view.Zoom());
        
        if(position >= 0.3) {
            DrawImpl(nextTiles_, g, view, 0.5 + (position / 2));
        }

        if(position >= 0.3) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                           (float)(1.0 - position)));
        }
        else {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        DrawImpl(tiles_, g, view, 1.0 + position);
        g.dispose();
    }
}
