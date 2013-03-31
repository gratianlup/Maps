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
import Rendering.Utils.*;

public abstract class AnimationBase {
    protected IAnimatable parent_;
    protected IInterpolation interpolation_;
    protected Stopwatch watch_;

    /*
     ** Constructors.
     */
    public AnimationBase(IAnimatable parent, long duration,
                         IInterpolation interpolation) {
        parent_ = parent;
        interpolation_ = interpolation;
        watch_ = new Stopwatch(duration);
    }

    /*
     ** Protected methods.
     */
    protected void StartWatch() { 
        watch_.Start(); 
    }
    
    protected void StopWatch() { 
        watch_.Start(); 
    }

    /*
     ** Public methods.
     */
    public IAnimatable Parent() { 
        return parent_; 
    }
    
    public boolean Completed() { 
        return watch_.Completed(); 
    }
    
    public double Progress() { 
        return watch_.Progress(); 
    }
    
    public long Duration() { 
        return watch_.Duration(); 
    }
    
    public IInterpolation Interpolation() { 
        return interpolation_; 
    }

    /*
     * Abstract methods.
     */
    public abstract void Update();
    public abstract void Start();
}
