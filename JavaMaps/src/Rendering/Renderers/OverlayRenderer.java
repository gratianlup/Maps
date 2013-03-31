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
import java.awt.Graphics2D;
import java.awt.image.VolatileImage;
import java.util.HashMap;
import java.util.Iterator;

public class OverlayRenderer implements IRenderer, IOverlayHost {
    private IRendererHost host_;
    HashMap<Object, IVisual> overlays_;

    public OverlayRenderer(IRendererHost host) {
        host_ = host;
        overlays_ = new HashMap<Object, IVisual>(8);
    }

    /*
     ** Public methods.
     */
    public boolean IsEditor() { 
        return false; 
    }
    
    public ILayer Layer() { 
        return null; 
    }

    public int ZIndex() { 
        return Integer.MAX_VALUE - 1; // Below the control layer.
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
        if(overlays_.isEmpty()) {
            return;
        }

        // Draw all registered objects.
        VolatileImage buffer = view.GetBuffer(this);
        Graphics2D g = buffer.createGraphics();
        Iterator<IVisual> overlayIt = overlays_.values().iterator();
        
        while(overlayIt.hasNext()) {
            IVisual overlay = overlayIt.next();
            overlay.Draw(g, view);
        }

        g.dispose();
    }

    public void AddOverlay(Object key, IVisual item) {
        overlays_.put(item, item);
    }

    public void RemoveOverlay(Object key) {
        overlays_.remove(key);
    }

    public boolean HasOverlay(Object key) {
        return overlays_.containsKey(key);
    }

    public IVisual GetOverlay(Object key) {
        return overlays_.get(key);
    }
}
