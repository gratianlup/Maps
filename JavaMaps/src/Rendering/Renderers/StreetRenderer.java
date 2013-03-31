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
import Rendering.*;
import Rendering.Utils.*;
import Core.*;
import java.awt.Graphics2D;
import java.util.*;
import java.awt.image.*;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.AlphaComposite;
import Rendering.Info.StreetInfo;

public final class StreetRenderer implements IRenderer {
    // Constants representing the width of various street types.
    static final double STREET_WIDTH = 8;
    static final double AVENUE_WIDTH = 12;
    static final double BOULEVARD_WIDTH = 16;
    static final double NAME_DISTANCE = 256;

    /*
     ** Members.
     */
    private IMapProvider provider_;
    private IRendererHost host_;
    private ILayer layer_;
    private IProjection projection_;
    private LineTree lines_;
    private LineCollection visibleLines_;
    private int zoomLevels_;
    private java.awt.Color[] streetColors_;
    private ArrayList<Line> avenueList_;
    private ArrayList<Line> boulevardList_;
    private Font[] fonts_;
    private Font[] largeFonts_;
    private double opacity_;
    private boolean visible_;

    /*
     ** Constructors.
     */
    public StreetRenderer(ILayer layer, IRendererHost host) {
        host_ = host;
        layer_ = layer;
        provider_ = host.MapProvider();
        projection_ = provider_.Projection();
        zoomLevels_ = provider_.ZoomLevels();
        visibleLines_ = new LineCollection();

        // Initialize the used data structures
        // and load/preprocess the street information.
        Region2D maxBounds = provider_.MapBounds(zoomLevels_ - 1);
        lines_ = new LineTree(maxBounds.Width(), maxBounds.Height(), zoomLevels_);
        SetStreetColors();
        SetFonts();
        LoadStreets();
        avenueList_ = new ArrayList<Line>(100);
        boulevardList_ = new ArrayList<Line>(100);
        opacity_ = 1.0;
        visible_ = true;
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
    
    public boolean HasPrefetcher() { 
        return false; 
    }
    
    public IPrefetcher Prefetcher() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public int ZIndex() { 
        return 1; // Above the image layer.
    }
    
    public void SetZIndex(int value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean MouseDown(Point point, View view, Modifier modifier) {
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
    
    public boolean MouseMoved(Point point, View view, Modifier modifier) { 
        return false; 
    }
    
    public boolean MouseDragged(Point point, View view, Modifier modifier) { 
        return false; 
    }
    
    public boolean MouseCaptured() { 
        return false; }
    

    public double Opacity() { 
        return opacity_; 
    }
    
    public void SetOpacity(double value) { 
        opacity_ = value; 
    }

    public boolean Visible() { 
        return visible_; 
    }
    
    public void SetVisible(boolean value) {
        visible_ = value;
    }

    public void Render(View view) {
        Draw(view);
    }

    public IObjectInfo HitTest(Point point, View view) {
        // Project the point on the map sourface,
        // then try to find the nearest line.
        Point test = new Point(point.X() + view.Bounds().Left(),
                               point.Y() + view.Bounds().Top());
        LineTree.NearestInfo nearest = lines_.NearestLine(test, (int)view.Zoom());
        
        if((nearest == null) || (nearest.Line() == null)) {
            return null;
        }

        // Check if the street is close enough
        // to be considered as being under the mouse cursor,
        Street street = (Street)nearest.Line().Value();
        double streetWidth = ScaledStreetWidth(street.Type(), view.Zoom()) / 2;
        
        if(nearest.Distance() < streetWidth) {
            return new StreetInfo(street, this);
        }
        
        return null;
    }

    public void AddLine(Line line, int zoomLevel) {
        lines_.Add(line, zoomLevel);
    }

    public void RemoveLine(Line line, int zoomLevel) {
        lines_.Remove(line, zoomLevel);
    }

    public void UpdateName(Street street) {
        List<Coordinates> streetCoords = street.Coordinates();
        int coordCount = streetCoords.size();
        
        if(coordCount < 2) {
            return;
        }
        
        // Transform the coordinates of the points the form
        // the street into pixel mapped to the view.
        ArrayList<Point> points = new ArrayList<Point>(coordCount);
        ArrayList<Line> unnamed = new ArrayList<Line>(32);
        
        for(int i = 0; i < coordCount; i++) {
            points.add(projection_.FromCoordinates(streetCoords.get(i), 
                                                   zoomLevels_ - 1));
        }

        // Create lines from the street points.
        for(int i = 0; i < coordCount - 1; i++) {
            Line line = new Line(points.get(i), points.get(i + 1), street);
            Line found = lines_.FindLine(line, zoomLevels_ - 1);
            
            if(found != null) {
                unnamed.add(found);
            }
        }

        // Compute which lines should have the street name attached.
        AttachNames(unnamed, street);
    }

    /*
    ** Private methods.
    */
    private double StreetWidth(StreetType type) {
        switch(type) {
            case Street: { return STREET_WIDTH; }
            case Avenue: { return AVENUE_WIDTH; }
            case Boulevard: { return BOULEVARD_WIDTH; }
        }
        
        throw new RuntimeException("Street type not found");
    }

    private double ScaledStreetWidth(StreetType type, double zoom) {
        double scale = Math.pow(2, zoomLevels_ - zoom - 1);

        switch(type) {
            case Street: { return STREET_WIDTH / scale; }
            case Avenue: { return AVENUE_WIDTH / scale; }
            case Boulevard: { return BOULEVARD_WIDTH / scale; }
        }
        
        throw new RuntimeException("Street type not found");
    }

    private void SetStreetColors() {
        streetColors_ = new Color[3];
        Core.Color color = provider_.StreetColor(StreetType.Street);
        streetColors_[0] = new java.awt.Color(color.R(), color.G(), color.B(), color.Alpha());

        color = provider_.StreetColor(StreetType.Avenue);
        streetColors_[1] = new java.awt.Color(color.R(), color.G(), color.B(), color.Alpha());

        color = provider_.StreetColor(StreetType.Boulevard);
        streetColors_[2] = new java.awt.Color(color.R(), color.G(), color.B(), color.Alpha());
    }

    private java.awt.Color StreetColor(StreetType type) {
        return streetColors_[type.Index()];
    }

    private Stroke ScaledStreetStroke(StreetType type, double zoom) {
        return new BasicStroke((int)ScaledStreetWidth(type, zoom));
    }

    private void SetFonts() {
        fonts_ = new Font[3];
        fonts_[0] = new Font("Dialog", Font.BOLD, 14);
        fonts_[1] = new Font("Dialog", Font.PLAIN, 18);
        fonts_[2] = new Font("Dialog", Font.BOLD,  22);

        largeFonts_ = new Font[3];
        largeFonts_[0] = new Font("Dialog", Font.BOLD, 14);
        largeFonts_[1] = new Font("Dialog", Font.BOLD, 18);
        largeFonts_[2] = new Font("Dialog", Font.BOLD, 20);
    }

    private Font StreetFont(StreetType streetType) {
        return fonts_[streetType.Index()];
    }

    private Font LargeFont(StreetType streetType) {
        return largeFonts_[streetType.Index()];
    }

    private void AttachNames(ArrayList<Line> lines, Street street) {
        // COmpute the position of the street name.
        double dist = 0;
        int last = -1;
        int count = lines.size();
        
        for(int i = 0; i < count; i++) {
            Line line = lines.get(i);
            double length = Point.Distance(line.XA(), line.YA(),
                                           line.XB(), line.YB());
            line.SetLength((short)length);
            dist += length;
            
            if(dist > NAME_DISTANCE) {
                // The name bust be attached to this name.
                if((i - last) < 2) {
                    // A single line.
                    line.SetNameCount((short)(dist / NAME_DISTANCE));
                }
                else {
                    lines.get(last + ((i - last) / 2)).SetNameCount((short)1);
                }

                last = i;
                dist = 0;
            }
        }

        if(last == -1) {
            // The street must have at least one line
            // where the name should appear.
            lines.get(lines.size() / 2).SetNameCount((short)1);
        }
    }

    // Incarca toate strazile, la toate nivelurile de zoom.
    // Pentru niveluri de zoom mai mici decat cel maxim se aplica selectie
    // si simplificare de linii.
    private void LoadStreets() {
        // Load all streets at all valid zoom levels.
        // For all zoom levels below the maximum one a line simplification
        // algorithm is applied on each of the line to reduce rendering time.
        int count = provider_.StreetCount();
        HashMap<ObjectId, ArrayList<Point>> streetPoints = 
                new HashMap<ObjectId, ArrayList<Point>>(count);
        ArrayList<Line> unnamed = new ArrayList<Line>(32);
        Iterator<Street> streetIt = provider_.GetStreetIterator();
        
        while(streetIt.hasNext()) {
            Street street = streetIt.next();
            List<Coordinates> streetCoords = street.Coordinates();
            int coordCount = streetCoords.size();
            
            if(coordCount < 2) {
                continue; // Shouldn't happen im practice.
            }

            // Transform the coordinates to pixels.
            ArrayList<Point> points = new ArrayList<Point>(coordCount);
            
            for(int i = 0; i < coordCount; i++) {
                points.add(projection_.FromCoordinates(streetCoords.get(i), 
                                                       zoomLevels_ - 1));
            }

            // Create the lines based on the street points.
            streetPoints.put(street.Id(), points);
            unnamed.clear();
            
            for(int i = 0; i < coordCount - 1; i++) {
                Line line = new Line(points.get(i), points.get(i + 1), street);
                unnamed.add(line);
                lines_.Add(line, zoomLevels_ - 1);
            }

            AttachNames(unnamed, street);
        }

        // Run the simplification algorithm on each line
        // for each smaller zoom level than the maximum one.
        ArrayList<Point> selected = new ArrayList<Point>();
        double sx = 1.0; // The scaling factors for X and Y.
        double sy = 1.0;

        for(int zoom = zoomLevels_ - 2; zoom >= 0; zoom--) {
            double minDistance =  Math.pow(2, zoomLevels_ - zoom - 1);
            Region2D cntBounds = provider_.MapBounds(zoom);
            Region2D prevBounds = provider_.MapBounds(zoom + 1);
            sx *= cntBounds.Width() / prevBounds.Width();
            sy *= cntBounds.Height() / prevBounds.Height();
            streetIt = provider_.GetStreetIterator();
            
            while(streetIt.hasNext()) {
                Street street = streetIt.next();

                // If the width of the street is below 2 pixels
                // the street isn't shown at all.
                if(ScaledStreetWidth(street.Type(), zoom) < 2.0) {
                    continue;
                }

                // Simplifiy the points on the line.
                // A subsets of the points is selected which 
                // approximate the original lines fairly accurately.
                ArrayList<Point> points = streetPoints.get(street.Id());
                
                if(points == null || points.size() < 2) {
                    continue; // Shouldn't happen im practice.
                }
                
                selected.clear();
                LineSimplifier.Simplify(points, minDistance, selected);

                // It is guaranteed that at least two points remain.
                unnamed.clear();
                int selCount = selected.size();
                
                for(int i = 0; i < selCount - 1; i++) {
                    Point a = selected.get(i);
                    Point b = selected.get(i + 1);
                    Line line = new Line(a.X() * sx, a.Y() * sy,
                                         b.X() * sx, b.Y() * sy, street);
                    unnamed.add(line);
                    lines_.Add(line, zoom);
                }

                AttachNames(unnamed, street);
            }
        }
    }

    private void DrawName(Line line, Graphics2D g, View view, 
                          double scale, Font font, Color color) {
        // Draw the name of the street near the specified line.
        Street street = (Street)line.Value();
        
        if((line.NameCount() == 0) || (street.Name() == null)) {
            return;
        }

        double length = (double)line.Length();
        double viewX = view.Bounds().Left();
        double viewY = view.Bounds().Top();
        double dirX = 1.0;  // Controls the name orientation.
        double dirY = -1.0;
        double streetWidth = ScaledStreetWidth(street.Type(), 
                                               (int)Math.rint(view.Zoom()));

        // Compute the text size if not done already.
        if(line.NameHeight() == 0) {
            FontMetrics metrics = g.getFontMetrics(font);
            line.SetNameHeight((short)metrics.getHeight());
            line.SetNameWidth((short)metrics.stringWidth(street.Name()));
        }

        // Find the line angle and prevent a rotation greater
        // than ~110 degrees (the text would be hard to read).
        // textului cu mai mult de ~110 grade.
        double angle = Math.atan2(line.YB() - line.YA(), line.XB() - line.XA());
        
        if(Math.abs(angle) > 1.88) {
            dirX = -1.0;
            dirY = 1.0;
            angle += Math.PI;
            streetWidth += line.NameHeight() / 2;
        }
        
        g.setFont(font);
        g.setColor(color);

        // Translate and rotate the text in the start position.
        AffineTransform prevTransf = g.getTransform();
        g.translate((int)(line.XA() * scale - viewX),
                    (int)(line.YA() * scale - viewY));
        g.rotate(angle);

        if(line.NameCount() < 2) {
            // The name if showed a single time.
            g.translate(dirX * (length / 2) * scale - (line.NameWidth() / 2),
                        dirY * streetWidth * scale);
            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(-2, -line.NameHeight() + 2, 
                       line.NameWidth() + 5, line.NameHeight() + 3);
            
            g.setColor(color);
            g.drawString(street.Name(), 0, 0);
        }
        else {
            // The line is split into multiple segments
            // and each one will have one of the words.
            int count = line.NameCount();
            double segLength = length / count;
            g.translate(0, dirY * streetWidth * scale);

            for(int i = 0; i < count; i++) {
                g.translate(dirX * (segLength / 2) * scale - 
                            (line.NameWidth() / 2), 0);

                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(-2, -line.NameHeight() + 2, 
                           line.NameWidth() + 5, line.NameHeight() + 3);
                g.setColor(color);
                g.drawString(street.Name(), 0, 0);
                
                g.translate(dirX * (segLength / 2) * scale + 
                            (line.NameWidth() / 2), 0);
            }
        }

        // Restore the old transformation matrix.
        g.setTransform(prevTransf);
    }

    private void DrawLine(Line line, Graphics2D g, View view, double scale) {
        // Draws the line at the specified scale factor.
        double viewX = view.Bounds().Left();
        double viewY = view.Bounds().Top();
        Street street = (Street)line.Value();        

        g.setStroke(ScaledStreetStroke(street.Type(), view.Zoom()));
        g.setColor(StreetColor(street.Type()));
        g.drawLine((int)(line.XA() * scale - viewX), (int)(line.YA() * scale - viewY),
                   (int)(line.XB() * scale - viewX), (int)(line.YB() * scale - viewY));
    }

    private void DrawNames(LineCollection lines, Graphics2D g, 
                           View view, double scale) {
        // Draws all street names at the specified scale factor.
        Iterator<Line> lineIt = lines.Objects().iterator();
        avenueList_.clear();
        boulevardList_.clear();

        // First draw the name for the standard streets, while puting 
        // the other types of streets in separate lists to be draw later.
        while(lineIt.hasNext()) {
            Line line = lineIt.next();
            StreetType type = ((Street)line.Value()).Type();
            
            switch(type) {
                case Street: {
                    DrawName(line, g, view, scale, StreetFont(type), Color.WHITE);
                    break;
                }
                case Avenue: { avenueList_.add(line); break; }
                case Boulevard: { boulevardList_.add(line); break; }
            }
        }

        // Draw the names for other types of streets based on their importance
        // (a Bulevard is considere more important than an Avenue).
        int count = avenueList_.size();
        
        for(int i = 0; i < count; i++) {
            StreetType type = ((Street)avenueList_.get(i).Value()).Type();
            DrawName(avenueList_.get(i), g, view, scale, StreetFont(type), Color.WHITE);
        }

        count = boulevardList_.size();
        
        for(int i = 0; i < count; i++) {
            StreetType type = ((Street)boulevardList_.get(i).Value()).Type();
            DrawName(boulevardList_.get(i), g, view, scale, StreetFont(type), Color.WHITE);
        }
    }

    private void DrawImpl(LineCollection lines, Graphics2D g, View view, double scale) {
        // Draw all lines at the specified scaling factor.
        Iterator<Line> lineIt = lines.Objects().iterator();
        avenueList_.clear();
        boulevardList_.clear();
        
        // First draw the name for the standard streets, while puting 
        // the other types of streets in separate lists to be draw later.
        while(lineIt.hasNext()) {
            Line line = lineIt.next();
            StreetType type = ((Street)line.Value()).Type();
            
            switch(type) {
                case Street: {
                    DrawLine(line, g, view, scale);
                    break;
                }
                case Avenue: { avenueList_.add(line); break; }
                case Boulevard: { boulevardList_.add(line); break; }
            }
        }

        // Draw the other types of streets based on their importance
        // (a Bulevard is considere more important than an Avenue).
        int count = avenueList_.size();
        for(int i = 0; i < count; i++) {
            DrawLine(avenueList_.get(i), g, view, scale);
        }

        count = boulevardList_.size();
        for(int i = 0; i < count; i++) {
            DrawLine(boulevardList_.get(i), g, view, scale);
        }

        // Now draw the street names. It is done last
        // so that the names are not covered by other streets.
        DrawNames(lines, g, view, scale);
    }

    private void Draw(View view) {
        // Activate antialiasing (makes the lines look much nicer).
        VolatileImage buffer = view.GetBuffer(this);
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        Region2D bounds = view.Bounds();
        int nextLevel = (int)Math.ceil(view.Zoom());

        // Increase the view range used to query the tree
        // with the width of the largest street so that the streets
        // on the edges are included too.
        double inflateVal = 2 * ScaledStreetWidth(StreetType.Boulevard,
                                                  view.Zoom());

        // If the map is being zoomed in/out perform an interpolation
        // between the lines for the current level and the next/previous level.
        double position = view.Zoom() - Math.floor(view.Zoom());
        
        if(position < 0.5) {
            // The lines from the previous level are displayed.
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                           (float)(opacity_ * (1.0 - position*2))));
            visibleLines_.Clear();
            view.PreviousBounds().Inflate(inflateVal, inflateVal);
            lines_.Intersect(view.PreviousBounds(), (int)view.Zoom(), visibleLines_);
            view.PreviousBounds().Inflate(-inflateVal, -inflateVal);

            DrawImpl(visibleLines_, g, view, 1.0 + position);            
        }
        else if(position >= 0.1) {
            // The lines from the next level are displayed.
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                           (float)(opacity_ * Math.min(1.0, position * Math.E - 0.1))));
            visibleLines_.Clear();
            view.NextBounds().Inflate(inflateVal, inflateVal);
            lines_.Intersect(view.NextBounds(), nextLevel, visibleLines_);
            view.NextBounds().Inflate(-inflateVal, -inflateVal);

            DrawImpl(visibleLines_, g, view, 0.5 + (position / 2));
        }

        g.dispose();
    }
}
