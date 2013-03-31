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
import java.awt.*;
import java.awt.image.*;

public final class View {
    /*
     ** Members.
     */
    private VolatileImage buffer_;
    private double leftMargin_;
    private double topMargin_;
    private Region2D bounds_;
    private Region2D maxBounds_;
    private Region2D viewBounds_;
    private double zoom_;
    private Region2D previousBounds_; // Previous zoom level (1.4 -> 1; 1 -> 1).
    private Region2D nextBounds_;     // Next zoom level (1.4 -> 2; 2 -> 2).
    private Region2D lastBounds_;     // Last valid zoom level (1.4 -> N).

    public View() {
        bounds_ = new Region2D(0, 0, 0, 0);
        maxBounds_ = new Region2D(0, 0, 0, 0);
        previousBounds_ = new Region2D(0, 0, 0, 0);
        nextBounds_ = new Region2D(0, 0, 0, 0);
        lastBounds_ = new Region2D(0, 0, 0, 0);
    }

    /*
     ** Public methods.
     */
    public VolatileImage GetBuffer(IRenderer renderer) {
        return buffer_;
    }

    public void CreateBuffers(GraphicsConfiguration config) {
        if(viewBounds_ == null) {
            // Not completely initialized yet.
            return;
        }

        int width = (int)viewBounds_.Width();
        int height = (int)viewBounds_.Height();
        buffer_ = config.createCompatibleVolatileImage(width, height, Paint.OPAQUE);
    }

    public boolean ValidateBuffers(GraphicsConfiguration config) {
        if(buffer_ == null) {
            CreateBuffers(config);
            return false;
        }

        int status = buffer_.validate(config);
        if(status != VolatileImage.IMAGE_OK) {
            if(status == VolatileImage.IMAGE_INCOMPATIBLE) {
                // The buffers where invalidated and must be recreated.
                // This happens seldom, for example when the window
                // is moved to another display.
                CreateBuffers(config);
            }
            
            return false;
        }
        
        return true;
    }

    public boolean DrawingFailed() {
        return buffer_.contentsLost();
    }

    public void ComputeAdjacentBounds(Region2D[] mapBounds) {
        // Compute the current region, but at the previous zoom level.
        int prevZoom = (int)zoom_;
        Region2D prevBounds = mapBounds[prevZoom];
        Region2D cntBounds = maxBounds_;
        
        double scaleXPrev = prevBounds.Width() / cntBounds.Width();
        double scaleYPrev = prevBounds.Height() / cntBounds.Height() ;
        previousBounds_.SetLeft(bounds_.Left() * scaleXPrev);
        previousBounds_.SetTop((bounds_.Top() * scaleYPrev));
        previousBounds_.SetWidth(bounds_.Width() * scaleXPrev);
        previousBounds_.SetHeight(bounds_.Height() * scaleYPrev);

        // Compute the current region, but at the next previous zoom level.
        int nextZoom = (int)Math.ceil(zoom_);
        Region2D nextBounds = mapBounds[nextZoom];

        double scaleXNext = nextBounds.Width() / cntBounds.Width();
        double scaleYNext = nextBounds.Height() / cntBounds.Height();
        nextBounds_.SetLeft(bounds_.Left() * scaleXNext);
        nextBounds_.SetTop(bounds_.Top() * scaleYNext);
        nextBounds_.SetWidth(bounds_.Width() * scaleXNext);
        nextBounds_.SetHeight(bounds_.Height() * scaleYNext);

        // Compute the current region, but at the last valid zoom level.
        int lastZoom = mapBounds.length - 1;
        Region2D lastBounds = mapBounds[lastZoom];

        double scaleXLast = lastBounds.Width() / cntBounds.Width();
        double scaleYLast = lastBounds.Height() / cntBounds.Height();
        lastBounds_.SetLeft(bounds_.Left() * scaleXLast);
        lastBounds_.SetTop(bounds_.Top() * scaleYLast);
        lastBounds_.SetWidth(bounds_.Width() * scaleXLast);
        lastBounds_.SetHeight(bounds_.Height() * scaleYLast);
    }

    public void Render(Graphics2D g, Component host) {
        g.drawImage(buffer_,
                   (int)leftMargin_, (int)topMargin_, // destination
                   (int)(leftMargin_ + bounds_.Width()),
                   (int)(topMargin_ + bounds_.Height()),
                   0, 0, (int)bounds_.Width(), (int)bounds_.Height(), host);
    }

    public double LeftMargin() { 
        return leftMargin_; 
    }
    
    public void SetLeftMargin(double value) { 
        leftMargin_ = value; 
    }

    public double TopMargin() { 
        return topMargin_; 
    }
    
    public void SetTopMargin(double value) { 
        topMargin_ = value; 
    }

    public Region2D Bounds() { 
        return bounds_; 
    }
    
    public void SetBounds(Region2D value) { 
        bounds_ = value; 
    }

    public Region2D MaxBounds() { 
        return maxBounds_; 
    }
    
    public void SetMaxBounds(Region2D value) { 
        maxBounds_ = value; 
    }

    public Region2D ViewBounds() { 
        return viewBounds_; 
    }
    
    public void SetViewBounds(Region2D value) { 
        viewBounds_ = value; 
    }

    public double Zoom() { 
        return zoom_; 
    }
    
    public void SetZoom(double value) { 
        zoom_ = value; 
    }

    public Region2D PreviousBounds() { 
        return previousBounds_; 
    }
    
    public void SetPreviousBounds(Region2D value) { 
        previousBounds_ = value; 
    }

    public Region2D NextBounds() { 
        return nextBounds_; 
    }
    
    public void SetNextBounds(Region2D value) { 
        nextBounds_ = value; 
    }

    public Region2D LastBounds() { 
        return lastBounds_; 
    }
    
    public void SetLastBounds(Region2D value) { 
        lastBounds_ = value; 
    }
}
