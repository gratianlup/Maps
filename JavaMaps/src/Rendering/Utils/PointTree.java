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
//
// Implements a Bucket PR-Quadtree which allows efficient
// finding and insertion of point-like visual objects.
package Rendering.Utils;
import Core.*;
import Rendering.IVisual;
import java.util.*;

public final class PointTree<T extends IVisual> {
    // The index in the child array based on the direction.
    private static final int DIRECTION_NW = 0;
    private static final int DIRECTION_NE = 1;
    private static final int DIRECTION_SW = 2;
    private static final int DIRECTION_SE = 3;

    private static final double[] DirectionX = new double[] {
        -0.5, 0.5, -0.5, 0.5
    };
    private static final double[] DirectionY = new double[] {
        -0.5, -0.5, 0.5, 0.5
    };
    private static final double[] IntersectionX = new double[] {
        -1, 0, -1, 0
    };
    private static final double[] IntersectionY = new double[] {
        -1, -1, 0, 0
    };

    // The maximum capacity of a leaf node.
    private static int SPLIT_THRESHOLD = 8;

    public class NearestInfo {
        private T value_;
        private double distance_;

        // ------------------------------------------------
        public NearestInfo() {
            distance_ = Double.MAX_VALUE;
        }

        public NearestInfo(T value, double distance) {
            value_ = value;
            distance_ = distance;
        }

        // ------------------------------------------------
        public double X() { 
            return value_.Position().X(); 
        }
        
        public double Y() { 
            return value_.Position().Y(); 
        }

        public T Value() { 
            return value_; 
        }
        
        public void SetValue(T value) { 
            value_ = value; 
        }
        
        public double Distance() { 
            return distance_; 
        }
        
        public void SetDistance(double value) { 
            distance_ = value; 
        }
    }

    
    class Node {
        private Object[] children_;
        private ArrayList<T> points_;
        private double x_;
        private double y_;
        private boolean leaf_;

        // ------------------------------------------------
        public Node(double x, double y, boolean leaf) {
            x_ = x;
            y_ = y;
            leaf_ = leaf;

            if(leaf) {
                points_ = new ArrayList<T>(SPLIT_THRESHOLD);
            }
            else {
                children_ = new Object[4];
            }
        }

        public Node(Point center, boolean leaf) {
            this(center.X(), center.Y(), leaf);
        }

        // ------------------------------------------------
        public Node Child(int direction) {
            assert((direction >= DIRECTION_NW) &&
                   (direction <= DIRECTION_SE));
            // ------------------------------------------------
            return (Node)children_[direction];
        }

        public void SetChild(Node child, int direction) {
            assert((direction >= DIRECTION_NW) &&
                   (direction <= DIRECTION_SE));
            // ------------------------------------------------
            children_[direction] = child;
        }

        public List<T> Points() { 
            return points_; 
        }
        
        public double X() { 
            return x_; 
        }
        
        public double Y() { 
            return y_; 
        }
        
        public boolean IsLeaf() { 
            return leaf_; 
        }

        public void MakeInternal() {
            assert(leaf_ == true);
            // ------------------------------------------------
            points_ = null;
            children_ = new Object[4];
            leaf_ = false;
        }

        public void MakeLeaf(ArrayList<T> list) {
            points_ = list;
            children_ = null;
            leaf_ = true;
        }

        @Override
        public String toString() {
            // N, 10, 25 oXoX
            return (leaf_ ? "L:" + points_.size() + "; " : "N: ") + x_ + ", " + y_ + " " +
                   (leaf_ ? "" : (children_[0] != null ? "X" : "o") +
                                 (children_[1] != null ? "X" : "o") +
                                 (children_[2] != null ? "X" : "o") +
                                 (children_[3] != null ? "X" : "o"));
        }
    }

    /*
     ** Members.
     */
    private Node root_;
    private int count_;
    private double width_;  // Maximum width of the covered surface.
    private double height_; // Maximum height of the covered surface.

    /*
     ** Constructors.
     */
    public PointTree(double width, double height) {
        root_ = new Node(width / 2, height / 2, true);
        count_ = 0;
        width_ = width;
        height_ = height;
    }
    
    public PointTree(double width, double height,
                     Collection<T> values) {
        this(width, height);
        AddAll(values);
    }

    /*
     ** Public methods.
     */
    public void Add(T value) {
        assert(value != null);
        // ------------------------------------------------
        // Insert the specified point into the tree.
        // If the point already exists it is not modified again.
        Point point = value.Position();
        double x = width_ / 2;
        double y = height_ / 2;
        double width = width_ / 2;
        double height = height_ / 2;
        Node node = root_;
        
        // Find the leaf node where the point should be inserted.
        while(!node.IsLeaf()) {
            int direction = Direction(point, node);
            x += width * DirectionX[direction];
            width /= 2;
            
            y += height * DirectionY[direction];
            height /= 2;

            node = node.Child(direction);
        }
        
        // Add the point to the leaf node. If the maximum capacity
        // is exceeded the leaf is split into 4 children, each having
        // some of the points (a point can't appear in two chidren).
        List<T> list = node.Points();
        list.add(value);
        count_++;

        if(list.size() > SPLIT_THRESHOLD) {
            SplitNode(node, x, width, y, height, list);
        }
    }
    
    private void SplitNode(Node node, double x, double width, 
                           double y, double height, List<T> list) {
        // Make the leaf node an internal node and create 4 children,
        // each having some of the points from the leaf node.
        node.MakeInternal();
        
        for(int dir = DIRECTION_NW; dir <= DIRECTION_SE; dir++) {
            double childX = x + (width * DirectionX[dir]);
            double childY = y + (height * DirectionY[dir]);
            Node child = new Node(childX, childY, true /* leaf */);
            node.SetChild(child, dir);
            int count = list.size();
            
            for(int i = 0; i < count; i++) {
                T temp = list.get(i);
                
                if(PointInRegion(temp.Position(), childX, childY, width, height)) {
                    child.Points().add(temp);
                }
            }
        }
    }

    public void AddAll(Collection<T> values) {
        assert(values != null);
        // ------------------------------------------------
        Iterator<T> valuesIt = values.iterator();
        
        while(valuesIt.hasNext()) {
            Add(valuesIt.next());
        }
    }

    public void Remove(T value) {
        assert(value != null);
        // ------------------------------------------------
        RemoveImpl(value, root_, width_ / 2, height_ / 2,
                                 width_ / 2, height_ / 2);
    }

    public void Intersect(Region2D region, ObjectCollection<T> list) {
        assert(region != null);
        assert(list != null);
        // ------------------------------------------------
        // Add to the list all points that intersect the specified region.
        if((root_ == null) || region.IsEmpty()) {
            return;
        }
        
        IntersectImpl(region, root_, width_ / 2, height_ / 2,
                                     width_ / 2, height_ / 2, list);
    }

    public T Find(T value) {
        assert(value != null);
        // ------------------------------------------------
        // Search for the specified point and return its value.
        if(root_ == null) {
            return null;
        }
        
        return FindImpl(value, root_, width_ / 2, height_ / 2,
                                      width_ / 2, height_ / 2);
    }

    public NearestInfo NearestPoint(Point point) {
        assert(point != null);
        // ------------------------------------------------
        // Find the nearest point to the specified one.
        if(root_ == null) {
            return null;
        }
        
        NearestInfo nearest = new NearestInfo();
        NearestPointImpl(point, root_, nearest, width_ / 2, height_ / 2,
                                                width_ / 2, height_ / 2);
        return nearest;
    }

    public void Near(Point point, double maxDistance, ObjectCollection<T> list) {
        assert(point != null);
        assert(list != null);
        assert(maxDistance >= 0);
        // ------------------------------------------------
        // Search all points that are at a distance smaller than 
        // 'maxDistance' to the specified point.
        if(root_ == null) {
            return;
        }
        
        NearestImpl(point, maxDistance, root_, list, width_ / 2, height_ / 2,
                                                     width_ / 2, height_ / 2);
    }

    public int Count() { 
        return count_; 
    }
    
    public double Width() { 
        return width_; 
    }
    
    public double Height() { 
        return height_; 
    }

    public void Clear() {
        root_ = new Node(width_ / 2, height_ / 2, true);
        count_ = 0;
    }

    /*
    ** Private methods.
    */
    private boolean RemoveImpl(T value, Node node, double x, double y,
                               double width, double height) {
        if(node.IsLeaf()) {
            // Check if the point is found in this node.
            List<T> points = node.Points();
            int count = points.size();
            
            for(int i = 0; i < count; i++) {
                if(points.get(i).equals(value)) {
                    points.remove(i);
                    count_--;
                    return true;
                }
            }
            
            return false;
        }
        
        // Search for the point in each of the children
        // that could contain it (skip non-overlapping regions).
        for(int i = DIRECTION_NW; i <= DIRECTION_SE; i++) {
            double nextX = x + (width * DirectionX[i]);
            double nextY = y + (height * DirectionY[i]);

            if(PointInRegion(value.Position(), nextX, nextY, width, height)) {
                boolean status =  RemoveImpl(value, node.Child(i), nextX, nextY,
                                             width / 2, height / 2);

                // If the point has been removed check if the nodes
                // children can be marked into a single one.
                if(status && MergePossible(node)) {
                    ArrayList<T> list = new ArrayList<T>();

                    if(MergeNodes(node, list)) {
                         node.MakeLeaf(list);
                         return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean MergePossible(Node node) {
         return node.Child(DIRECTION_NE).IsLeaf() ||
                node.Child(DIRECTION_NW).IsLeaf() ||
                node.Child(DIRECTION_SE).IsLeaf() ||
                node.Child(DIRECTION_SW).IsLeaf();
     }

    private boolean MergeNodes(Node node, List<T> list) {
        if(node.IsLeaf()) {
            // Copy all points to the input list.
            List<T> points = node.Points();
            int count = points.size();
            
            for(int i = 0; i < count; i++) {
                list.add(points.get(i));
            }

            return list.size() <= SPLIT_THRESHOLD;
        }
        else {
            // Copy the points from all children to the input list.
            for(int dir = DIRECTION_NW; dir <= DIRECTION_SE; dir++) {
                if(!MergeNodes(node.Child(dir), list)) {
                    return false;
                }
            }

            return true;
        }
    }

    private void IntersectImpl(Region2D region, Node node, 
                               double x, double y,
                               double width, double height, 
                               ObjectCollection<T> list) {
        if(node.IsLeaf()) {
            // Check all points in the leaf node.
            List<T> points = node.Points();
            int count = points.size();
            
            for(int i = 0; i < count; i++) {
                T point = points.get(i);
                
                if(region.Contains(point.Position()) && list.Valid(point)) {
                    list.Add(point);
                }
            }
        }
        else {
            // Check the points in each child whose region 
            // intersects with the specified region only.
            for(int i = DIRECTION_NW; i <= DIRECTION_SE; i++) {
                if(region.IntersectsWith(x + (IntersectionX[i] * width),
                                         y + (IntersectionY[i] * height),
                                         width, height)) {
                    // Search in this child node.
                    IntersectImpl(region, node.Child(i),
                                  x + (DirectionX[i] * width),
                                  y + (DirectionY[i] * height),
                                  width / 2, height / 2, list);
                }
            }
        }
    }

    private T FindImpl(T value, Node node, double x, double y,
                       double width, double height) {
        if(node.IsLeaf()) {
            // Check if the point is found in this leaf node.
            List<T> points = node.Points();
            int count = points.size();
            
            for(int i = 0; i < count; i++) {
                T point = points.get(i);
                
                if(point.equals(value)) {
                    return point;
                }
            }
        }
        else {
            // Check for the point in each child whose region 
            // intersects with the specified region only.
            for(int i = DIRECTION_NW; i <= DIRECTION_SE; i++) {                
                double nextX = x + (width * DirectionX[i]);
                double nextY = y + (height * DirectionY[i]);

                if(PointInRegion(value.Position(), nextX, nextY, width, height)) {
                    // Search in this child node.
                    T result = FindImpl(value, node.Child(i), nextX, nextY,
                                        width / 2, height / 2);
                    if(result != null) {
                        return result;
                    }
                }
             }
        }

        return null;
    }

    private void NearestPointImpl(Point point, Node node, NearestInfo nearest,
                                  double x, double y, double width, double height) {
        if(node.IsLeaf()) {
            // Check each point in the leaf node and keep the nearest one.
            List<T> points = node.Points();
            int count = points.size();
            
            for(int i = 0; i < count; i++) {
                T temp = points.get(i);
                double distance = point.Distance(temp.Position());
                
                if(distance < nearest.Distance()) {
                    // Select the closer point.
                    nearest.SetDistance(distance);
                    nearest.SetValue(temp);
                }
            }
        }
        else {
            // Check which children intersect with the point
            // and search for a closer point in each of them.
            for(int i = DIRECTION_NW; i <= DIRECTION_SE; i++) {
                if(node.Child(i) == null) {
                    continue;
                }

                double nextX = x + (width  * DirectionX[i]);
                double nextY = y + (height * DirectionY[i]);

                if(Overlap(point, nearest.Distance(),
                           nextX, nextY, width, height)) {
                    // Search in this child.
                    NearestPointImpl(point, node.Child(i), nearest,
                                     nextX, nextY, width / 2, height / 2);
                }
            }
        }
    }

    private void NearestImpl(Point point, double maxDistance, 
                             Node node, ObjectCollection<T> list,
                             double x, double y, 
                             double width, double height) {
        if(node.IsLeaf()) {
            // Add all points that are closer than 'maxDistance'.
            List<T> points = node.Points();
            int count = points.size();
            for(int i = 0; i < count; i++) {
                T temp = points.get(i);
                double distance = point.Distance(temp.Position());
                
                if(distance < maxDistance) {
                    list.Add(temp);
                }
            }
        }
        else {
            // Check which children intersect with the point
            // and search for close enough points in each of them.
            for(int i = DIRECTION_NW; i <= DIRECTION_SE; i++) {
                if(node.Child(i) == null) {
                    continue;
                }

                double nextX = x + (width * DirectionX[i]);
                double nextY = y + (height * DirectionY[i]);

                if(Overlap(point, maxDistance,
                           nextX, nextY, width, height)) {
                    // Search in this child.
                    NearestImpl(point, maxDistance, node.Child(i), list,
                                nextX, nextY, width / 2, height / 2);
                }
            }
        }
    }

    private int Direction(double x, double y, double otherX, double otherY) {
        // Return the direction of the first point
        // when compared with the second point.
        if(x < otherX) {
            if(y < otherY) {
                return DIRECTION_NW;
            }
            else {
                return DIRECTION_SW;
            }
        }
        else {
           if(y < otherY) {
               return DIRECTION_NE;
           }
           else {
                return DIRECTION_SE;
            }
       }
    }

    private int Direction(Point a, Node b) {
        return Direction(a.X(), a.Y(), b.X(), b.Y());
    }

    private boolean PointInRegion(Point point, double x, double y,
                                  double width, double height) {
        // X and Y represent the center of the region.
        return (point.X() >= (x - (width / 2))) &&
               (point.Y() >= (y - (height / 2))) &&
               (point.X() <= (x + (width / 2))) &&
               (point.Y() <= (y + (height / 2)));
    }

    private boolean Overlap(double circleX, double circleY, double radius,
                            double rectX, double rectY,
                            double rectWidth, double rectHeight) {
        double dx = circleX - rectX;
        double dy = circleY - rectY;

        // Check for the circle outside the rectangle case first.
        if((dx > ((rectWidth / 2) + radius)) ||
           (dy > ((rectHeight / 2) + radius))) {
            return false;
        }

        // Check for the circle inside the rectangle case.
        if((dx <= (rectWidth / 2)) ||
           (dy <= (rectHeight / 2))) {
            return true;
        }

        // Test for overlap with the rectangles corners
        // (the squared values are compered to not use a slow 'sqrt').
        double  distanceSq = (dx - (rectWidth / 2)) *
                             (dx - (rectWidth / 2)) +
                             (dy - (rectHeight / 2)) *
                             (dy - (rectHeight / 2));
        return distanceSq <= (radius * radius);
    }

     private boolean Overlap(Point circle, double radius,
                            double rectX, double rectY,
                            double rectWidth, double rectHeight) {
         return Overlap(circle.X(), circle.Y(), radius,
                        rectX, rectY, rectWidth, rectHeight);
     }
}
