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

package Rendering.Utils;
import Core.*;
import java.util.*;
import java.util.ArrayList;

public final class LineTree {
    // The index in the child array based on the direction.
    private static final int DIRECTION_NW = 0;
    private static final int DIRECTION_NE = 1;
    private static final int DIRECTION_SE = 2;
    private static final int DIRECTION_SW = 3;

    private static final double[] DirectionX = new double[] {
        -0.25, 0.25, 0.25, -0.25
    };
    private static final double[] DirectionY = new double[] {
        -0.25, -0.25, 0.25, 0.25
    };

    // The capacity of a leaf node. If it is exceeded,
    // the node is split in 4 child nodes and the lines
    // are distributed among them.
    private static int SPLIT_THRESHOLD = 4;

    
    public class NearestInfo {
        private Line line_;
        private double distance_;

        // ------------------------------------------------
        public NearestInfo() {
            distance_ = Double.MAX_VALUE;
        }

        public NearestInfo(Line line, double distance) {
            line_ = line;
            distance_ = distance;
        }

        // ------------------------------------------------
        public Line Line() { 
            return line_; 
        }
        
        public void SetLine(Line value) { 
            line_ = value; 
        }
        
        public double Distance() { 
            return distance_; 
        }
        
        public void SetDistance(double value) { 
            distance_ = value; 
        }
    }


    class Node {
        private double x_;
        private double y_;
        private double width_;
        private double height_;
        private ArrayList<Line> lines_;
        private Object[] children_;
        boolean leaf_;

        // ------------------------------------------------
        public Node(double x, double y, double width, double height, 
                    boolean leaf) {
            x_ = x;
            y_ = y;
            width_ = width;
            height_ = height;
            leaf_ = leaf;

            if(leaf) {
                lines_ = new ArrayList<Line>(2);
            }
            else {
                children_ = new Object[4];
            }
        }

        public Node(Point center, double width, double height, 
                    boolean leaf) {
            this(center.X(), center.Y(), width, height, leaf);
        }

        // ------------------------------------------------
        public double X()      { return x_; }
        public double Y()      { return y_; }
        public double Width()  { return width_; }
        public double Height() { return height_; }
        
        public double Left()   { return x_ - (width_ / 2); }
        public double Top()    { return y_ - (height_ / 2); }
        public double Right()  { return x_ + (width_ / 2); }
        public double Bottom() { return y_ + (height_ / 2); }
        
        public List<Line> Lines() { 
            return lines_; 
        }
        
        public boolean IsLeaf()   { 
            return leaf_; 
        }
        
        public void MakeInternal() {
            assert(leaf_ == true);
            // ------------------------------------------------
            lines_ = null;
            children_ = new Object[4];
            leaf_ = false;
        }

        public void MakeLeaf(ArrayList<Line> list) {
            lines_ = list;
            children_ = null;
            leaf_ = true;
        }

        public Node Child(int direction) {
            assert((direction >= DIRECTION_NW) &&
                   (direction <= DIRECTION_SW));
            // ------------------------------------------------
            return (Node)children_[direction];
        }

        public void SetChild(Node child, int direction) {
            assert((direction >= DIRECTION_NW) &&
                   (direction <= DIRECTION_SW));
            // ------------------------------------------------
            children_[direction] = child;
        }

        @Override
        public String toString() {
            // N, 10, 25 oXoX
            return (leaf_ ? "L: " : "N: ") + x_ + ", " + y_ + " " +
                   (leaf_ ? lines_.size() : (children_[0] != null ? "X" : "o") +
                                            (children_[1] != null ? "X" : "o") +
                                            (children_[2] != null ? "X" : "o") +
                                            (children_[3] != null ? "X" : "o"));
        }
    }

    /*
     ** Members.
     */
     private Object[] root_;
     private double width_;
     private double height_;
     private int zoomLevels_;

    /*
     ** Constructors.
     */
     public LineTree(double width, double height, int zoomLevels) {
         zoomLevels_ = zoomLevels;
         width_ = width;
         height_ = height;
         root_ = new Object[zoomLevels];
         
         for(int i = 0; i < zoomLevels; i++) {
            root_[i] = new Node(width / 2, height / 2, width, height, true);
         }
     }

    /*
     ** Public methods.
     */
     public void Add(Line line, int zoomLevel) {
         assert(line != null);
         assert(zoomLevel >= 0 && zoomLevel <= zoomLevels_);
         // ------------------------------------------------
         Node root = GetRoot(zoomLevel);
         AddImpl(line, root);
     }

     public void AddAll(Collection<Line> lines, int zoomLevel) {
        assert(lines != null);
        // ------------------------------------------------
        Iterator<Line> lineIt = lines.iterator();

        while(lineIt.hasNext()) {
            Add(lineIt.next(), zoomLevel);
        }
    }

     public void Remove(Line line, int zoomLevel) {
         assert(line != null);
         assert(zoomLevel >= 0 && zoomLevel <= zoomLevels_);
         // ------------------------------------------------
         Node root = GetRoot(zoomLevel);
         RemoveImpl(line, root);
     }

     public Line FindLine(Line line, int zoomLevel) {
         assert(line != null);
         assert(zoomLevel >= 0 && zoomLevel <= zoomLevels_);
         // ------------------------------------------------
         // Find the line at the specified zoom level.
         Node root = GetRoot(zoomLevel);
         return FindLineImpl(line, root);
     }

    public void Intersect(Region2D region, int zoomLevel, 
                          ObjectCollection<Line> list) {
        assert(region != null);
        assert(list != null);
        assert(GetRoot(zoomLevel) != null);
        // ------------------------------------------------
        // Add to the list all lines found in the specified region.
        // A line is considered in the region if a segment is in the region.
        if(region.IsEmpty()) {
            return;
        }

        IntersectImpl(region, GetRoot(zoomLevel), list);
    }

    public NearestInfo NearestLine(Point point, int zoomLevel) {
        assert(point != null);
        assert(zoomLevel >= 0 && zoomLevel < zoomLevels_);
        assert(GetRoot(zoomLevel) != null);
        // ------------------------------------------------        
        // Find the nearest line to the specified point.
        NearestInfo nearest = new NearestInfo();
        NearestLineImpl(point, GetRoot(zoomLevel), nearest);
        nearest.SetDistance(Math.sqrt(nearest.Distance()));
        return nearest;
    }

    public double Width() { 
        return width_; 
    }
    
    public double Height() { 
        return height_; 
    }

    public void Clear() {
        root_ = null;
    }

    /*
     ** Private methods.
     */
     private Node GetRoot(int zoomLevel) {
         return (Node)root_[zoomLevel];
     }

     private void SplitNode(Node node, List<Line> lines) {
        // Converts a leaf node to an internal node by creating
        // 4 children and redistributing the lines to them.
        node.MakeInternal();
        int lineCount = lines.size();
        
        for(int dir = DIRECTION_NW; dir <= DIRECTION_SW; dir++) {
            Node child = new Node(node.X() + (node.Width() * DirectionX[dir]), 
                                  node.Y() + (node.Height() * DirectionY[dir]),
                                  node.Width() / 2, node.Height() / 2, true);
            node.SetChild(child, dir);

            for(int i = 0; i < lineCount; i++) {
                AddImpl(lines.get(i), child);
            }
        }
    }

     private void AddImpl(Line line, Node node) {
         // Check if the line intersect with the nodes region.
         // If not it is guaranteed that it doesn't intersect
         // with the children too and nothing must be done.
         if(!LineIntersectsNode(line, node)) {
             return;
         }

         if(!node.IsLeaf()) {
             // The node is not a leaf, try to add the line
             // to all 4 children. Note that a line, compared to a point,
             // can appear in more than one child at the same time.
             for(int dir = DIRECTION_NW; dir <= DIRECTION_SW; dir++) {
                AddImpl(line, node.Child(dir));
             }
         }
         else {
             // Add the line to the node and redistribute the lines
             // if the maximum capacity is exceeded.
             List<Line> lines = node.Lines();
             lines.add(line);

             if(lines.size() > SPLIT_THRESHOLD) {
                SplitNode(node, lines);
             }
         }
     }

     private void RemoveImpl(Line line, Node node) {
         // Verifica daca linia se intersecteaza cu nodul.
         if(!LineIntersectsNode(line, node)) {
             return;
         }

         if(node.IsLeaf()) {
             // The node is a leaft, check if the line is found here.
             List<Line> lines = node.Lines();
             int count = lines.size();
             
             for(int i = 0; i < count; i++) {
                 if(lines.get(i).equals(line)) {
                     lines.remove(i);
                     return;
                 }
             }
         }
         else {
             // Remove the line from each of the 4 children.
             for(int dir = DIRECTION_NW; dir <= DIRECTION_SW; dir++) {
                 RemoveImpl(line, node.Child(dir));
             }

             // Combine the child nodes into a single one
             // if they are mostly empty (reduces memory demand).
             if(MergePossible(node)) {
                 ArrayList<Line> list = new ArrayList<Line>();
                 
                 if(MergeNodes(node, list)) {
                     node.MakeLeaf(list);
                 }
             }
         }
     }

     private boolean MergePossible(Node node) {
         return node.Child(DIRECTION_NE).IsLeaf() ||
                node.Child(DIRECTION_NW).IsLeaf() ||
                node.Child(DIRECTION_SE).IsLeaf() ||
                node.Child(DIRECTION_SW).IsLeaf();
     }

    private boolean MergeNodes(Node node, List<Line> list) {
        if(node.IsLeaf()) {
            // Copy the lines to the input list.
            List<Line> nodeLines = node.Lines();
            int count = nodeLines.size();

            for(int i = 0; i < count; i++) {
                Line line = nodeLines.get(i);
                
                if(!list.contains(line)) {
                    list.add(line);
                }
            }

            return list.size() < SPLIT_THRESHOLD;
        }
        else {
            // Copy the lines from each child to the input list.
            for(int dir = DIRECTION_NW; dir <= DIRECTION_SW; dir++) {
                if(!MergeNodes(node.Child(dir), list)) {
                    return false;
                }
            }

            return true;
        }
    }

    private Line FindLineImpl(Line line, Node node) {
        // If the line is outside the nodes range
        // no search needs to be done at all.
         if(!LineIntersectsNode(line, node)) {
             return null;
         }

         if(node.IsLeaf()) {
             List<Line> lines = node.Lines();
             int count = lines.size();
             
             for(int i = 0; i < count; i++) {
                 if(lines.get(i).equals(line)) {
                     return lines.get(i);
                 }
             }
         }
         else {
             // Search in each of the child nodes.
             for(int dir = DIRECTION_NW; dir <= DIRECTION_SW; dir++) {
                 Line found = FindLineImpl(line, node.Child(dir));
                 
                 if(found != null) {
                     return found;
                 }
             }
         }

         return null;
     }

    private void IntersectImpl(Region2D region, Node node,
                               ObjectCollection<Line> list) {
        if(node.IsLeaf()) {
            // Check which of the nodes lines
            // intersect with the specified region.
            // cu regiunea specificata.
            List<Line> lines = node.Lines();
            int lineCount = lines.size();

            for(int i = 0; i < lineCount; i++) {
                Line candidate = lines.get(i);
                if(LineUtils.LineIntersectsRect(candidate, region.Left(), region.Top(),
                                                region.Right(), region.Bottom())) {
                    // Found an intersecting line.
                    if(list.Valid(candidate)) {
                        list.Add(candidate);
                    }
                }
            }
        }
        else {
            // Run the query only on the children
            // that intersect with the specified region.
            for(int dir = DIRECTION_NW; dir <= DIRECTION_SW; dir++) {
                if(region.IntersectsWith(node.Left(), node.Top(),
                                         node.Right(), node.Bottom())) {
                    IntersectImpl(region, node.Child(dir), list);
                }
            }
        }
    }

    private void NearestLineImpl(Point point, Node node, NearestInfo nearest) {
        if(node.IsLeaf()) {
            // Search for the nearest line to specified point.
            List<Line> lines = node.Lines();
            int lineCount = lines.size();

            for(int i = 0; i < lineCount; i++) {
                Line candidate = lines.get(i);
                double dist = LineUtils.PointLineDistanceSq(point, candidate);
                
                if(dist < nearest.Distance()) {
                    // Found a closer line.
                    nearest.SetLine(candidate);
                    nearest.SetDistance(dist);
                }
            }
        }
        else {
            // Run the query only on the children
            // that intersect with the specified region.
            double distance = Math.sqrt(nearest.Distance());

            for(int dir = DIRECTION_NW; dir <= DIRECTION_SW; dir++) {
                if(Overlap(point.X(), point.Y(), distance,
                           node.X(), node.Y(), node.Width(), node.Height())) {
                    NearestLineImpl(point, node.Child(dir), nearest);
                }
            }
        }
    }

    private boolean LineIntersectsNode(Line line, Node node) {
        return LineUtils.LineIntersectsRect(line, node.Left(), node.Top(),
                                  node.Right(), node.Bottom());
    }

    private boolean Overlap(double circleX, double circleY, double radius,
                            double rectX, double rectY,
                            double rectWidth, double rectHeight) {
        double dx = circleX - rectX;
        double dy = circleY - rectY;

        // Circle outside the rectangle case.
        if((dx > ((rectWidth / 2) + radius)) ||
           (dy > ((rectHeight / 2) + radius))) {
            return false;
        }

        // Circle inside the rectangle case.
        if((dx <= (rectWidth / 2)) ||
           (dy <= (rectHeight / 2))) {
            return true;
        }

        // Circle intersection with the corners of the rectangle.
        double  distanceSq = (dx - (rectWidth / 2)) *
                             (dx - (rectWidth / 2)) +
                             (dy - (rectHeight / 2)) *
                             (dy - (rectHeight / 2));
        return distanceSq <= (radius * radius);
    }
}
