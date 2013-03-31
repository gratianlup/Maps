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

package Rendering.Animations;
import Rendering.*;

public final class PanAnimation extends AnimationBase {
    private double startX_;
    private double startY_;
    private double finalX_;
    private double finalY_;
    private double deltaX_;
    private double deltaY_;
    private View view_;

    /*
     ** Constructors.
     */
    public PanAnimation(IAnimatable parent, View view, 
                        double dx, double dy, long duration,
                        IInterpolation interpolation){
        super(parent, duration, interpolation);
        deltaX_ = dx;
        deltaY_ = dy;
        view_ = view;
    }

    /*
     ** Public methods.
     */
    @Override
    public void Update() {
        double x = Math.floor(Interpolation().GetValue(startX_, finalX_, Progress()));
        double y = Math.floor(Interpolation().GetValue(startY_, finalY_, Progress()));
        view_.Bounds().SetLeft(x);
        view_.Bounds().SetTop(y);
    }

    @Override
    public void Start() {
        startX_ = view_.Bounds().Left();
        startY_ = view_.Bounds().Top();
        finalX_ = startX_ + deltaX_;
        finalY_ = startY_ + deltaY_;
        StartWatch();
    }

    public double DeltaX() { 
        return deltaX_; 
    }
    
    public double DeltaY() { 
        return deltaY_; 
    }
}
