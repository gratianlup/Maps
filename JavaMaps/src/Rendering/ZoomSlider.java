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

public final class ZoomSlider {
    private enum SelectedButton {
        ButtonPlus,
        ButtonMinus,
        None
    }

    private static final double SLIDER_HEIGHT = 10;
    private static final double BUTTON_HEIGHT = 16;
    private static final long ZOOM_DURATION = 500;

    /*
    ** Members.
    */
    private IRendererHost host_;
    private double maxZoom_;
    private double minZoom_;
    private double currentZoom_;
    private double left_;
    private double top_;
    private double width_;
    private double height_;
    private boolean showMarks_;
    private Color backColor_;
    private Color buttonColor_;
    private Color selectColor_;
    private Color fontColor_;
    private GeneralPath button_;
    private double sliderTop_;
    private SelectedButton selected_;

    /*
    ** Constructors.
    */
    public ZoomSlider(IRendererHost host, double minZoom, double maxZoom,
                      double width, double height) {
        host_ = host;
        minZoom_ = minZoom;
        maxZoom_ = maxZoom;
        width_ = width;
        height_ = height;
        currentZoom_ = minZoom;
        showMarks_ = true;
        selected_ = SelectedButton.None;

        // Draw the button.
        button_ = new GeneralPath();
        button_.moveTo(0, 0);
        button_.lineTo(width_, 0);
        button_.lineTo(width_, BUTTON_HEIGHT);
        button_.curveTo(width_, BUTTON_HEIGHT + 8,
                      0.0, BUTTON_HEIGHT + 8,
                      0.0, BUTTON_HEIGHT);
        button_.lineTo(0, 0);
        button_.closePath();
    }

    /*
    ** Public methods.
    */
    public double MaxZoom() { 
        return maxZoom_; 
    }
    
    public double MinZoom() { 
        return minZoom_; 
    }

    public double CurrentZoom() { 
        return currentZoom_; 
    }
    
    public void SetCurrentZoom(double value) {
        currentZoom_ = value;
        double axisLength = height_ - 2*BUTTON_HEIGHT - SLIDER_HEIGHT;
        sliderTop_ = axisLength -
                     (currentZoom_ * (axisLength / (maxZoom_ - minZoom_)));
    }

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
        width_ = value; 
    }

    public double Height() { 
        return height_; 
    }
    
    public void SetHeight(double value) { 
        height_ = value; 
    }

    public boolean ShowMarks() { 
        return showMarks_; 
    }
    
    public void SetShowMarks(boolean value) { 
        showMarks_ = value; 
    }

    public Color BackColor() { 
        return backColor_; 
    }
    
    public void SetBackColor(Color value) { 
        backColor_ = value; 
    }

    public Color ButtonColor() { 
        return buttonColor_; 
    }
    
    public void SetButtonColor(Color value) { 
        buttonColor_ = value; 
    }

    public Color GetFontColor() { 
        return fontColor_; 
    }
    
    public void SetFontColor(Color value) { 
        fontColor_ = value; 
    }

    public Color GetSelectColor() { 
        return selectColor_; 
    }
    
    public void SetSelectColor(Color value) { 
        selectColor_ = value; 
    }

    public boolean MouseDown(Point point) {
        if((point.X() < left_) || (point.X() > (left_ + width_)) ||
           (point.Y() < top_) || (point.Y() > (top_ + height_))) {
            // The cursor is not over the slider.
            return false;
        }

        if(point.Y() < (top_ + BUTTON_HEIGHT)) {
            // The + button has been selected.
            host_.Zoom(1.0, ZOOM_DURATION);
            selected_ = SelectedButton.ButtonPlus;
            return true;
        }
        else if(point.Y() > (top_ + height_ - BUTTON_HEIGHT)) {
            // The - button has been selected.
            host_.Zoom(-1.0, ZOOM_DURATION);
            selected_ = SelectedButton.ButtonMinus;
            return true;
        }
        else {
            // If the region above the indicator is selected
            // zoom in is performed, if below zoom out.
            if(point.Y() < (top_ + BUTTON_HEIGHT + sliderTop_)) {
                host_.Zoom(1.0, ZOOM_DURATION);
                selected_ = SelectedButton.ButtonPlus;
            }
            else if(point.Y() > (top_ + BUTTON_HEIGHT + sliderTop_ + SLIDER_HEIGHT)) {
                host_.Zoom(-1.0, ZOOM_DURATION);
                selected_ = SelectedButton.ButtonMinus;
            }

            return true;
        }
    }

    public boolean MouseUp(Point point) {
        if(selected_ != SelectedButton.None) {
            selected_ = SelectedButton.None;
            host_.Repaint();
            return true;
        }

        return false;
    }

    public boolean MouseDragged(Point point) { 
        return false; 
    }
    
    public boolean MouseCaptured() { 
        return false; 
    }

    public void Draw(Graphics2D g) {
        AffineTransform prevTransform = g.getTransform();
        g.setColor(backColor_);

        // Draw the lines that delimit the slider.
        g.fillRect((int)(left_),
                   (int)(top_ + BUTTON_HEIGHT),
                   (int)width_,
                   (int)(height_ - (2 * BUTTON_HEIGHT)));

        if(showMarks_) {
            double step = (height_ - (2 * BUTTON_HEIGHT)) / (maxZoom_ - minZoom_);
            int last = (int)(maxZoom_ - minZoom_ - 1);

            if(last > 0) {
                g.setColor(buttonColor_);
                for(int i = 1; i <= last; i++) {
                    double y = top_ + BUTTON_HEIGHT + (step * i);
                    g.drawLine((int)left_, (int)y,
                               (int)(left_ + width_ - 1), (int)y);
                }
            }
        }

        // Draw the - button.
        if(selected_ == SelectedButton.ButtonMinus) {
            g.setColor(selectColor_);
        }
        else {
            g.setColor(buttonColor_);
        }

        g.translate(left_, top_ - BUTTON_HEIGHT + height_);
        g.fill(button_);

        // Draw the + button.
        if(selected_ == SelectedButton.ButtonPlus) {
            g.setColor(selectColor_);
        }
        else {
            g.setColor(buttonColor_);
        }
        
        g.translate(width_, -height_ + 2*BUTTON_HEIGHT);
        g.rotate(Math.PI);
        g.fill(button_);

        // Draw the text representing the +.
        g.setColor(fontColor_);
        g.setTransform(prevTransform);
        g.fillRect((int)(left_ + width_ / 2 - 6),
                   (int)(top_ + BUTTON_HEIGHT / 2 - 4), 11, 3);
        g.fillRect((int)(left_ + width_ / 2 - 2),
                   (int)(top_ + BUTTON_HEIGHT / 2 - 8), 3, 12);

        // Draw the text representing the -.
        g.fillRect((int)(left_ + width_ / 2 - 6),
                   (int)(top_ + height_ - BUTTON_HEIGHT / 2 + 1), 11, 3);

        // Now draw the slider.
        g.setColor(selectColor_);
        g.fillRect((int)left_, (int)(top_ + BUTTON_HEIGHT + sliderTop_),
                   (int)width_, (int)SLIDER_HEIGHT);
    }
}
