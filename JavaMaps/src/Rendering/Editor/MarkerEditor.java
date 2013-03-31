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

package Rendering.Editor;
import Rendering.IVisual;
import Rendering.*;
import Rendering.Utils.*;
import Core.*;
import Rendering.Renderers.MarkerRenderer;
import Rendering.Info.LinkInfo;
import Rendering.Info.NodeInfo;
import Rendering.Info.*;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class MarkerEditor implements IRenderer {
    class MarkerVisual implements IVisual {
        private Marker marker_;
        private MarkerRenderer parent_;
        private Point position_;
        private Color color_;
        private NodeEditor.NodeVisual nearestNode_;

        /*
        ** Constructors.
        */
        public MarkerVisual(Marker marker, MarkerRenderer parent,
                            Point position, Color color) {
            marker_ = marker;
            parent_ = parent;
            position_ = position;
            color_ = color;
        }

        /*
        ** Public methods.
        */
        public Marker Marker() { 
            return marker_; 
        }
        
        public MarkerRenderer Parent() { 
            return parent_; 
        }
        
        public ILayer Layer() { 
            return parent_.Layer(); 
        }

        public Point Position() { 
            return position_; 
        }
        
        public void SetPosition(Point value) { 
            position_ = value; 
        }

        public Color Color() { 
            return color_; 
        }
        
        public void SetColor(Color value) { 
            color_ = value; 
        }

        public NodeEditor.NodeVisual NearestNode() { 
            return nearestNode_; 
        }
        
        public void SetNearestNode(NodeEditor.NodeVisual value) {
            nearestNode_ = value; 
        }

        public void Draw(Graphics2D g, View view) {
            double x = position_.X() - view.Bounds().Left();
            double y = position_.Y() - view.Bounds().Top();

            // Set the circle color based on its selection state.
            if(this == selectedMarker_) {
                g.setColor(SELECTED_MARKER_COLOR);
            }
            else {
                g.setColor(color_);
            }

            // Set the line width.
            if(parent_.Layer() == selectedLayer_) {
                g.setStroke(selectedMarkerStroke_);
            }
            else {
                g.setStroke(markerStroke_);
            }

            double centerX = x - (MARKER_SIZE / 2);
            double centerY = y - (MARKER_SIZE / 2);
            g.drawOval((int)centerX, (int)centerY, (int)MARKER_SIZE, (int)MARKER_SIZE);

            // Display the link to the closest node.
            if((this == selectedMarker_) &&
               (nearestNode_ != null)) {
                // Compute the point on the edge of the circle.
                double nodeX = nearestNode_.Position().X() - view.Bounds().Left();
                double nodeY = nearestNode_.Position().Y() - view.Bounds().Top();
                double dx = nodeX - x;
                double dy = nodeY - y;
                double angle = Math.atan2(dy, dx);
                
                double ax = x + ((MARKER_SIZE / 2) + 2) * Math.cos(angle);
                double ay = y + ((MARKER_SIZE / 2) + 2) * Math.sin(angle);

                // Draw the line and the node.
                g.drawLine((int)ax, (int)ay, (int)nodeX, (int)nodeY);
                g.fillOval((int)(nodeX - NODE_SIZE/2),
                           (int)(nodeY - NODE_SIZE/2),
                           (int)NODE_SIZE, (int)NODE_SIZE);
            }
        }
    }

    /*
    ** Constants.
    */
    private static double MARKER_SIZE = 40;
    private static Color[] MARKER_COLORS = new Color[] {
        new Color(0, 167, 255), // Blue
        new Color(166, 227, 4), // Green
        new Color(250, 148, 0), // Orange
    };
    private static Color SELECTED_MARKER_COLOR = new Color(255, 43, 104);
    private static float MARKER_WIDTH = 3;
    private static float NODE_SIZE = 20;
    private static float SELECTED_MARKER_WIDTH = 4;
    private static float UNSELECTED_OPACITY = 0.20f;
    private static double EDITOR_OPACITY = 0.50;

    /*
    ** Members.
    */
    private IMapProvider provider_;
    private IRendererHost host_;
    private IProjection projection_;
    private NodeEditor nodeEditor_;
    private Region2D maxBounds_;
    private boolean visible_;
    private int zoomLevels_;

    private List<MarkerRenderer> markerRenderers_;
    private PointTree<MarkerVisual> markers_;
    private HashMap<Marker, MarkerVisual> markerMap_;
    private BasicCollection<MarkerVisual> visibleMarkers_;
    private ArrayList<MarkerVisual> frontMarkers_;

    private MarkerVisual selectedMarker_;
    private MarkerRenderer.MarkerVisual selectedObject_;
    private ILayer selectedLayer_;
    private ILayer addLayer_;
    private boolean mouseCaptured_;
    private Point startPosition_;
    private Point dragStart_;

    private Stroke markerStroke_;
    private Stroke selectedMarkerStroke_;
    private HashMap<MarkerRenderer, Color> layerColors_;

    /*
    ** Constructors.
    */
    public MarkerEditor(List<MarkerRenderer> renderers, NodeEditor nodeEditor,
                        IRendererHost host) {
        host_ = host;
        provider_ = host.MapProvider();
        projection_ = provider_.Projection();
        zoomLevels_ = provider_.ZoomLevels();
        markerRenderers_ = renderers;
        nodeEditor_ = nodeEditor;
        visible_ = true;

        maxBounds_ = provider_.MapBounds(zoomLevels_ - 1);
        markers_ = new PointTree<MarkerVisual>(maxBounds_.Width(), maxBounds_.Height());
        visibleMarkers_ = new BasicCollection<MarkerVisual>();
        frontMarkers_ = new ArrayList<MarkerVisual>();
        markerMap_ = new HashMap<Marker, MarkerVisual>();

        markerStroke_ = new BasicStroke(MARKER_WIDTH);
        selectedMarkerStroke_ = new BasicStroke(SELECTED_MARKER_WIDTH);
        SetLayerColors(renderers);
        LoadMarkers();
    }

    /*
    ** Public methods.
    */
    public boolean IsEditor() { 
        return true; 
    }
    
    public ILayer Layer() { 
        return null; 
    }
    
    public boolean HasPrefetcher() { 
        return false; 
    }
    
    public IPrefetcher Prefetcher() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public int ZIndex() { 
        return Integer.MAX_VALUE - 2; // Below overlay.
    } 
    
    public void SetZIndex(int value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean MouseDown(Point point, View view, Modifier modifier) {
        if(visible_ == false) return false;

        // Search for the marker found at the specified position.
        Point adjPoint = AdjustPoint(point, view);
        MarkerVisual markerVis = HitTestImpl(adjPoint, view);
        
        if(markerVis != null) {
            // A marker has been selected. If the action has been performed
            // using a right click enter the mode for selecting the nearest node.
            if(modifier.IsSet(modifier.BUTTON_RIGHT)) {
                nodeEditor_.SetVisible(true);
                nodeEditor_.SetOpacity(EDITOR_OPACITY);
                nodeEditor_.SetLinksVisible(false);
            }
            else {
                // The marker has been moved; get the associate object
                // from the module that is drawing the markers.
                selectedObject_ = markerVis.Parent().GetMarkerVisual(markerVis.Marker());
            }

            selectedMarker_ = markerVis;
            selectedLayer_ = markerVis.Layer();
            dragStart_ = point;
            mouseCaptured_ = true;
            startPosition_ = selectedMarker_.Position();

            // Notify the parent and force a redraw.
            host_.SendAction(Action.ObjectSelected(HitTest(adjPoint, view)));
            host_.Repaint();
            return true;
        }
        else {
            // If the shift key is pressed add a new marker.
            if(modifier.IsSet(Modifier.KEY_SHIFT) && (addLayer_ != null)) {
                selectedMarker_ = AddMarker(adjPoint, LayerRenderer(addLayer_));
                
                if(selectedMarker_ != null) {
                    selectedLayer_ = addLayer_;
                    mouseCaptured_ = true;
                    dragStart_ = point;
                    mouseCaptured_ = true;
                    startPosition_ = selectedMarker_.Position();
                    selectedObject_ = selectedMarker_.Parent().GetMarkerVisual(selectedMarker_.Marker());
                }

                // Notify the parent and force a redraw.
                host_.SendAction(Action.ObjectSelected(HitTest(point, view)));
                host_.Repaint();
                return true;
            }
            else {
                selectedMarker_ = null;
                selectedLayer_ = null;
            }       
        }

        host_.Repaint();
        return false;    }

    public boolean MouseUp(Point point, View view, Modifier modifier) {
        // Restore the node editor layout.
        if(visible_ == false) {
            return false;
        }
        
        nodeEditor_.SetVisible(false);
        nodeEditor_.SetOpacity(1.0);
        nodeEditor_.SetLinksVisible(true);
        selectedObject_ = null;
        mouseCaptured_ = false;
        host_.Repaint();
        return false;
    }

    public boolean MouseMoved(Point point, View view, Modifier modifier) { 
        return false; 
    }
    
    public boolean MouseCaptured() { 
        return mouseCaptured_; 
    }

    public boolean MouseDragged(Point point, View view, Modifier modifier) {
        assert(mouseCaptured_);
        // ------------------------------------------------
        if(visible_ == false) {
            return false;
        }

        if(selectedObject_ != null) {
            // The selected marker must be moved.
            MoveMarker(selectedMarker_, selectedObject_, point, startPosition_);
        }
        else {
            // Select the nearest node.
            SelectNode(selectedMarker_, point, startPosition_);
        }

        host_.Repaint();
        return true;
    }

    public IObjectInfo HitTest(Point point, View view) {
        MarkerVisual markerVis = HitTestImpl(point, view);
        if(markerVis != null) {
            Node nearest = markerVis.NearestNode() != null ?
                               markerVis.NearestNode().Node() : null;
            return new MarkerInfo(markerVis.Marker(), markerVis.Layer(), this,
                                  markerVis.Position(), 
                                  ToCoordinates(markerVis.Position()), nearest);
        }

        return null;
    }

    public double Opacity() { 
        return 1.0; 
    }
    
    public void SetOpacity(double value) {}

    public boolean Visible() { 
        return visible_; 
    }
    
    public void SetVisible(boolean value) { 
        visible_ = value; 
    }

    public void Render(View view) {
        // Draw only if the view is at the maximum valid zoom leve.
        if((int)view.Zoom() != (zoomLevels_ - 1)) {
            return;
        }

        // Activate antialiasing.
        VolatileImage buffer = view.GetBuffer(this);
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

        // Find the points visible in the current view.
        Region2D bounds = view.Bounds();
        double inflateVal = 2 * MARKER_SIZE;

        visibleMarkers_.Clear();
        bounds.Inflate(inflateVal, inflateVal);
        markers_.Intersect(bounds, visibleMarkers_);
        bounds.Inflate(-inflateVal, -inflateVal);

        Draw(visibleMarkers_, g, view);
        g.dispose();
    }

    public void RemoveMarker(Marker marker, ILayer parentLayer) {
        assert(marker != null);
        assert(parentLayer != null);
        // ------------------------------------------------
        // Remove the marker from the point tree and
        // from the associated drawing module.
        MarkerVisual markerVis = markerMap_.get(marker);
        markers_.Remove(markerVis);
        markerVis.Parent().RemoveMarker(markerVis.Parent().GetMarkerVisual(markerVis.Marker()));
        host_.Repaint();
    }

    public void SetCurrentMarker(ILayer markerLayer) {
        assert(markerLayer != null);
        // ------------------------------------------------
        // Set the layer where the markers are added.
        addLayer_ = markerLayer;
    }

    /*
    ** Private methods.
    */
    private void LoadMarkers() {
        // Load each marker from all layers.
        for(MarkerRenderer renderer : markerRenderers_) {
            ILayer layer = renderer.Layer();
            addLayer_ = layer;
            Iterator<Marker> markerIt = provider_.GetMarkerIterator(layer.ID());
            
            while(markerIt.hasNext()) {
                Marker marker = markerIt.next();

                // Create the visual object that represents the marker.
                MarkerVisual markerVis = new MarkerVisual(marker, renderer,
                                            FromCoordinates(marker.Coordinates()),
                                            MarkerColor(renderer));
                if(marker.NearestNode() != null) {
                    markerVis.SetNearestNode(nodeEditor_.GetNodeVisual(marker.NearestNode()));
                }
                
                markers_.Add(markerVis);
                markerMap_.put(marker, markerVis);
            }
        }
    }

    private void Draw(BasicCollection<MarkerVisual> markers,
                      Graphics2D g, View view) {
        if(markers.Count() == 0) return;

        // The markers from the selected layer are drawn last
        // to make sure they are above all other markers.
        frontMarkers_.clear();

        if(selectedMarker_ != null) {
            // The markers on the unselected layers are drawn with lower opacity.
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                      UNSELECTED_OPACITY));
        }

        Iterator<MarkerVisual> markerIt = markers.Objects().iterator();
        
        while(markerIt.hasNext()) {
            MarkerVisual markerVis = markerIt.next();
            
            if(markerVis.Layer().equals(selectedLayer_)) {
                frontMarkers_.add(markerVis);
            }
            else {
                markerVis.Draw(g, view);
            }
        }

        if(selectedMarker_ != null) {
            // Use maximum opacity for the markers from the selected layer.
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            int count = frontMarkers_.size();
            
            for(int i = 0; i < count; i++) {
                frontMarkers_.get(i).Draw(g, view);
            }
        }
    }

    private MarkerVisual HitTestImpl(Point point, View view) {
        // Check if a marker can be found in the specified position.
        PointTree<MarkerVisual>.NearestInfo nearest = markers_.NearestPoint(point);
        
        if(nearest == null) {
            return null;
        }
        else if(nearest.Distance() < MARKER_SIZE) {
            return nearest.Value();
        }

        return null;
    }

    private MarkerVisual AddMarker(Point point, MarkerRenderer parent) {
        // Add a new marker to the "addLayer_" layer.
        Marker marker = new Marker(ObjectId.NewId(), ToCoordinates(point));
        MarkerInfo info = new MarkerInfo(marker, parent.Layer(), this,
                                         point, marker.Coordinates(), null);

        if(host_.SendAction(Action.ObjectAdded(info)).Valid()) {
            MarkerVisual markerVis = new MarkerVisual(marker, parent, 
                                                      point, MarkerColor(parent));
            // Find the nearest node and associate it with the marker.
            NodeInfo nodeInfo = (NodeInfo)nodeEditor_.NearestNode(point);
            Node nearest = nodeInfo != null ? nodeInfo.Node() : null;
            
            if(nearest != null) {
                markerVis.SetNearestNode(nodeEditor_.GetNodeVisual(nearest.Id()));
            }

            markers_.Add(markerVis);
            parent.AddMarker(marker);
            return markerVis;
        }

        return null;
    }

    private boolean MoveMarker(MarkerVisual markerVis,
                               MarkerRenderer.MarkerVisual markerObject,
                               Point mousePoint, Point start) {
        // Notify the parent that the marker is going to be moved.
        double dx = mousePoint.X() - dragStart_.X();
        double dy = mousePoint.Y() - dragStart_.Y();
        Point newPos = new Point(start.X() + dx, start.Y() + dy);
        
        // Find the nearest node and associate it with the marker.
        NodeInfo nodeInfo = (NodeInfo)nodeEditor_.NearestNode(newPos);
        Node nearest = nodeInfo != null ? nodeInfo.Node() : null;

        MarkerInfo info = new MarkerInfo(markerVis.Marker(), markerVis.Layer(),
                                         this, newPos, ToCoordinates(newPos), nearest);
        if(host_.SendAction(Action.ObjectMoved(info)).Valid()) {
            // The marker can be moved, do it.s
            markers_.Remove(markerVis);
            markerVis.SetPosition(newPos);
            markers_.Add(markerVis);
            markerVis.Parent().MoveMarker(markerObject, newPos);

            if(nearest != null) {
                markerVis.SetNearestNode(nodeEditor_.GetNodeVisual(nearest.Id()));
            }

            return true;
        }

        return false;
    }

    private void SelectNode(MarkerVisual markerVis, Point mousePoint, Point start) {
        // Associate the nearest node with the specified marker.
        double dx = mousePoint.X() - dragStart_.X();
        double dy = mousePoint.Y() - dragStart_.Y();
        Point newPos = new Point(start.X() + dx, start.Y() + dy);

        NodeInfo nodeInfo = (NodeInfo)nodeEditor_.NearestNode(newPos);
        Node nearest = nodeInfo != null ? nodeInfo.Node() : null;
        
        if(nearest != null) {
            markerVis.SetNearestNode(nodeEditor_.GetNodeVisual(nearest.Id()));
        }
    }

    private void SetLayerColors(List<MarkerRenderer> list) {
        // Set the colors that should be used for each marker type.
        int ct = 0;
        layerColors_ = new HashMap<MarkerRenderer, Color>();

        for(MarkerRenderer renderer : list) {
            layerColors_.put(renderer, MARKER_COLORS[ct % MARKER_COLORS.length]);
            ct++;
        }
    }

    private Color MarkerColor(MarkerRenderer renderer) {
        return layerColors_.get(renderer);
    }

    private MarkerRenderer LayerRenderer(ILayer layer) {
        for(MarkerRenderer renderer : markerRenderers_) {
            if(renderer.Layer().equals(layer)) {
                return renderer;
            }
        }

        throw new RuntimeException("Renderer not found.");
    }

    private Point AdjustPoint(Point point, View view) {
        return new Point(point.X() + view.Bounds().Left(),
                         point.Y() + view.Bounds().Top());
    }

    private Point FromCoordinates(Coordinates coord) {
        return projection_.FromCoordinates(coord, zoomLevels_ - 1);
    }

    private Coordinates ToCoordinates(Point point) {
        return projection_.ToCoordinates(point, zoomLevels_ - 1);
    }
}
