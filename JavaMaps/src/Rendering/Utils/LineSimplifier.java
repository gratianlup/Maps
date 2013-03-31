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
import java.util.*;
import Core.*;

public class LineSimplifier {
    private static void SimplifyImpl(List<Point> points, int startPoint,
                                     int endPoint, double minDistanceSq,
                                     List<Point> selected) {
        double startX = points.get(startPoint).X();
        double startY = points.get(startPoint).Y();
        double endX = points.get(endPoint).X();
        double endY = points.get(endPoint).Y();
        double maxDistSq = 0;
        int index = -1;

        for(int i = startPoint + 1; i < endPoint; i++) {
            double distance = LineUtils.PointLineDistanceSq(points.get(i),
                                              startX, startY, endX, endY);
            if(distance > maxDistSq) {
                maxDistSq = distance;
                index = i;
            }
        }

        // If the distance is large enough simplification is attempted
        // on the regions found on the left and right of this point.
        if(maxDistSq >= minDistanceSq) {
            if((index - startPoint) >= 2) {
                SimplifyImpl(points, startPoint, index,  minDistanceSq, selected);
            }

            selected.add(points.get(index));
            
            if((endPoint - index) >= 2) {
                SimplifyImpl(points, index, endPoint, minDistanceSq, selected);
            }
        }
    }

    // Select a subset of the points using the Douglas-Peucker algorithm.
    // astfel incat sa se pastreze precizia specificata.
    // http://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm
    public static void Simplify(List<Point> points, double minDistance,
                                List<Point> selected) {
        assert(points != null);
        assert(minDistance >= 0);
        assert(selected != null && points.size() >= 2);
        // ------------------------------------------------
        // The first and last points are always included.
        selected.add(points.get(0));
        SimplifyImpl(points, 0, points.size() - 1, minDistance * minDistance, selected);
        selected.add(points.get(points.size() - 1));
    }
}
