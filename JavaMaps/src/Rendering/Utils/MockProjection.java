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

// Mock projection to be used only during testing!
public class MockProjection implements IProjection {
    int levels;

    public MockProjection(IMapProvider provider) {
        levels = provider.ZoomLevels();
    }

    public Point FromCoordinates(Coordinates coord, int zoomLevel) {
        double scale = 1.0 / (levels - zoomLevel);
        return new Point(coord.Longitude() * scale,
                         coord.Latitude() * scale);
    }

    public Coordinates ToCoordinates(Point point, int zoomLevel) {
        double scale = levels - zoomLevel;
        return new Coordinates(point.Y() * scale, point.X() * scale);
    }

    public Region2D FromRegion(Region region, int zoomLevel) {
        double scale = 1.0 / (levels - zoomLevel);
        return new Region2D(region.TopLeft().Longitude() * scale,
                            region.TopLeft().Latitude() * scale,
                            (region.BottomRight().Longitude() - region.TopLeft().Longitude()) * scale,
                            (region.BottomRight().Latitude() - region.TopLeft().Latitude()) * scale);
    }

    public Region FromRegion2D(Region2D region, int zoomLevel) {
        double scale = levels - zoomLevel;
        return new Region(region.Top() * scale, region.Left() * scale,
                          region.Bottom() * scale, region.Right() * scale);
    }
}
