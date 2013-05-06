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
import Core.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import Rendering.Animations.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class NavigationWheel implements IAnimatable {
    // Animation used to show/hide the arrows when the point
    // used to move the map view is being used.
    private final class ArrowAnimation extends AnimationBase {
        private double start_;
        private double end_;

        public ArrowAnimation(NavigationWheel parent, double start, double end,
                              long duration, IInterpolation interpolation) {
            super(parent, duration, interpolation);
            start_ = start;
            end_ = end;
        }

        @Override
        public void Update() {
            arrowOpacity_ = interpolation_.GetValue(start_, end_, Progress());
        }

        @Override
        public void Start() { 
            StartWatch(); 
        }
    }
    
    // Animation that brings the point used to move the mpa view
    // in its default position (in the center of the wheel).
    private final class PointAnimation extends AnimationBase {
        private double startX_;
        private double startY_;

        public PointAnimation(NavigationWheel parent, double startX, double startY,
                              long duration, IInterpolation interpolation) {
            super(parent, duration, interpolation);
            startX_ = startX;
            startY_ = startY;
        }

        @Override
        public void Update() {
            pointX_ = interpolation_.GetValue(startX_, centerX_, Progress());
            pointY_ = interpolation_.GetValue(startY_, centerY_, Progress());
        }

        @Override
        public void Start() { 
            StartWatch(); 
        }
    }

    class PanUpdater implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            double dx = pointX_ - centerX_;
            double dy = pointY_ - centerY_;
            host_.Pan(new Point(dx / 2, dy / 2), 0 /* without animation */);
        }
    }

    /*
    ** Constants.
    */
    private static final double ARROW_SIZE = 20;
    private static final double ARROW_MARGIN = 6;
    private static final int NO_ARROW = -1;
    private static final double POINT_SIZE = 20;
    private static final float CIRCLE_WIDTH = 8;
    private static final long PAN_DURATION = 500;
    private static final long ARROW_ANIMATION_DURATION = 200;
    private static final long POINT_ANIMATION_DURATION = 200;

    /*
    ** Members.
    */
    private IRendererHost host_;
    private double left_;
    private double top_;
    private double width_;
    private double height_;
    private double centerX_;
    private double centerY_;

    private Color backColor_;
    private Color pointColor_;
    private Color arrowColor_;
    private Color selectColor_;
    private double opacity_;
    private GeneralPath arrow_;
    private BasicStroke circleStroke_;
    
    private double pointX_;
    private double pointY_;
    private double[] arrowX_;
    private double[] arrowY_;
    private double[] arrowAngle_;
    private double[] arrowAdjX_;
    private double[] arrowAdjY_;

    private boolean mouseCaptured_;
    private int selectedArrow_;
    private Point mouseStart_;
    private double arrowOpacity_;
    private javax.swing.Timer panTimer_;

    /*
    ** Constructors.
    */
    public NavigationWheel(IRendererHost host) {
        host_ = host;
        circleStroke_ = new BasicStroke(CIRCLE_WIDTH);

        // Generate an arrow pointed to the right.
        // The other arrows are drawn by rotation this arrow.
        arrow_ = new GeneralPath();
        arrow_.moveTo(0, 0);
        arrow_.lineTo(ARROW_SIZE, ARROW_SIZE / 2);
        arrow_.lineTo(0, ARROW_SIZE);
        arrow_.closePath();

        arrowX_ = new double[4];
        arrowY_ = new double[4];
        arrowAngle_ = new double[4];
        arrowAdjX_ = new double[4];
        arrowAdjY_ = new double[4];
        
        arrowAngle_[0] =  Math.PI;
        arrowAngle_[1] = -Math.PI / 2;
        arrowAngle_[2] =  0;
        arrowAngle_[3] =  Math.PI / 2;

        // Adjustment factor after the rotation transformation.
        arrowAdjX_[0] =  1.0;
        arrowAdjY_[0] =  1.0;
        arrowAdjX_[1] =  0.0;
        arrowAdjY_[1] =  1.0;
        arrowAdjX_[2] =  0.0;
        arrowAdjY_[2] =  0.0;
        arrowAdjX_[3] =  1.0;
        arrowAdjY_[3] =  0.0;

        selectedArrow_ = NO_ARROW;
        arrowOpacity_ = 1.0;
    }

    /*
    ** Public methods.
    */
    public double Left() { 
        return left_; 
    }
    
    public void SetLeft(double value) { 
        left_ = value; 
    }

    public double Top() { 
        return top_; 
    }
    
    public void SetTop(double value) { 
        top_ = value; 
    }

    public double Width() { 
        return width_; 
    }
    
    public void SetWidth(double value) {
        width_ = height_ = value;

        // Recompute the element position (relative to the top-left corner).
        centerX_ = pointX_ = value / 2;
        centerY_ = pointY_ = value / 2;

        arrowX_[0] = ARROW_MARGIN; // Left.
        arrowY_[0] = (height_ / 2) - (ARROW_SIZE / 2);
        
        arrowX_[1] = (width_ / 2) - (ARROW_SIZE / 2); // Up.
        arrowY_[1] = ARROW_MARGIN;

        arrowX_[2] = width_ - ARROW_SIZE - ARROW_MARGIN; // Right.
        arrowY_[2] = (height_ / 2) - (ARROW_SIZE / 2);

        arrowX_[3] = (width_ / 2) - (ARROW_SIZE / 2); // Down.
        arrowY_[3] = height_ - ARROW_SIZE - ARROW_MARGIN;
    }

    public double Height() { 
        return height_; 
    }
    
    public void SetHeight(double value) {
        SetWidth(value);
    }

    public Color GetBackColor() { 
        return backColor_; 
    }
    
    public void SetBackColor(Color value) { 
        backColor_ = value; 
    }

    public Color GetPointColor() { 
        return pointColor_; 
    }
    
    public void SetPointColor(Color value) {
        pointColor_ = value; 
    }

    public Color GetArrowColor() { 
        return arrowColor_; 
    }
    
    public void SetArrowColor(Color value) { 
        arrowColor_ = value; 
    }

    public Color GetSelectColor() {
        return selectColor_; 
    }
    
    public void SetSelectColor(Color value) { 
        selectColor_ = value; 
    }

    public void SetOpacity(double value) { 
        opacity_ = value; 
    }

    public boolean MouseDown(Point point) {
        // Check if one of the arrows has been selected.
        double x = point.X() - left_;
        double y = point.Y() - top_;

        for(int i = 0; i < 4; i++) {
            if((x >= arrowX_[i]) && (x <= arrowX_[i] + ARROW_SIZE) &&
               (y >= arrowY_[i]) && (y <= arrowY_[i] + ARROW_SIZE)) {
                // Move the map in the corresponding direction.
                selectedArrow_ = i;
                Pan(i);
                return true;
            }
        }

        // Check if the point in the center of the wheel has been selected.
        if(Point.Distance(pointX_, pointY_, x, y) <= POINT_SIZE) {
            mouseCaptured_ = true;
            mouseStart_ = new Point(x, y);
            HideArrows();

            // Start the timer that animates the map panning.
            panTimer_ = new javax.swing.Timer(16, new PanUpdater());
            panTimer_.start();
            return true;
        }

        return false;
    }

    public boolean MouseUp(Point point) {
        if(mouseCaptured_) {
            // Reshow the arrows and bring the point in the center of the wheel.
            mouseCaptured_ = false;
            ShowArrows();
            ResetPoint();
            panTimer_.stop();
            panTimer_ = null;
            host_.Repaint();
            return true;
        }

        if(selectedArrow_ != NO_ARROW) {
            selectedArrow_ = NO_ARROW;
            host_.Repaint();
            return true;
        }

        return false;
    }

    public boolean MouseDragged(Point point) {
        if(mouseCaptured_) {
            // Compute the new coordinates of the point.
            double dx = (point.X() - left_) - mouseStart_.X();
            double dy = (point.Y() - top_) - mouseStart_.Y();
            double newX = centerX_ + dx;
            double newY = centerY_ + dy;

            double distance = Point.Distance(newX, newY, centerX_, centerY_);
            double maxDistance = (width_ / 2) - (CIRCLE_WIDTH / 2) - (POINT_SIZE / 2);

            // Don't allow the point to move outside the circle.
            if(distance > maxDistance) {
                distance = maxDistance;
                double angle = Math.atan2(dy, dx);
                newX = centerX_ + (maxDistance * Math.cos(angle));
                newY = centerY_ + (maxDistance * Math.sin(angle));
            }

            // Now move the map view.
            pointX_ = newX;
            pointY_ = newY;
            host_.Repaint();
            return true;
        }

        return false;
    }

    public boolean MouseCaptured() { 
        return mouseCaptured_; 
    }
    
    public void AnimationCompleted(AnimationBase animation) {
    }

    public void Draw(Graphics2D g) {
        AffineTransform prevTransform = g.getTransform();

        // Draw the wheel.
        g.setStroke(circleStroke_);
        g.setColor(backColor_);
        g.drawOval((int)left_, (int)top_,
                   (int)width_, (int)height_);

        // Draw the point inside the wheel.
        if(mouseCaptured_) {
            g.setColor(selectColor_);
        }
        else {
            g.setColor(pointColor_);
        }

        g.fillOval((int)(left_ + pointX_ - POINT_SIZE / 2),
                   (int)(top_  + pointY_ - POINT_SIZE / 2),
                   (int)POINT_SIZE, (int)POINT_SIZE);

        // Draw the four arrows by rotating
        // the arrow that points to the right.
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                  (float)(arrowOpacity_ * opacity_)));

        for(int i = 0; i < 4; i++) {
            if(i == selectedArrow_) {
                g.setColor(selectColor_);
            }
            else {
                g.setColor(arrowColor_);
            }

            g.setTransform(prevTransform);
            g.translate(left_ + arrowX_[i] + (arrowAdjX_[i] * ARROW_SIZE),
                        top_  + arrowY_[i] + (arrowAdjY_[i] * ARROW_SIZE));
            g.rotate(arrowAngle_[i]);
            g.fill(arrow_);
        }
    }

    /*
    ** Private methods.
    */
    private void Pan(int direction) {
        // Move the map view in the direction of the selected arrow.
        double delta = 2 * host_.MapProvider().TileSize();

        switch(direction) {
            case 0: { host_.Pan(new Point(-delta, 0), PAN_DURATION); break; }
            case 1: { host_.Pan(new Point(0, -delta), PAN_DURATION); break; }
            case 2: { host_.Pan(new Point(delta, 0),  PAN_DURATION); break; }
            case 3: { host_.Pan(new Point(0, delta),  PAN_DURATION); break; }
        }
    }
    
    private void HideArrows() {
        ArrowAnimation arrowAnim = new ArrowAnimation(this, arrowOpacity_, 0.0,
                                                      ARROW_ANIMATION_DURATION,
                                                      new LinearInterpolation());
        host_.AddAnimation(arrowAnim);
    }

    private void ShowArrows() {
        ArrowAnimation arrowAnim = new ArrowAnimation(this, arrowOpacity_, 1.0,
                                                      ARROW_ANIMATION_DURATION,
                                                      new LinearInterpolation());
        host_.AddAnimation(arrowAnim);
    }

    private void ResetPoint() {
        ExpInterpolation.Mode mode = ExpInterpolation.Mode.EaseIn;
        PointAnimation pointAnim = 
                new PointAnimation(this, pointX_, pointY_,
                                   POINT_ANIMATION_DURATION,
                                   new ExpInterpolation(3, mode));
        host_.AddAnimation(pointAnim);
    }
}
