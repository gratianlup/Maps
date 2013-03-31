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
import Core.*;

public final class ZoomAnimation extends AnimationBase {
    private View current_;
    MapViewer viewer_;
    double xPerc_;
    double yPerc_;
    double startZoom_;
    double endZoom_;

    /*
     ** Constructors.
     */
    public ZoomAnimation(IAnimatable parent, View current,
                         double origXPerc, double origYPerc, double endZoom,
                         long duration, IInterpolation interpolation){
        super(parent, duration, interpolation);
        viewer_ = (MapViewer)parent;
        current_ = current;
        xPerc_ = origXPerc;
        yPerc_ = origYPerc;
        startZoom_ = current.Zoom();
        endZoom_ = endZoom;
    }

    /*
     ** Public methods.
     */
    @Override
    public void Update() {
        double zoom = Interpolation().GetValue(startZoom_, endZoom_, Progress());
        viewer_.ApplyZoomStep(xPerc_, yPerc_, zoom);
    }

    @Override
    public void Start() {
        StartWatch();
    }

    public double StartZoom() { 
        return startZoom_; 
    }
    
    public double EndZoom() { 
        return startZoom_; 
    }
    
    public double DeltaZoom() { 
        return endZoom_ - startZoom_; 
    }
}
