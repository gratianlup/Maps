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
import Rendering.Info.LinkInfo;
import Rendering.Info.NodeInfo;
import Rendering.Info.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.VolatileImage;
import java.awt.AlphaComposite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class NodeEditor implements IRenderer {
    public final class NodeVisual implements IVisual {
        private Node node_;
        private Point position_;
        private ArrayList<LinkVisual> inLinks_;
        private ArrayList<LinkVisual> outLinks_;

        /*
        ** Constructors.
        */
        public NodeVisual(Node node, Point position) {
            node_ = node;
            position_ = position;
            inLinks_ = new ArrayList<LinkVisual>(2);
            outLinks_ = new ArrayList<LinkVisual>(2);
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
        
        public Node Node() { 
            return node_; 
        }
        
        public List<LinkVisual> InLinks() { 
            return inLinks_; 
        }
        
        public List<LinkVisual> OutLinks() { 
            return outLinks_; 
        }

        @Override
        public int hashCode() {
            return node_.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            NodeVisual other = (NodeVisual) obj;
            return (node_.equals(other.node_));
        }

        public void Draw(Graphics2D g, View view) {
            // Draw the node, then all associated links.
            double x = position_.X() - view.Bounds().Left();
            double y = position_.Y() - view.Bounds().Top();

            if((this == selectedNode_) ||
               (this == targetNode_)) {
                g.setColor(SELECTED_NODE_COLOR);
            }
            else {
                g.setColor(NODE_COLOR);
            }

            // Draw a citcle.
            double centerX = x - (NODE_SIZE / 2);
            double centerY = y - (NODE_SIZE / 2);
            g.setStroke(nodeStroke_);
            g.fillOval((int)centerX, (int)centerY, (int)NODE_SIZE, (int)NODE_SIZE);
            g.setColor(Color.BLACK);
            g.drawOval((int)centerX, (int)centerY, (int)NODE_SIZE, (int)NODE_SIZE);
        }
    }


    public final class LinkVisual implements IVisual {
        private static final int LINE_STEPS = 16;

        /*
        ** Members.
        */
        private Link link_;
        private NodeVisual parentNode_;
        private NodeVisual nextNode_;
        private Line[] lines_;
        private double angle_; // The angle formed by the line.

        /*
        ** Constructors.
        */
        public LinkVisual(Link link, NodeVisual first, NodeVisual last) {
            link_ = link;
            parentNode_ = first;
            nextNode_ = last;
            lines_ = new Line[LINE_STEPS - 1];
        }

        /*
        ** Public methods.
        */
        public Point Position() { 
            return parentNode_.Position(); 
        }
        
        public Link Link() { 
            return link_; 
        }
        
        public void SetLink(Link value) { 
            link_ = value; 
        }

        public NodeVisual ParentNode() { 
            return parentNode_; 
        }
        
        public void SetParentNode(NodeVisual value) { 
            parentNode_ = value; 
        }

        public NodeVisual NextNode() { 
            return nextNode_; 
        }
        
        public void SetNextNode(NodeVisual value) { 
            nextNode_ = value; 
        }

        public void AddLines() {
            // Add a line placed at the end of the nodes.
            Point a = parentNode_.Position();
            Point b = nextNode_.Position();
            angle_ = Math.atan2(b.Y() - a.Y(), b.X() - a.X());

            // Compute the fixed points (anchors).
            double ax1 = a.X() + (NODE_SIZE/2 * Math.cos((Math.PI / 2) - angle_));
            double ay1 = a.Y() - (NODE_SIZE/2 * Math.sin((Math.PI / 2) - angle_));
            double ax2 = b.X() + (NODE_SIZE/2 * Math.cos((Math.PI / 2) - angle_));
            double ay2 = b.Y() - (NODE_SIZE/2 * Math.sin((Math.PI / 2) - angle_));

            // Compute the control points.
            double cx1 = a.X() + (NODE_SIZE*1.5 * Math.cos((Math.PI / 2) - angle_));
            double cy1 = a.Y() - (NODE_SIZE * Math.sin((Math.PI / 2) - angle_));
            double cx2 = b.X() + (NODE_SIZE*1.5 * Math.cos((Math.PI / 2) - angle_));
            double cy2 = b.Y() - (NODE_SIZE * Math.sin((Math.PI / 2) - angle_));

            CreateBezierLines(ax1, ay1, ax2, ay2, cx1, cy1, cx2, cy2);
        }

        public void RemoveLines() {
            // Remove the lines that form the link from the line tree.
            for(int i = 0; i < (LINE_STEPS - 1); i++) {
                linkLines_.Remove(lines_[i], zoomLevels_ - 1);
            }
        }

        public void Draw(Graphics2D g, View view) {
            double dx = view.Bounds().Left();
            double dy = view.Bounds().Top();

            // Select the color and line width of the line.
            if(this == selectedLink_) {
                g.setColor(SELECTED_LINK_COLOR);
                g.setStroke(selectedLinkStroke_);
            }
            else {
                if((parentNode_ == selectedNode_) ||
                   (nextNode_ == selectedNode_)) {
                    g.setColor(NODE_LINK_COLOR);
                    g.setStroke(selectedLinkStroke_);
                }
                else {
                    g.setColor(LINK_COLOR);
                    g.setStroke(linkStroke_);
                }
            }

            // Draw the main line.
            for(int i = 0; i < (LINE_STEPS - 1); i++) {
                Line line = lines_[i];
                g.drawLine((int)(line.XA() - dx), (int)(line.YA() - dy),
                           (int)(line.XB() - dx), (int)(line.YB() - dy));
            }
            
            // Draw the other two lines which form the arrow.
            // The first point is the end of the main line.
            // The second point is computed based on the angle formed
            // with the angle of the main line.
            double arrowX = lines_[LINE_STEPS / 2].XB();
            double arrowY = lines_[LINE_STEPS / 2].YB();
            double x1 = arrowX + (ARROW_SIZE * Math.cos(ARROW_ANGLE - angle_)) - dx;
            double y1 = arrowY - (ARROW_SIZE * Math.sin(ARROW_ANGLE - angle_)) - dy;
            double x2 = arrowX + (ARROW_SIZE * Math.cos(-ARROW_ANGLE - angle_)) - dx;
            double y2 = arrowY - (ARROW_SIZE * Math.sin(-ARROW_ANGLE - angle_)) - dy;

            g.drawLine((int)(arrowX - dx), (int)(arrowY - dy), (int)x1, (int)y1);
            g.drawLine((int)(arrowX - dx), (int)(arrowY - dy), (int)x2, (int)y2);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;

            LinkVisual other = (LinkVisual) obj;
            if(link_ == null) {
                return parentNode_.equals(other.parentNode_) &&
                       nextNode_.equals(other.nextNode_);
            }
            else return link_.equals(other.link_);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 23 * hash + (link_ != null ? link_.hashCode() : 0);
            return hash;
        }

        /*
        ** Private methods.
        */
        private void CreateBezierLines(double anchor1X, double anchor1Y,
                                       double anchor2X, double anchor2Y,
                                       double control1X, double control1Y,
                                       double control2X, double control2Y) {
            double u = 0;
            double stepU = 1.0 / (LINE_STEPS - 1);
            double prevX = 0;
            double prevY = 0;
            
            for(int i = 0; i < LINE_STEPS; i++) {
                double x = Math.pow(u,3)*(anchor2X+3*(control1X-control2X)-anchor1X) +
                           3*Math.pow(u,2)*(anchor1X-2*control1X+control2X) +
                           3*u*(control1X-anchor1X)+anchor1X;
                double y = Math.pow(u,3)*(anchor2Y+3*(control1Y-control2Y)-anchor1Y) +
                           3*Math.pow(u,2)*(anchor1Y-2*control1Y+control2Y) +
                           3*u*(control1Y-anchor1Y)+anchor1Y;
                u += stepU;

                if(i > 0) {
                    // Add the line formed from the previous and current points.
                    lines_[i - 1] = new Line(prevX, prevY, x, y, this);
                    linkLines_.Add(lines_[i - 1], zoomLevels_ - 1);
                }
                
                prevX = x;
                prevY = y;
            }
        }
    }


    final class LinkCollection implements ObjectCollection<Line> {
        private HashMap<LinkVisual, LinkVisual> links_;

        /*
         ** Constructors.
         */
        public LinkCollection() {
            links_ = new HashMap<LinkVisual, LinkVisual>();
        }

        /*
         ** Public methods.
         */
        public void Add(Line line) {
            LinkVisual link = (LinkVisual)line.Value();
            links_.put(link, link);
        }
        
        public boolean Valid(Line line) { 
            return true; 
        }
        
        public void Remove(Line value) {}
        
        public void Clear() { 
            links_.clear(); 
        }
        
        public int Count() { 
            return links_.size(); 
        }
        
        public Collection<Line> Objects() { 
            return null; 
        }
        
        public Iterator<LinkVisual> Links() { 
            return links_.keySet().iterator(); 
        }
    }

    /*
    * Constante
    */
    private final double NODE_SIZE = 20;
    private final double ARROW_SIZE = 10;
    private final double ARROW_ANGLE = Math.toRadians(135);
    private final double LINK_SIZE = 2;
    private final double SELECTED_LINK_SIZE = 3;
    private final Color NODE_COLOR = new Color(45, 190, 255, 192);     // Blue.
    private final Color SELECTED_NODE_COLOR = new Color(255, 43, 104); // Magenta.
    private final Color LINK_COLOR = new Color(0, 0, 0);               // Black.
    private final Color NODE_LINK_COLOR = new Color(255, 43, 104);     // Blue.
    private final Color SELECTED_LINK_COLOR = new Color(125, 189, 0);  // Green.

    /*
    * Private members.
    */
    private IMapProvider provider_;
    private IRendererHost host_;
    private LineEditor lineEditor_;
    private IProjection projection_;
    Region2D maxBounds_;
    private boolean visible_;
    private int zoomLevels_;
    private double opacity_;

    PointTree<NodeVisual> nodes_;
    BasicCollection<NodeVisual> visibleNodes_;
    HashMap<ObjectId, NodeVisual> nodeMap;
    private LineTree linkLines_;

    private Stroke linkStroke_;
    private Stroke selectedLinkStroke_;
    private Stroke nodeStroke_;

    private NodeVisual selectedNode_;
    private LinkVisual selectedLink_;
    private LinkCollection visibleLinks_;
    private ArrayList<LinkVisual> frontLinks_;
    
    private boolean mouseCaptured_;
    private boolean showLinks_;
    private Point dragStart_;
    private Point startPosition_;
    private LinkVisual fakeLink_;   // Used when adding new links.
    private NodeVisual targetNode_;

    /*
    * Constructori
    */
    public NodeEditor(LineEditor lineEditor, IRendererHost host) {
        host_ = host;
        lineEditor_ = lineEditor;
        provider_ = host.MapProvider();
        projection_ = provider_.Projection();
        zoomLevels_ = provider_.ZoomLevels();
        opacity_ = 1.0;
        visible_ = true;
        showLinks_ = true;

        // Initialize all used data structures.
        visibleNodes_ = new BasicCollection<NodeVisual>();
        maxBounds_ = provider_.MapBounds(zoomLevels_ - 1);
        nodes_ = new PointTree<NodeVisual>(maxBounds_.Width(), maxBounds_.Height());
        nodeMap = new HashMap<ObjectId, NodeVisual>();
        linkLines_ = new LineTree(maxBounds_.Width(), maxBounds_.Height(), zoomLevels_);
        linkStroke_ = new BasicStroke((int)LINK_SIZE);
        selectedLinkStroke_ = new BasicStroke((int)SELECTED_LINK_SIZE);
        nodeStroke_ = new BasicStroke(1);
        visibleLinks_ = new  LinkCollection();
        frontLinks_ = new ArrayList<LinkVisual>();

        LoadNodes();
    }

    /*
    ** Public methods.
    */
    public boolean IsEditor() { 
        return true; 
    }
    
    public ILayer Layer() { 
        return lineEditor_.Layer(); 
    }
    
    public boolean HasPrefetcher() { 
        return false; 
    }
    
    public IPrefetcher Prefetcher() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public int ZIndex() { 
        return Integer.MAX_VALUE - 2; // Below the overlay.
    }
    
    public void SetZIndex(int value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean MouseDown(Point point, View view, Modifier modifier) {
        if(!visible_ == false) {
            return false;
        }
        
        // Search for the node below the specified position.
        point = AdjustPoint(point, view);
        NodeVisual nodeVis = HitTestNode(point, view);
        
        if(nodeVis != null) {
            // If the action has been performed using a right-click
            // a new link is added to this node; otherwise the node is moved.
            if(modifier.IsSet(Modifier.BUTTON_RIGHT)) {
                fakeLink_ = CreateFakeLink(nodeVis, point);
                selectedLink_ = fakeLink_;
            }
            else {
                selectedLink_ = null;
            }

            selectedNode_ = nodeVis;
            mouseCaptured_ = true;
            dragStart_ = point;
            startPosition_ = selectedNode_.Position();

            // Notify the parent and redraw.
            host_.SendAction(Action.ObjectSelected(HitTest(point, view)));
            host_.Repaint();
            return true;
        }
        else {
            // A new node is added if the Shift key is pressed.
            if(modifier.IsSet(Modifier.KEY_SHIFT)) {
                selectedNode_ = AddNode(point);
                
                if(selectedNode_ != null) {
                    mouseCaptured_ = true;
                    dragStart_ = point;
                    startPosition_ = selectedNode_.Position();
                }

                // Notify the parent and redraw.
                host_.SendAction(Action.ObjectSelected(HitTest(point, view)));
                host_.Repaint();
                return true;
            }
            else {
                selectedNode_ = null;
            }            
        }

        // Search for a line if a node has not been found.
        LinkVisual linkVis = HitTestLink(point, view);
        
        if(linkVis != null) {
            selectedLink_ = linkVis;
            selectedNode_ = linkVis.ParentNode();
            
            // Notify the parent and redraw.
            host_.SendAction(Action.ObjectSelected(HitTest(point, view)));
            host_.Repaint();
            return true;
        }
        else {
            // Deselect the previous selected link.s
            selectedLink_ = null;
            host_.Repaint();
        }

        return false;
    }

    public boolean MouseUp(Point point, View view, Modifier modifier) {
        if(!visible_ == false) {
            return false;
        }
        
        if(fakeLink_ != null) {
            // The new link must be added; get the point below the cursor.
            point = AdjustPoint(point, view);
            NodeVisual nodeVis = HitTestNode(point, view);
            selectedLink_ = AddLink(fakeLink_, nodeVis);
            targetNode_ = null;
            fakeLink_ = null;
            host_.Repaint();
        }

        mouseCaptured_ = false;
        return (selectedLink_ != null) || (selectedNode_ != null);
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
        if(!visible_ == false) {
            return false;
        }
        
        point = AdjustPoint(point, view);

        if(fakeLink_ != null) {
            // Find the nearest node. If the distance is small enough
            // consider the node position to be equal to the mouse position.
            NodeVisual nearestNode = HitTestNode(point, view);
            
            if((nearestNode != null) &&
               (nearestNode.Position().Distance(point) < (NODE_SIZE / 2))) {
                point = nearestNode.Position();
                targetNode_ = nearestNode;
            }
            else {
                targetNode_ = null;
            }

            MoveFakeLink(fakeLink_, point, startPosition_);
        }
        else if(selectedNode_ != null) {
            MoveNode(selectedNode_, point, startPosition_);
        }
 
        host_.Repaint();
        return true;
    }

    public IObjectInfo HitTest(Point point, View view) {
        NodeVisual nodeVis = HitTestNode(point, view);
        if(nodeVis != null) {
            return new NodeInfo(nodeVis.Node(), this, nodeVis.Position(),
                                       ToCoordinates(nodeVis.Position()));
        }

        LinkVisual linkVis = HitTestLink(point, view);
        
        if(linkVis != null) {
            return new LinkInfo(linkVis.Link(), linkVis.ParentNode().Node(), this);
        }

        return null;
    }

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

    public boolean LinksVisible() { 
        return showLinks_; 
    }
    
    public void SetLinksVisible(boolean value) { 
        showLinks_ = value; 
    }

    public void RemoveNode(Node node) {
        assert(node != null);
        // ------------------------------------------------
        // Remove the node and all links connected to it.
        NodeVisual nodeVis = nodeMap.get(node.Id());
        nodes_.Remove(nodeVis);
        RemoveLinkLines(nodeVis.InLinks());
        RemoveLinkLines(nodeVis.OutLinks());

        // The links from the "Out" list must be removed
        // from the "In" lists of the next nodes, and the ones from
        // the "In" list must be remove from the "Out" lists.
        RemoveInLinks(nodeVis.OutLinks());
        RemoveOutLinks(nodeVis.InLinks());
        host_.Repaint();
    }

    public void RemoveLink(Link link, Node parentNode) {
        assert(link != null);
        assert(parentNode != null);
        // ------------------------------------------------
        // Remove the link from the nodes to which it is connected.
        NodeVisual nodeVis = nodeMap.get(parentNode.Id());

        List<LinkVisual> links = nodeVis.OutLinks();
        int count = links.size();
        
        for(int i = 0; i < count; i++) {
            LinkVisual linkVis = links.get(i);
            
            if(linkVis.Link().equals(link)) {
                // The link to be removed has been found. It must be removed
                // from the "Out" list of the current node and from
                // the "In" list of the next node.
                linkVis.RemoveLines();
                nodeVis.OutLinks().remove(linkVis);
                linkVis.NextNode().InLinks().remove(linkVis);
                host_.Repaint();
                return;
            }
        }
    }

    public NodeVisual GetNodeVisual(ObjectId nodeId) {
        assert(nodeId != null);
        // ------------------------------------------------
        return nodeMap.get(nodeId);
    }

    public IObjectInfo NearestNode(Point point) {
        PointTree<NodeVisual>.NearestInfo nearest = nodes_.NearestPoint(point);
        if((nearest != null) && (nearest.Value() != null)) {
            NodeVisual nodeVis = nearest.Value();
            return new NodeInfo(nodeVis.Node(), this, nodeVis.Position(), 
                                ToCoordinates(nodeVis.Position()));
        }

        return null;
    }

    public void Render(View view) {
        // Don't draw if the view is not at the maximum valid zoom level.
        if((int)view.Zoom() != (zoomLevels_ - 1)) {
            return;
        }

        // Activate antialiasing.
        VolatileImage buffer = view.GetBuffer(this);
        Graphics2D g = buffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                           RenderingHints.VALUE_STROKE_PURE);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                  (float)opacity_));

        // Find the points visible in the current view.
        Region2D bounds = view.Bounds();
        double inflateVal = 2 * NODE_SIZE;

        visibleNodes_.Clear();
        visibleLinks_.Clear();
        bounds.Inflate(inflateVal, inflateVal);
        nodes_.Intersect(bounds, visibleNodes_);
        linkLines_.Intersect(bounds, zoomLevels_ - 1, visibleLinks_);
        bounds.Inflate(-inflateVal, -inflateVal);

        Draw(visibleNodes_, visibleLinks_, g, view);
        g.dispose();
    }

    /*
    ** Private methods.
    */
    private void LoadNodes() {
        // Load all nodes defined in the street layer.
        // First all nodes are added, then the associated link information.
        Iterator<Node> nodeIt = provider_.GetNodeIterator();
        
        while(nodeIt.hasNext()) {
            Node node = nodeIt.next();
            NodeVisual nodeVis = new NodeVisual(node, FromCoordinates(node.Coordinates()));
            nodes_.Add(nodeVis);
            nodeMap.put(node.Id(), nodeVis);
        }

        nodeIt = provider_.GetNodeIterator();
        
        while(nodeIt.hasNext()) {
            Node node = nodeIt.next();
            NodeVisual nodeVis = nodeMap.get(node.Id());

            Iterator<Link> linkIt = node.Links().values().iterator();
            while(linkIt.hasNext()) {
                Link link = linkIt.next();
                LinkVisual linkVis = new LinkVisual(link, nodeVis,
                                            nodeMap.get(link.NodeId()));

                // Associate the link with the node.
                nodeVis.OutLinks().add(linkVis);
                linkVis.NextNode().InLinks().add(linkVis);
                linkVis.AddLines();
            }
        }
    }

    private void Draw(BasicCollection<NodeVisual> nodes, LinkCollection links,
                      Graphics2D g, View view) {
        // Draw all nodes and associated links from the list.
        if(nodes.Count() == 0) {
            return;
        }

        // First draw the nodes, then the links if enabled.
        Iterator<NodeVisual> nodeIt = nodes.Objects().iterator();
        while(nodeIt.hasNext()) {
            NodeVisual nodeVis = nodeIt.next();
            nodeVis.Draw(g, view);
        }
        
        if(!showLinks_) {
            return;
        }
        
        // "frontLinks_" stores the links that should be drawn before all
        // other lists becuase they belong to the selected node.
        frontLinks_.clear(); 
        Iterator<LinkVisual> linkIt = links.Links();
        
        while(linkIt.hasNext()) {
            LinkVisual linkVis = linkIt.next();
            if((linkVis.ParentNode() == selectedNode_) ||
               (linkVis.NextNode() == selectedNode_)) {
                frontLinks_.add(linkVis);
            }
            else {
                linkVis.Draw(g, view);
            }
        }

        // Now draw the links associated with the selected node.
        int count = frontLinks_.size();
        
        for(int i = 0; i < count; i++) {
            frontLinks_.get(i).Draw(g, view);
        }

        // Draw the link which is currently being added.
        if(fakeLink_ != null) {
            fakeLink_.Draw(g, view);
        }
    }

    private NodeVisual HitTestNode(Point point, View view) {
        // Search for the node found at the specified position.
        PointTree<NodeVisual>.NearestInfo nearest = nodes_.NearestPoint(point);
        
        if(nearest == null) {
            return null;
        }
        else if(nearest.Distance() < NODE_SIZE) {
            return nearest.Value();
        }

        return null;
    }

    private LinkVisual HitTestLink(Point point, View view) {
        // Search for the link found at the specified position.
        LineTree.NearestInfo nearest = linkLines_.NearestLine(point, zoomLevels_ - 1);
        
        if(nearest == null) {
            return null;
        }
        else if(nearest.Distance() < (LINK_SIZE * 4)) {
            return (LinkVisual)nearest.Line().Value();
        }

        return null;
    }

    private void AddLinkLines(List<LinkVisual> links) {
        int count = links.size();
        
        for(int i = 0; i < count; i++) {
            links.get(i).AddLines();
        }
    }

    private void RemoveLinkLines(List<LinkVisual> links) {
        int count = links.size();
        
        for(int i = 0; i < count; i++) {
            links.get(i).RemoveLines();
        }
    }

    private void UpdateLinkLines(List<LinkVisual> links) {
        RemoveLinkLines(links);
        AddLinkLines(links);
    }

    private void RemoveInLinks(List<LinkVisual> links) {
        for(LinkVisual linkVis : links) {
            linkVis.NextNode().InLinks().remove(linkVis);
        }
    }

    private void RemoveOutLinks(List<LinkVisual> links) {
        for(LinkVisual linkVis : links) {
            linkVis.ParentNode().OutLinks().remove(linkVis);
        }
    }

    private boolean MoveNode(NodeVisual nodeVis, Point mousePoint, Point start) {
        // Moves the specified node based on the start position
        // and the current position of the mouse cursor.
        double dx = mousePoint.X() - dragStart_.X();
        double dy = mousePoint.Y() - dragStart_.Y();
        Point newPos = new Point(start.X() + dx, start.Y() + dy);

        NodeInfo info = new NodeInfo(nodeVis.Node(), this, newPos,
                                     ToCoordinates(newPos));
        if(host_.SendAction(Action.ObjectMoved(info)).Valid()) {
            // The node can be moved, do it.
            nodes_.Remove(nodeVis);
            nodeVis.SetPosition(newPos);
            nodes_.Add(nodeVis);

            // All links connected to this node must be updated.
            UpdateLinkLines(nodeVis.InLinks());
            UpdateLinkLines(nodeVis.OutLinks());
            return true;
        }

        return false;
    }

    private NodeVisual AddNode(Point point) {
        // Add a new node at the specified position.
        Node node = new Node(ObjectId.NewId(), ToCoordinates(point));
        NodeInfo info = new NodeInfo(node, this, point, node.Coordinates());

        if(host_.SendAction(Action.ObjectAdded(info)).Valid()) {
            NodeVisual nodeVis = new NodeVisual(node, point);
            nodes_.Add(nodeVis);
            nodeMap.put(node.Id(), nodeVis);
            return nodeVis;
        }

        return null;
    }

    private LinkVisual CreateFakeLink(NodeVisual nodeVis, Point point) {
        // When adding a link a new fake link is created; if the mouse cursor
        // is above a node when releasing the mouse the fake link
        // is converted to a real link and added to the layer.
        NodeVisual fakeVis = new NodeVisual(nodeVis.Node(), point);
        LinkVisual linkVis = new LinkVisual(null, nodeVis, fakeVis);
        linkVis.AddLines();
        return linkVis;
    }

    private void MoveFakeLink(LinkVisual linkVis, Point mousePoint, Point start) {
        // Move the link based on its start position and the current
        // position of the mouse cursor.
        double dx = mousePoint.X() - dragStart_.X();
        double dy = mousePoint.Y() - dragStart_.Y();
        Point newPos = new Point(start.X() + dx, start.Y() + dy);

        // Muta nodul fals si actualizeaza linia.
        linkVis.NextNode().SetPosition(newPos);
        linkVis.RemoveLines();
        linkVis.AddLines();
    }

    private LinkVisual AddLink(LinkVisual linkVis, NodeVisual nextNodeVis) {
        // Adds a new link connected to the specified node.
        if(nextNodeVis == null) {
            // No node has been selected, the link will be removed.
            return null;
        }

        // Don't add a link if there exists one between the two nodes.
        for(LinkVisual nodeVis : linkVis.ParentNode().OutLinks()) {
            if(nodeVis.NextNode().Node().equals(nextNodeVis.Node())) {
                linkVis.RemoveLines();
                return null;
            }
        }

        // Get the lists with the points found near the points.
        // Search for two points belonging to the same street.
        BasicCollection<LineEditor.PointVisual> parentPoints =
                lineEditor_.NearPoints(linkVis.ParentNode().Position(), NODE_SIZE);
        BasicCollection<LineEditor.PointVisual> nextPoints =
                lineEditor_.NearPoints(nextNodeVis.Position(), NODE_SIZE);
        if((parentPoints.Count() == 0) || (nextPoints.Count() == 0)) {
            
            linkVis.RemoveLines();
            return null;
        }

        List<LineEditor.PointVisual> parentList = parentPoints.ObjectList();
        List<LineEditor.PointVisual> nextList = nextPoints.ObjectList();
        int parentCount = parentList.size();
        int nextCount = nextList.size();
        LineEditor.PointVisual parentPoint = null;
        LineEditor.PointVisual nextPoint = null;

        for(int i = 0; i < parentCount; i++) {
            for(int j = 0; j < nextCount; j++) {
                // Check if the two points are on the same street.
                LineEditor.PointVisual point1 = parentList.get(i);
                LineEditor.PointVisual point2 = nextList.get(j);

                if(point1.Street() == point2.Street()) {
                    parentPoint = point1;
                    nextPoint = point2;
                    break;
                }
            }

            if(parentPoint != null) {
                break;
            }
        }

        if(parentPoint != null) {
            Link link = new Link(ObjectId.NewId(), nextNodeVis.Node().Id(),
                                 nextPoint.Street().Id(), parentPoint.Index(),
                                 nextPoint.Index(), 0 /* distanta */);
            LinkInfo info = new LinkInfo(link, linkVis.ParentNode().Node(), this);
            
            if(host_.SendAction(Action.ObjectAdded(info)).Valid()) {
                // The link can be added, do it.
                linkVis.SetLink(link);
                linkVis.SetNextNode(nextNodeVis);
                linkVis.RemoveLines();
                linkVis.AddLines();
                linkVis.ParentNode().OutLinks().add(linkVis);
                nextNodeVis.InLinks().add(linkVis);
                return linkVis;
            }
        }

        linkVis.RemoveLines();
        return null;
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
