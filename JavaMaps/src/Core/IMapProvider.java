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

package Core;
import java.util.*;
import java.awt.Image;
import Data.*;
import java.io.*;

public interface IMapProvider {
    // Map information.
    String Name();
    int ZoomLevels();
    Region MapBounds();
    Region2D MapBounds(int zoomLevel);
    IProjection Projection();

    // Layers.
    int LayerCount();
    boolean HasLayer(LayerType layerType);
    ILayer GetLayer(ObjectId layerId);
    Iterator<ILayer> GetLayerIterator();

    // Tiles.
    double TileSize();
    int TileCount(int zoomLevel);
    TileInfo GetTile(Point point, int zoomLevel);
    void GetTiles(Region2D region, int zoomLevel, List<TileInfo> list);
    Image LoadTile(ObjectId tileId);

    // Nodes.
    int NodeNumber();
    Iterator<Node> GetNodeIterator();
    Node GetNode(ObjectId nodeID);

    // Streets.
    int StreetCount();
    //TODO
    Iterator<Street> GetStreetIterator();
    Street GetStreet(ObjectId streetID);
    Color StreetColor(StreetType streetType);

    // Markers.
    int MarkerCount(ObjectId layerID);
    Iterator<Marker> GetMarkerIterator(ObjectId layerID);
    Marker GetMarker(ObjectId markerID, ObjectId layerID);
    Image LoadMarkerIcon(ObjectId layerID) throws IOException;

    // Map operations.
    void Load(String location) throws IOException, IllegalStateException;
    void Save(String location) throws IOException;
}
