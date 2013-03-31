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
import Rendering.Info.StreetInfo;
import Rendering.Info.StreetPointInfo;
import Rendering.Renderers.StreetRenderer;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class LineEditor implements IRenderer {
    // Contains information associated with a point on the line.
    // The points on a line form a doubly-linked list.
    public class PointVisual implements IVisual {
        private Street street_;
        private Point position_;
        private int index_;            // The position of the point in the list.
        private PointVisual previous_; // The previou point in the list.
        private PointVisual next_;     // The next point  in the list.

        /*
        ** Constructors.
        */
        public PointVisual(Street street, Point position, int index) {
            street_ = street;
            position_ = position;
            index_ = index;
        }

        /*
        ** Public methods.
        */
        public Point Position() { 
            return position_; 
        }
        
        public void SetPosition(Point value) { 
            position_ = value; 
        }
        
        public Street Street() { 
            return street_; 
        }

        public PointVisual Previous() { 
            return previous_; 
        }
        
        public void SetPrevious(PointVisual value) { 
            previous_ = value; 
        }
        
        public PointVisual Next() { 
            return next_; 
        }
        
        public void SetNext(PointVisual value) { 
            next_ = value; 
        }

        public int Index() { 
            return index_; 
        }
        
        public void SetIndex(int value) { 
            index_ = value; 
        }

        public void Draw(Graphics2D g, View view) {
            double x = position_.X() - view.Bounds().Left();
            double y = position_.Y() - view.Bounds().Top();
            double size;

            // Set the color of the point based on its state.
            if(this == selectedPoint_) {
                g.setColor(SELECTED_POINT_COLOR);
                size = SELECTED_POINT_SIZE;
            }
            else {
                // The point is part of the selected street.
                if(street_ == selectedStreet_) {
                    g.setColor(STREET_POINT_COLOR);
                    size = STREET_POINT_SIZE;
                }
                else {
                    // Just a typical point.
                    g.setColor(POINT_COLOR);
                    size = POINT_SIZE;
                }
            }

            double centerX = x - (size / 2);
            double centerY = y - (size / 2);
            g.fillOval((int)centerX, (int)centerY, (int)size, (int)size);
            g.setColor(Color.BLACK);
            g.drawOval((int)centerX, (int)centerY, (int)size, (int)size);

            // Display the text (only for the selected point/street).
            if((this == selectedPoint_) || (street_ == selectedStreet_)) {
                FontMetrics metrics = g.getFontMetrics(pointFont_);
                String name = Integer.toString(index_);
                double nameX = x - (metrics.stringWidth(name) / 2) - 1;
                double nameY = y + (metrics.getHeight() / 2);

                g.setColor(FONT_COLOR);
                g.drawString(name, (int)nameX, (int)nameY);
            }
        }

        @Override
        public int hashCode() {
            return street_.hashCode() ^ position_.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            PointVisual other = (PointVisual) obj;
            return (street_.equals(other.street_)) &&
                   (position_.equals(other.position_));
        }
    }

    /*
    ** Constants.
    */
    private final double POINT_SIZE = 12;
    private final double STREET_POINT_SIZE = 20;
    private final double SELECTED_POINT_SIZE = 20;
    private final Color POINT_COLOR = new Color(255, 255, 255, 220);    // Alb.
    private final Color STREET_POINT_COLOR = new Color(10, 227, 255);   // Albastru.
    private final Color SELECTED_POINT_COLOR = new Color(125, 222, 51); // Verde.
    private final double FONT_SIZE = 8;
    private final Color FONT_COLOR = Color.BLACK;

    /*
    ** Members.
    */
    private IMapProvider provider_;
    private IRendererHost host_;
    private StreetRenderer streetRenderer_;
    private IProjection projection_;
    private Region2D maxBounds_;
    private boolean visible_;
    private int zoomLevels_;

    // Maps a street to its first point.
    private HashMap<Street, PointVisual> streetStart_; 
    private PointTree<PointVisual> points_;
    private BasicCollection<PointVisual> visiblePoints_;
    private ArrayList<PointVisual> frontPoints_;

    private Font pointFont_;
    private PointVisual selectedPoint_;
    private Street selectedStreet_;
    private boolean mouseCaptured_;
    private Point dragStart_;
    private ArrayList<Point> startPositions_;
    private ArrayList<PointVisual> startVisuals_;

    /*
    ** Constructors.
    */
    public LineEditor(StreetRenderer streetRenderer, IRendererHost host) {
        host_ = host;
        streetRenderer_ = streetRenderer;
        provider_ = host.MapProvider();
        projection_ = provider_.Projection();
        zoomLevels_ = provider_.ZoomLevels();
        visible_ = true;

        // Creeaza structurile de date folosite.
        streetStart_ = new HashMap<Street, PointVisual>();
        visiblePoints_ = new BasicCollection<PointVisual>();
        maxBounds_ = provider_.MapBounds(zoomLevels_ - 1);
        points_ = new PointTree<PointVisual>(maxBounds_.Width(), maxBounds_.Height());
        startPositions_ = new ArrayList<Point>();
        startVisuals_ = new ArrayList<PointVisual>();
        frontPoints_ = new ArrayList<PointVisual>();
        pointFont_ = new Font("Dialog", Font.ITALIC, (int)FONT_SIZE);

        LoadPoints();
    }

    /*
     ** Public methods.
     */
    public boolean IsEditor() { return true; }
    public ILayer Layer() { return streetRenderer_.Layer(); }
    public boolean HasPrefetcher() { return false; }
    public IPrefetcher Prefetcher() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public int ZIndex() { return Integer.MAX_VALUE - 2; } // Sub overlay.
    public void SetZIndex(int value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean MouseDown(Point point, View view, Modifier modifier) {
        if(!visible_) {
            return false;
        }

        // Search for a point at the specified position.
        PointVisual pointVis = HitTestImpl(point, view);
        
        if(pointVis != null) {
            if(modifier.IsSet(Modifier.BUTTON_RIGHT)) {
                // Add a new point near the found one.
                selectedPoint_ = AddPoint(AdjustPoint(point, view),
                                          pointVis.Street(), pointVis.Index() + 1);
                if(selectedPoint_ == null) {
                    selectedStreet_ = null;
                }
                else {
                    // Select the associated street.
                    selectedStreet_ = selectedPoint_.Street();
                }
            }
            else {
                // Select the street associated with the selected point.
                selectedPoint_ = pointVis;
                selectedStreet_ = pointVis.Street();
            }
            
            // Start the point dragging operation.
            mouseCaptured_ = true;
            dragStart_ = point;
            AddStartPositions(selectedPoint_);

            // Notify the parent and force a redraw.
            host_.SendAction(Action.ObjectSelected(HitTest(point, view)));
            host_.Repaint();
            return true;
        }
        else {
            if(modifier.IsSet(Modifier.KEY_SHIFT)) {
                // Add a new street having as the initial point
                // the specified point found under the mouse cursor.
                selectedStreet_ = AddStreet(AdjustPoint(point, view));
                
                if(selectedStreet_ != null) {
                    selectedPoint_ = streetStart_.get(selectedStreet_);
                    mouseCaptured_ = true;
                    dragStart_ = point;
                    AddStartPositions(selectedPoint_);

                    // Notify the parent and force a redraw.
                    host_.SendAction(Action.ObjectSelected(HitTest(point, view)));
                    host_.Repaint();
                    return true;
                }
            }

            // Deselect the old selected point.
            selectedPoint_ = null;
            host_.Repaint();
        }

        // If no point has been found search for a line.
        IObjectInfo info = streetRenderer_.HitTest(point, view);
        
        if(info != null) {
            assert(info.Type() == InfoType.Street);
            selectedStreet_ = ((StreetInfo)info).Street();
            mouseCaptured_ = true;
            dragStart_ = point;
            AddStartPositions(selectedStreet_);

            // Notify the parent and force a redraw.
            host_.SendAction(Action.ObjectSelected(info));
            host_.Repaint();
            return true;
        }
        else {
            // Delesect the old selected line.
            selectedStreet_ = null;
            host_.Repaint();
        }

        // The event is sent to the other modules.
        return false;
    }
    
    public boolean MouseUp(Point point, View view, Modifier modifier) {
        if(!visible_) {
            return false;
        }
        
        if(mouseCaptured_) {
            if(selectedStreet_ != null) {
                streetRenderer_.UpdateName(selectedStreet_);
            }
            
            mouseCaptured_ = false;
            host_.Repaint();
            return true;
        }

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
        if(!visible_) {
            return false;
        }

        if(selectedPoint_ != null) {
            // A single point is selected and must be dragged.
            MovePoint(selectedPoint_, point,
                      startPositions_.get(0), selectedPoint_.Street());
        }
        else {
            // An entire street is selected and must be dragged.
            int count = startPositions_.size();
            
            for(int i = 0; i < count; i++) {
                if(!MovePoint(startVisuals_.get(i),point, 
                             startPositions_.get(i), selectedStreet_)) {
                    break;
                }
            }
        }

        host_.Repaint();
        return true;
    }

    public IObjectInfo HitTest(Point point, View view) {
        PointVisual pointVis = HitTestImpl(point, view);
        if(pointVis != null) {
            return new StreetPointInfo(pointVis.Street(), this, pointVis.Position(),
                                       ToCoordinates(pointVis.Position()), pointVis.Index());
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

    public void RemovePoint(Street street, int index) {
        assert(street != null);
        assert(index >= 0 && index < street.Coordinates().size());
        // ------------------------------------------------
        PointVisual pointVis = streetStart_.get(street);
        
        if(pointVis == null) {
            return;
        }

        // Find the associated visual element.
        for(int i = 0; i < index; i++) {
            pointVis = pointVis.Next();
        }

        PointVisual prevVis = pointVis.Previous();
        PointVisual nextVis = pointVis.Next();

        // Update the lines and the connections between the points.
        if(pointVis.Previous() == null) {
            if(pointVis.Next() == null) {
                // Notify that this has been the last point
                // and the entire streem must be removed.
                StreetInfo info = new StreetInfo(street, this);
                Action action = Action.ObjectRemoved(info);
                host_.SendAction(action);
                
                if(!action.Valid()) {
                    return;
                }
            }
            else {
                RemoveLine(pointVis.Position(), nextVis.Position(), street);
                nextVis.SetPrevious(null);
                streetStart_.put(street, nextVis);
            }
        }
        else if(pointVis.Next() == null) {
            // The last point in the list.
            RemoveLine(prevVis.Position(), pointVis.Position(), street);
            prevVis.SetNext(null); // Rupe legatura.
        }
        else {
            // Two lines must be removed and replaced by a single one.
            RemoveLine(prevVis.Position(), pointVis.Position(), street);
            RemoveLine(pointVis.Position(), nextVis.Position(), street);
            AddLine(prevVis.Position(), nextVis.Position(), street);

            prevVis.SetNext(nextVis);
            nextVis.SetPrevious(prevVis);
        }

        // Decrease the index of all following points with 1.
        while(nextVis != null) {
            nextVis.SetIndex(nextVis.Index() - 1);
            nextVis = nextVis.Next();
        }

        points_.Remove(pointVis);
        host_.Repaint();
    }

    public void RemoveStreet(Street street) {
        assert(street != null);
        // ------------------------------------------------
        // Remove all lines and points associated with the street.
        List<Coordinates> list = street.Coordinates();
        for(int i = 0; i < list.size(); i++) {
            RemovePoint(street, 0);
        }
    }

    public void NameChanged(Street street) {
        assert(street != null);
        // ------------------------------------------------
        streetRenderer_.UpdateName(street);
    }

    public BasicCollection<PointVisual> NearPoints(Point point, double maxDistance) {
        assert(point != null);
        assert(maxDistance >= 0);
        // ------------------------------------------------
        // Get the list with the nearest points.
        BasicCollection<PointVisual> list = new BasicCollection<PointVisual>();
        points_.Near(point, maxDistance, list);
        return list;
    }

    public void Render(View view) {
        // The editor is shown only if the view is set at maximum zoom.
        if((int)view.Zoom() != (zoomLevels_ - 1)) {
            return;
        }

        // Activate antialiasing (makes the lines look nicer).
        VolatileImage buffer = view.GetBuffer(this);
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

        // Find the points visible in the current view
        // and draw only these ones.
        Region2D bounds = view.Bounds();
        double inflateVal = 2 * POINT_SIZE;

        visiblePoints_.Clear();
        bounds.Inflate(inflateVal, inflateVal);
        points_.Intersect(bounds, visiblePoints_);
        bounds.Inflate(-inflateVal, -inflateVal);

        Draw(visiblePoints_, g, view);
        g.dispose();
    }

    /*
     ** Private methods.
     */
    private void LoadPoints() {
        Iterator<Street> streetIt = provider_.GetStreetIterator();
        
        while(streetIt.hasNext()) {
            Street street = streetIt.next();

            // Add all points which define the street.
            List<Coordinates> coords = street.Coordinates();
            PointVisual previous = null;
            int count = coords.size();
            
            for(int i = 0; i < count; i++) {
                // The coordinates are stored in pixels,
                // adjusted for the maximum valid zoom level.
                Point point = FromCoordinates(coords.get(i));
                PointVisual pointVis = new PointVisual(street, point, i);

                if(i == 0) {
                    // The first point is connected with the street.
                    streetStart_.put(street, pointVis);
                }
                else {
                    // Connect the previous point to the current one.
                    previous.SetNext(pointVis);
                    pointVis.SetPrevious(previous);
                }

                points_.Add(pointVis);
                previous = pointVis;
            }
        }
    }

    private void Draw(BasicCollection<PointVisual> points,
                      Graphics2D g, View view) {
        if(points.Count() == 0) {
                              return;
                          }

        // The points from the selected street are drawn
        // only at the end so that they are above all other points.
        frontPoints_.clear();
        Iterator<PointVisual> pointIt = points.Objects().iterator();
        
        while(pointIt.hasNext()) {
            PointVisual pointVis = pointIt.next();
            
            if(pointVis.Street() == selectedStreet_) {
                frontPoints_.add(pointVis);
            }
            else {
                pointVis.Draw(g, view);
            }
        }

        // Draw the points from the selected line.
        int count = frontPoints_.size();
        
        for(int i = 0; i < count; i++) {
            frontPoints_.get(i).Draw(g, view);
        }
    }

    private PointVisual HitTestImpl(Point point, View view) {
        Point test = AdjustPoint(point, view);
        PointTree<PointVisual>.NearestInfo nearest = points_.NearestPoint(test);
        
        if(nearest == null) {
            return null;
        }

        if(nearest.Distance() < POINT_SIZE) {
            return nearest.Value();
        }

        return null; 
    }

    private Point AdjustPoint(Point point, View view) {
        return new Point(point.X() + view.Bounds().Left(),
                         point.Y() + view.Bounds().Top());
    }

    private void AddStartPositions(Street street) {
        startPositions_.clear();
        startVisuals_.clear();
        PointVisual pointVis = streetStart_.get(street);

        do {
            startPositions_.add(new Point(pointVis.Position()));
            startVisuals_.add(pointVis);
            pointVis = pointVis.Next();
        } while(pointVis != null);
    }

    private void AddStartPositions(PointVisual pointVis) {
        startPositions_.clear();
        startPositions_.add(new Point(pointVis.Position()));
        startVisuals_.add(pointVis);
    }

    private Point FromCoordinates(Coordinates coord) {
        return projection_.FromCoordinates(coord, zoomLevels_ - 1);
    }

    private Coordinates ToCoordinates(Point point) {
        return projection_.ToCoordinates(point, zoomLevels_ - 1);
    }

    private void AddLine(Point a, Point b, Street street) {
        streetRenderer_.AddLine(new Line(a, b, street), zoomLevels_ - 1);
    }

    private void RemoveLine(Point a, Point b, Street street) {
        streetRenderer_.RemoveLine(new Line(a, b, street), zoomLevels_ - 1);
    }

    private void UpdateLines(PointVisual pointVis, Point oldPoint) {
        // Update the lines that form a street after a point has been deletet
        // (old lines are removed and new ones are added).
        Street street = pointVis.Street();

        if(pointVis.Previous() == null) {
            // The first point, at most one line must be removed and re-addded.
            if(pointVis.Next() == null) {
                return;
            }

            RemoveLine(oldPoint, pointVis.Next().Position(), street);
            AddLine(pointVis.Position(), pointVis.Next().Position(), street);
        }
        else if(pointVis.Next() == null) {
            // The last point, a line must be removed and re-added.
            RemoveLine(pointVis.Previous().Position(), oldPoint, street);
            AddLine(pointVis.Previous().Position(), pointVis.Position(), street);
        }
        else {
            // Two lines must be removed and re-added
            // when the point is in the middle of the list.
            RemoveLine(pointVis.Previous().Position(), oldPoint, street);
            AddLine(pointVis.Previous().Position(), pointVis.Position(), street);

            RemoveLine(oldPoint, pointVis.Next().Position(), street);
            AddLine(pointVis.Position(), pointVis.Next().Position(), street);
        }
    }

    private boolean MovePoint(PointVisual pointVis, Point mousePoint,
                              Point start, Street street) {
        // A point forming a street has been moved.
        // Notify the parent and redraw the affected street lines.
        double dx = mousePoint.X() - dragStart_.X();
        double dy = mousePoint.Y() - dragStart_.Y();
        Point oldPos = pointVis.Position();
        Point newPos = new Point(start.X() + dx, start.Y() + dy);

        StreetPointInfo info = new StreetPointInfo(street, this, newPos, 
                                                   ToCoordinates(newPos), 
                                                   pointVis.Index());
        Action action = Action.ObjectMoved(info);
        host_.SendAction(action);
        
        if (action.Valid()) {
            points_.Remove(pointVis);
            pointVis.SetPosition(newPos);
            points_.Add(pointVis);
            
            // Recreate the affected lines.
            UpdateLines(pointVis, oldPos);
            return true;
        }

        return false; // Nu s-a putut face mutarea.
    }

    // Adauga un nou punct pe o strada la pozitia specificata.
    // Parintele este notificat si liniile sunt adaugate.
    private PointVisual AddPoint(Point point, Street street, int index) {
        // Add a new point on the street at the specified position.
        // Some new lines must be added.
        PointVisual pointVis = new PointVisual(street, point, index);
        selectedPoint_ = pointVis;

        StreetPointInfo info = new StreetPointInfo(street, this, point,
                                                   ToCoordinates(point), index);
        Action action = Action.ObjectAdded(info);
        host_.SendAction(action);
        
        if(action.Valid()) {
            points_.Add(pointVis);

            // Find the point before this one
            // so that they can be connected using the linked list.
            PointVisual prevVis = streetStart_.get(street);
            for(int i = 0; i < index - 1; i++) {
                prevVis = prevVis.Next();
            }

            if(index == 0) {
                // This becomes the first point of the street.
                if(prevVis != null) {
                    AddLine(pointVis.Position(), prevVis.Position(), street);
                    pointVis.SetNext(prevVis);
                    prevVis.SetPrevious(pointVis);
                }

                streetStart_.put(street, pointVis);
            }
            else {
                pointVis.SetNext(prevVis.Next());
                prevVis.SetNext(pointVis);
                pointVis.SetPrevious(prevVis);
                
                if(pointVis.Next() != null) {
                    // Remove the line connecting the previous point
                    // and the (currently) next one and add two new lines
                    // that go through the new point.
                    RemoveLine(prevVis.Position(), pointVis.Next().Position(), street);
                    AddLine(prevVis.Position(), pointVis.Position(), street);
                    AddLine(pointVis.Position(), pointVis.Next().Position(), street);
                    pointVis.Next().SetPrevious(pointVis);
                }
                else {
                    // Add a line at the end of the street.
                    AddLine(prevVis.Position(), pointVis.Position(), street);
                }
            }

            // Increment all point indices after the new one.
            PointVisual p = pointVis.Next();
            
            while(p != null) {
                p.SetIndex(p.Index() + 1);
                p = p.Next();
            }
        }

        return pointVis;
    }

    private Street AddStreet(Point point) {
        // Add a new street having the specified start point.
        Street street = new Street(ObjectId.NewId(), StreetType.Street);
        StreetInfo info = new StreetInfo(street, this);

        if(host_.SendAction(Action.ObjectAdded(info)).Valid()) {
            AddPoint(point, street, 0);
        }

        return street;
    }
}
