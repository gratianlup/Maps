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
import Rendering.IVisual;
import Rendering.*;
import Rendering.Utils.*;
import Core.*;
import Rendering.Animations.*;
import Rendering.Info.MarkerInfo;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.*;
import java.awt.image.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.FontMetrics;
import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MarkerRenderer implements IRenderer, IAnimatable {
    public class MarkerVisual implements IVisual {
        private Marker marker_;
        private Point position_;

        public MarkerVisual(Marker marker, Point point) {
            marker_ = marker;
            position_ = point;
        }

        public double X() { 
            return position_.X(); 
        }
        
        public double Y() { 
            return position_.Y(); 
        }
        
        public Marker Marker() { 
            return marker_; 
        }
        
        public Point Position() { 
            return position_; 
        }
        
        public void SetPosition(Point value) { 
            position_ = value; 
        }

        public void Draw(Graphics2D g, View view) {
            // The markers are drawn by the main class.
        }

        @Override
        public int hashCode() {
            return marker_.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            MarkerVisual other = (MarkerVisual) obj;
            return marker_.ID().equals(other.Marker().ID());
        }
    }

    // Used to draw the name of the marker above all other layer
    // in the moment the mouse cursor is above the marker.
    // (peste toate celelalte layer-e).
    class MarkerOverlay implements IVisual {
        private MarkerVisual markerVis_;
        private NameAnimation animation_;

        public MarkerOverlay(MarkerVisual visual, NameAnimation animation) {
            markerVis_ = visual;
            animation_ = animation;
        }

        public void Draw(Graphics2D g, View view) {
            double viewX = view.Bounds().Left();
            double viewY = view.Bounds().Top();
            double sx = GetScaleX(view.Zoom());
            double sy = GetScaleY(view.Zoom());
            int x = (int)((sx * markerVis_.X()) - (iconWidth_ / 2) - viewX);
            int y = (int)((sy * markerVis_.Y()) - (iconHeight_ / 2) - viewY);

            String name = markerVis_.Marker().Name();
            
            if(name == null) {
                // Nothing to draw actually.
                return;
            }

            FontMetrics metrics = g.getFontMetrics(nameFont_);
            int nameH = metrics.getHeight();
            int nameW = metrics.stringWidth(name);

            // Draw a rectangle around the text.
            int alpha = (int)(255 * animation_.Opacity());
            g.setColor(new Color(189, 223, 147, alpha));
            g.fillRoundRect(x + iconWidth_ + 4, y,
                            nameW + 9, nameH + 2, 10, 10);

            g.setColor(new Color(90, 90, 90, alpha));
            g.drawRoundRect(x + iconWidth_ + 4, y,
                            nameW + 9, nameH + 2, 10, 10);

            // Draw the text inside the rectangle.
            g.setColor(new Color(0, 0, 0, alpha));
            g.setFont(nameFont_);
            g.drawString(name, x + iconWidth_ + 8,
                               y + (iconHeight_ / 2) + (nameH / 4));
        }

        public MarkerVisual MarkerVisual() { 
            return markerVis_; 
        }
        
        public NameAnimation Animation() { 
            return animation_; 
        }
        
        public Point Position() { 
            return Point.Zero; 
        }

        @Override
        public int hashCode() {
            return markerVis_.marker_.ID().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) return false;
            if(getClass() != obj.getClass()) return false;
            
            final MarkerOverlay other = (MarkerOverlay) obj;
            if(markerVis_ != other.markerVis_ &&
              (markerVis_ == null || !markerVis_.equals(other.markerVis_))) {
                return false;
            }
            return true;
        }
    }

    // Animtaion used to show/hide the marker name.
    enum AnimationType {
        Show,
        Hide
    }

    class NameAnimation extends AnimationBase {
        private MarkerVisual marker_;
        private AnimationType type_;
        private double start_;
        private double end_;
        private double value_;

        public NameAnimation(MarkerVisual marker, MarkerRenderer parent,
                             AnimationType type, double start, double end,
                             long duration, IInterpolation interpolation) {
            super(parent, duration, interpolation);
            marker_ = marker;
            type_ = type;
            start_ = start;
            end_ = end;
        }

        public AnimationType Type() { 
            return type_; 
        }
        
        public MarkerVisual Marker() { 
            return marker_; 
        }
        
        public double Opacity() { 
            return value_; 
        }

        @Override
        public void Update() {
            value_ = interpolation_.GetValue(start_, end_, Progress());
        }

        @Override
        public void Start() { StartWatch(); }
    }

    /*
     ** Constants.
     */
    private static long ANIMATION_DURATION = 200;
    private static int NAME_SIZE = 14;

    /*
     ** Members.
     */
    private IMapProvider provider_;
    private IRendererHost host_;
    private ILayer layer_;
    private IProjection projection_;
    private int zoomLevels_;
    private Region2D maxBounds_;
    private double opacity_;
    private PointTree<MarkerVisual> markers_;
    private Image icon_;
    private boolean visible_;
    private BasicCollection<MarkerVisual> visibleMarkers_;
    private int iconWidth_;
    private int iconHeight_;
    private double[] scaleX;
    private double[] scaleY;
    private AlphaComposite opacityComp_;
    private Font nameFont_;
    private MarkerVisual prevMarker;

    /*
     ** Constructors.
     */
    public MarkerRenderer(ILayer layer, IRendererHost host) {
        host_ = host;
        layer_ = layer;
        provider_ = host.MapProvider();
        projection_ = provider_.Projection();
        zoomLevels_ = provider_.ZoomLevels();
        visible_ = true;
        maxBounds_ = provider_.MapBounds(zoomLevels_ - 1);
        markers_ = new PointTree<MarkerVisual>(maxBounds_.Width(), maxBounds_.Height());
        
        // Try to load the icon used to represent the marker.
        try {
            icon_ = provider_.LoadMarkerIcon(layer.ID());
            
            if(icon_ != null) {
                iconWidth_ = icon_.getWidth((ImageObserver)host);
                iconHeight_ = icon_.getHeight((ImageObserver)host);
            }
            else {
                iconWidth_ = 0;
                iconHeight_ = 0;
            }
        } catch (IOException ex) {
            Logger.getLogger(MarkerRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

        SetOpacity(1.0);
        visibleMarkers_ = new BasicCollection<MarkerVisual>();

        // Load all markers form the associated layer.
        Iterator<Marker> markerIt = provider_.GetMarkerIterator(layer.ID());
        
        while(markerIt.hasNext()) {
            Marker marker = markerIt.next();
            Point point = projection_.FromCoordinates(marker.Coordinates(),
                                                      zoomLevels_ - 1);
            MarkerVisual markerVis = new MarkerVisual(marker, point);
            markers_.Add(markerVis);
        }

        ComputeScaleFactor();
        nameFont_ = new Font("Dialog", Font.BOLD, NAME_SIZE);
    }

    public boolean IsEditor() { 
        return false; 
    }
    
    public ILayer Layer() { 
        return layer_; 
    }
    
    public int ZIndex() { 
        return 2; 
    }
    
    public void SetZIndex(int value) {}
    
    public boolean HasPrefetcher() { 
        return false; 
    }
    
    public IPrefetcher Prefetcher() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean MouseDown(Point point, View view, Modifier modifier) {
        // Check if a marker is found under the specified point.
        IObjectInfo info = HitTest(point, view);
        
        if(info != null) {
            host_.SendAction(Action.ObjectSelected(info));
            return true;
        }

        return false;
    }

    public boolean MouseUp(Point point, View view, Modifier modifier) {
        return MouseDown(point, view, modifier);
    }
    
    public boolean MouseDragged(Point point, View view, Modifier modifier) { 
        return false;  // No dragging supported.
    }

    public boolean MouseMoved(Point point, View view, Modifier modifier) {
        MarkerVisual result = HitTestImpl(point, view);
        
        if(result != null) {
            if(!result.equals(prevMarker)) {
                // A new marker has been selected, deselect the previous one.
                if(prevMarker != null) {
                    HideMarkerName(result);
                }

                ShowMarkerName(result);
                prevMarker = result;
            }
        }
        else {
            if(prevMarker != null) {
                // The cursor is not above any marker anymore.
                HideMarkerName(prevMarker);
                prevMarker = null;
            }
        }

        return false;
    }

    public boolean MouseCaptured() { 
        return false; // Mouse cannot be captured.
    }

    public double Opacity() { 
        return opacity_; 
    }
    
    public void SetOpacity(double value) {
        opacity_ = value;
        opacityComp_ = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                 (float)opacity_);
    }

    public boolean Visible() { 
        return visible_; 
    }
    
    public void SetVisible(boolean value) { 
        visible_ = value; 
    }
    
    public IObjectInfo HitTest(Point point, View view) {
        // Search for the nearest marker to the specified point.
        MarkerVisual markerVis = HitTestImpl(point, view);
        if(markerVis != null) {
            Node nearestNode = markerVis.Marker().NearestNode() != null ?
                               provider_.GetNode(markerVis.Marker().NearestNode()) :
                               null;
            Point temp = new Point(markerVis.X(), markerVis.Y());
            return new MarkerInfo(markerVis.Marker(), layer_, this, temp,
                                  projection_.ToCoordinates(temp, (int)view.Zoom()),
                                  nearestNode);
        }

        return null;
    }

    public void AddMarker(Marker marker) {
        Point point = projection_.FromCoordinates(marker.Coordinates(),
                                                      zoomLevels_ - 1);
        MarkerVisual markerVis = new MarkerVisual(marker, point);
        markers_.Add(markerVis);
        visibleMarkers_.Add(markerVis);
    }

    public void RemoveMarker(MarkerVisual markerVis) {
        markers_.Remove(markerVis);
    }

    public void MoveMarker(MarkerVisual markerVis, Point newPosition) {
        markers_.Remove(markerVis);
        markerVis.SetPosition(newPosition);
        markers_.Add(markerVis);
    }

    public MarkerVisual GetMarkerVisual(Marker marker) {
        // Search is done only in the list with visible markers.
        for(MarkerVisual markerVis : visibleMarkers_.Objects()) {
            if(markerVis.Marker().equals(marker)) {
                return markerVis;
            }
        }

        throw new RuntimeException("Marker visual not found.");
    }

    public void Render(View view) {
        VolatileImage buffer = view.GetBuffer(this);
        Graphics2D g = buffer.createGraphics();
        g.setComposite(opacityComp_);
        
        // Activate antialiasing.
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

        // Get the markers visible in the current view.
        Region2D bounds = view.LastBounds();
        double inflateVal = 2 * Math.max(iconWidth_, iconHeight_);
        
        visibleMarkers_.Clear();
        bounds.Inflate(inflateVal, inflateVal);
        markers_.Intersect(bounds, visibleMarkers_);
        bounds.Inflate(-inflateVal, -inflateVal);

        // Draw only the markes in the current view.
        Draw(g, view);
        g.dispose();
    }

    public void AnimationCompleted(AnimationBase animation) {
        NameAnimation nameAnim = (NameAnimation)animation;
        
        if(nameAnim.Type() == AnimationType.Hide) {
            // The text should not be displayed anymore.
            host_.Overlay().RemoveOverlay(nameAnim.Marker());
        }
    }

    /*
     ** Private methods.
     */
    private void ComputeScaleFactor() {
        // Compute the scale factores based on the map size
        // for each of the possible zoom levels.
        scaleX = new double[zoomLevels_];
        scaleY = new double[zoomLevels_];

        double sx = 1.0;
        double sy = 1.0;
        scaleX[zoomLevels_ - 1] = sx;
        scaleY[zoomLevels_ - 1] = sy;

        for(int i = zoomLevels_ - 2; i >= 0; i--) {
            Region2D cntBounds = provider_.MapBounds(i);
            Region2D prevBounds = provider_.MapBounds(i + 1);
            sx *= cntBounds.Width() / prevBounds.Width();
            sy *= cntBounds.Height() / prevBounds.Height();
            scaleX[i] = sx;
            scaleY[i] = sy;
        }
    }

    private double GetScaleX(double zoom) {
        double scale = zoom - Math.floor(zoom);
        return scaleX[(int)zoom] +
               (scaleX[(int)Math.ceil(zoom)] - scaleX[(int)zoom]) * scale;
    }

    private double GetScaleY(double zoom) {
        double scale = zoom - Math.floor(zoom);
        return scaleY[(int)zoom] +
               (scaleY[(int)Math.ceil(zoom)] - scaleY[(int)zoom]) * scale;
    }

    private MarkerVisual HitTestImpl(Point point, View view) {
        double sx = 1.0 / GetScaleX(view.Zoom());
        double sy = 1.0 / GetScaleY(view.Zoom());
        Point test = new Point(sx * (point.X() + view.Bounds().Left()),
                               sy * (point.Y() + view.Bounds().Top()));

        PointTree<MarkerVisual>.NearestInfo nearest = markers_.NearestPoint(test);
        
        if(nearest == null) {
            // This happens if there are no markes in the entire layer.
            return null;
        }

        if(nearest.Distance() < Math.min(sx * iconWidth_, sy * iconHeight_)) {
            return nearest.Value();
        }

        return null;
    }

    private void ShowMarkerName(MarkerVisual markerVis) {
        IOverlayHost host = host_.Overlay();
        double start = 0.0;
        double end = 1.0;

        if(host.HasOverlay(markerVis)) {
            // If an animation is already running stop it
            // and restart it with an adjusted duration.
            MarkerOverlay prevOverlay = (MarkerOverlay)host.GetOverlay(markerVis);
            start = prevOverlay.Animation().Opacity();
            host_.RemoveAnimation(prevOverlay.Animation());
        }

        NameAnimation animation = new NameAnimation(markerVis, this, AnimationType.Show,
                                                    start, end, ANIMATION_DURATION,
                                                    new LinearInterpolation());

        MarkerOverlay overlay = new MarkerOverlay(markerVis, animation);
        host_.Overlay().AddOverlay(markerVis.Marker(), overlay);
        host_.AddAnimation(animation);
    }

    private void HideMarkerName(MarkerVisual markerVis) {
        IOverlayHost host = host_.Overlay();
        double start = 1.0;
        double end = 0.0;

        if(host.HasOverlay(markerVis)) {
            // If an animation is already running stop it
            // and restart it with an adjusted duration.
            MarkerOverlay prevOverlay = (MarkerOverlay)host.GetOverlay(markerVis);
            start = prevOverlay.Animation().Opacity();
            host_.RemoveAnimation(prevOverlay.Animation());
        }

        NameAnimation animation = new NameAnimation(markerVis, this, AnimationType.Hide,
                                                    start, end, ANIMATION_DURATION,
                                                    new LinearInterpolation());

        MarkerOverlay overlay = new MarkerOverlay(markerVis, animation);
        host_.Overlay().AddOverlay(markerVis.Marker(), overlay);
        host_.AddAnimation(animation);
    }

    private void Draw(Graphics2D g, View view) {
        double viewX = view.Bounds().Left();
        double viewY = view.Bounds().Top();
        double sx = GetScaleX(view.Zoom());
        double sy = GetScaleY(view.Zoom());
        Iterator<MarkerVisual> markerIt = visibleMarkers_.Objects().iterator();
        
        while(markerIt.hasNext()) {
            MarkerVisual markerVis = markerIt.next();
            int x = (int)((sx * markerVis.X()) - (iconWidth_ / 2) - viewX);
            int y = (int)((sy * markerVis.Y()) - (iconHeight_ / 2) - viewY);
            g.drawImage(icon_, x, y, (ImageObserver)host_);
        }
    }
}
