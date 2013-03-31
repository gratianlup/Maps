// Copyright (c) 2010 Ramona Maris. All rights reserved.
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

package Core;
import java.io.Serializable;

public class Color implements Serializable {
    private int R_;
    private int G_;
    private int B_;
    private int alpha_;

    /*
     ** Constructors.
     */
    public Color(int R, int G, int B, int alpha){
        R_ = R;
        G_ = G;
        B_ = B;
        alpha_ = alpha;
    }

    public Color(int R, int G, int B){
        R_ = R;
        G_ = G;
        B_ = B;
        alpha_ = 255; // Opac.
    }

    /*
     ** Public methods.
     */
    public int R(){ return R_;}
    public void SetR( int R ){ R_ = R;}
    public int G(){ return G_;}
    public void SetG( int G ){ G_ = G;}
    public int B(){ return B_;}
    public void SetB( int B ){ B_ = B;}
    public int Alpha(){ return alpha_;}
    public void SetAlpha( int alpha ){ alpha_ = alpha;}

    //Pentru debugging.
    @Override
    public String toString(){
        return "R=" + R_ + ", G=" + G_ + ", B=" + B_ + ", A=" + alpha_;
    }
}
