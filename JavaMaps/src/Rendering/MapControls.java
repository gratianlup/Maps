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
import Core.Region2D;
import Core.IMapProvider;
import Core.ILayer;
import Core.Point;
import java.awt.Color;
import java.awt.image.VolatileImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;

public final class MapControls implements IRenderer {
    private static final double MIN_OPACITY = 0.25;
    private static final double EXPONENT_VALUE = 16.0;
    private static final double MIN_EXPONENT = 1.0;
    private static final double MAX_EXPONENT = Math.exp(EXPONENT_VALUE);

    private static final double SLIDER_WIDTH = 24;
    private static final double SLIDER_HEIGHT = 128;
    private static final double SLIDER_MARGIN_RIGHT = 44;
    private static final double SLIDER_MARGIN_TOP = 118;

    private static final Color SLIDER_BACK_COLOR = new Color(0, 0, 0, 220);
    private static final Color SLIDER_BUTTON_COLOR = new Color(64, 64, 64, 192);
    private static final Color SLIDER_SELECT_COLOR = new Color(61, 163, 220);
    private static final Color SLIDER_FONT_COLOR = new Color(255, 255, 255, 255);

    private static final double WHEEL_SIZE = 80;
    private static final double WHEEL_MARGIN_RIGHT = 16;
    private static final double WHEEL_MARGIN_TOP = 16;

    private static final Color WHEEL_BACK_COLOR = SLIDER_BACK_COLOR;
    private static final Color WHEEL_POINT_COLOR = SLIDER_BUTTON_COLOR;
    private static final Color WHEEL_ARROW_COLOR = SLIDER_BUTTON_COLOR;
    private static final Color WHEEL_SELECT_COLOR = SLIDER_SELECT_COLOR;

    /*
    ** Members.
    */
    private IRendererHost host_;
    private IMapProvider provider_;
    private ZoomSlider slider_;
    private NavigationWheel wheel_;
    private double opacity_;

    /*
    ** Constructors.
    */
    public MapControls(IRendererHost host) {
        host_ = host;
        provider_ = host.MapProvider();
        opacity_ = MIN_OPACITY;

        // Create the controls.
        slider_ = new ZoomSlider(host, 0, (double)provider_.ZoomLevels() - 1,
                                 SLIDER_WIDTH, SLIDER_HEIGHT);
        slider_.SetBackColor(SLIDER_BACK_COLOR);
        slider_.SetButtonColor(SLIDER_BUTTON_COLOR);
        slider_.SetFontColor(SLIDER_FONT_COLOR);
        slider_.SetSelectColor(SLIDER_SELECT_COLOR);

        wheel_ = new NavigationWheel(host);
        wheel_.SetWidth(WHEEL_SIZE);
        wheel_.SetHeight(WHEEL_SIZE);
        wheel_.SetBackColor(WHEEL_BACK_COLOR);
        wheel_.SetPointColor(WHEEL_POINT_COLOR);
        wheel_.SetArrowColor(WHEEL_ARROW_COLOR);
        wheel_.SetSelectColor(WHEEL_SELECT_COLOR);
        wheel_.SetOpacity(opacity_);
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
        return Integer.MAX_VALUE; 
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

    // All messages are redirected to the controlols.
    public boolean MouseDown(Point point, View view, Modifier modifier) {
        if(slider_.MouseDown(point)) {
            return true;
        }
        
        return wheel_.MouseDown(point);
    }

    public boolean MouseUp(Point point, View view, Modifier modifier) {
        if(slider_.MouseUp(point)) {
            return true;
        }
        
        return wheel_.MouseUp(point);
    }

    public boolean MouseDragged(Point point, View view, Modifier modifier) {
        if(slider_.MouseDragged(point)) {
            return true;
        }
        
        return wheel_.MouseDragged(point);
    }

    public boolean MouseCaptured() {
        return slider_.MouseCaptured() ||
               wheel_.MouseCaptured();
    }

    public boolean MouseMoved(Point point, View view, Modifier modifier) {
        // Compute the opacity of the controls. They have maximum opacity
        // only when the cursor is close to them. To do this an exponential
        // function scaled on the corresponding value domain is used
        // for both the X and Y axis.
        Region2D bounds = view.ViewBounds();
        double marginRight = bounds.Width() - WHEEL_SIZE - WHEEL_MARGIN_RIGHT;
        double marginTop = bounds.Height() - SLIDER_HEIGHT - SLIDER_MARGIN_TOP;

        double expX = EXPONENT_VALUE * (point.X() / marginRight);
        double expValueX = Math.min(MAX_EXPONENT, Math.exp(expX));

        double expY = EXPONENT_VALUE * ((bounds.Height() - point.Y()) / marginTop);
        double expValueY = Math.min(MAX_EXPONENT, Math.exp(expY));

        double opacityX = expValueX / (MAX_EXPONENT - MIN_EXPONENT);
        double opacityY = expValueY / (MAX_EXPONENT - MIN_EXPONENT);
        opacity_ = Math.min(1.0, MIN_OPACITY + (opacityX * opacityY));
        
        wheel_.SetOpacity(opacity_);
        host_.Repaint();
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
        return false; 
    }
    
    public void SetVisible(boolean value) {}
    
    public void Render(View view) {}

    public void Render(Graphics2D g, View view) {
        // Activate anti-aliasing.
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                  (float)opacity_));

        // Draw each control.
        slider_.SetCurrentZoom(view.Zoom());
        slider_.Draw(g);
        wheel_.Draw(g);
        g.dispose();
    }

    public void ViewerResized(View view) {
        Region2D bounds = view.ViewBounds();

        // Update the control position.
        slider_.SetLeft(bounds.Width() - SLIDER_MARGIN_RIGHT - SLIDER_WIDTH);
        slider_.SetTop(SLIDER_MARGIN_TOP);
        wheel_.SetLeft(bounds.Width() - WHEEL_MARGIN_RIGHT - WHEEL_SIZE);
        wheel_.SetTop(WHEEL_MARGIN_TOP);
    }
}
