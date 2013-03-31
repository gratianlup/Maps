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
import Data.*;
import java.awt.Image;
import java.util.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.*;

// Mock map provider to be used only during testing!
public class MockMapProvider implements IMapProvider {

    class ImageLayer implements ILayer {
        ObjectId id_;
        
        ImageLayer() {
            id_ = ObjectId.NewId();
        }

        public ObjectId ID() {
            return id_;
        }

        public String Name() {
            return "Image layer";
        }

        public LayerType Type() {
            return LayerType.Image;
        }
    }

    class StreetLayer implements ILayer {
        ObjectId id_;

        StreetLayer() {
            id_ = ObjectId.NewId();
        }

        public ObjectId ID() {
            return id_;
        }

        public String Name() {
            return "Street layer";
        }

        public LayerType Type() {
            return LayerType.Street;
        }
    }

    class MarkerLayer implements ILayer {
        ObjectId id_;
        String name_;

        MarkerLayer(String name) {
            id_ = ObjectId.NewId();
            name_ = name;
        }

        public ObjectId ID() {
            return id_;
        }

        public String Name() {
            return name_;
        }

        public LayerType Type() {
            return LayerType.Marker;
        }
    }

    class Tile {
        public ObjectId Id;
        public String Path;

        public Tile(ObjectId id, String path) {
            Id = id;
            Path = path;
        }
    }

    ArrayList<ILayer> layers = new ArrayList<ILayer>();
    Tile[] tiles1 = new Tile[] {
        new Tile(ObjectId.NewId(), "0_0.jpg"),
        new Tile(ObjectId.NewId(), "0_1.jpg"),
        new Tile(ObjectId.NewId(), "0_2.jpg"),
        new Tile(ObjectId.NewId(), "0_3.jpg"),
    };

    Tile[] tiles2 = new Tile[] {
        new Tile(ObjectId.NewId(), "1_0.jpg"),
        new Tile(ObjectId.NewId(), "1_1.jpg"),
        new Tile(ObjectId.NewId(), "1_2.jpg"),
        new Tile(ObjectId.NewId(), "1_3.jpg"),
        new Tile(ObjectId.NewId(), "1_4.jpg"),
        new Tile(ObjectId.NewId(), "1_5.jpg"),
        new Tile(ObjectId.NewId(), "1_6.jpg"),
        new Tile(ObjectId.NewId(), "1_7.jpg"),
        new Tile(ObjectId.NewId(), "1_8.jpg"),
        new Tile(ObjectId.NewId(), "1_9.jpg"),
        new Tile(ObjectId.NewId(), "1_10.jpg"),
        new Tile(ObjectId.NewId(), "1_11.jpg"),
        new Tile(ObjectId.NewId(), "1_12.jpg"),
        new Tile(ObjectId.NewId(), "1_13.jpg"),
        new Tile(ObjectId.NewId(), "1_14.jpg"),
        new Tile(ObjectId.NewId(), "1_15.jpg")
    };

    Tile[] tiles3 = new Tile[64];
    Tile[] tiles4 = new Tile[256];
    Tile[] tiles5 = new Tile[1024];
    ArrayList<Tile[]> tiles = new ArrayList<Tile[]>();
    ArrayList<Street> streets = new ArrayList<Street>();
    ArrayList<Marker> markers1 = new ArrayList<Marker>();
    ArrayList<Marker> markers2 = new ArrayList<Marker>();
    ArrayList<Marker> markers3 = new ArrayList<Marker>();
    String[] names = new String[] {
        "Second", "Third", "Park", "Main", "Oak", "Maple", "Washington", "Hill", "Lake"
    };

    HashMap<ObjectId, Node> nodes = new HashMap<ObjectId, Node>();

    private void AddBezierStreet(Point control1, Point control2, Point anchor1, Point anchor2) {
        double u = 0;
        double su = 1.0 / 6.0;
        Street street = new Street(ObjectId.NewId(), StreetType.Street,
                                       "b", null, null);
        for(int i = 0; i <= 6; i++) {
            double x = Math.pow(u,3)*(anchor2.X()+3*(control1.X()-control2.X())-anchor1.X())
                         +3*Math.pow(u,2)*(anchor1.X()-2*control1.X()+control2.X())
                        +3*u*(control1.X()-anchor1.X())+anchor1.X();
            double y = Math.pow(u,3)*(anchor2.Y()+3*(control1.Y()-control2.Y())-anchor1.Y())
                        +3*Math.pow(u,2)*(anchor1.Y()-2*control1.Y()+control2.Y())
                        +3*u*(control1.Y()-anchor1.Y())+anchor1.Y();
            street.AddCoordinate(new Coordinates(y, x));
            u += su;
        }
        streets.add(street);
    }

    public MockMapProvider() {
        layers.add(new ImageLayer());
        layers.add(new RoadLayer(ObjectId.NewId(), new Color(0,0,0), new Color(0,0,0), new Color(0,0,0)));
        layers.add(new MarkerLayer("1"));
        layers.add(new MarkerLayer("2"));
        layers.add(new MarkerLayer("3"));

        for(int i = 0; i < 64; i++) {
            tiles3[i] = new Tile(ObjectId.NewId(), "2_" + i + ".jpg");
        }

        for(int i = 0; i < 256; i++) {
            tiles4[i] = new Tile(ObjectId.NewId(), "3_" + i + ".jpg");
        }

         for(int i = 0; i < 1024; i++) {
            tiles5[i] = new Tile(ObjectId.NewId(), "4_" + i + ".jpg");
        }

        tiles.add(tiles1);
        tiles.add(tiles2);
        tiles.add(tiles3);
        tiles.add(tiles4);
        tiles.add(tiles5);

        Random rand = new Random(19);
        Node prevNode = null;
        for(int i = 0; i < 320; i++) {
            StreetType type;
            int t = rand.nextInt(20);
            if(t < 14) type = StreetType.Street;
            else if(t < 17) type = StreetType.Avenue;
            else type = StreetType.Boulevard;
            Street street = new Street(ObjectId.NewId(), type,
                                       names[rand.nextInt(names.length)], null, null);
            streets.add(street);
            prevNode = null;
            for(int j = 0; j < 8; j++) {
                Coordinates coord = new Coordinates( j* (8192/8),
                                     i * (8192/32) + rand.nextInt(12));
                street.AddCoordinate(coord);
                Node node = new Node(ObjectId.NewId(), coord);
                nodes.put(node.Id(), node);
                if(street.StartNode() == null) {
                    street.SetStartNode(node);
                }

                if(prevNode != null && i % 2 != 0) {
                    prevNode.AddLink(new Link(ObjectId.NewId(), node.Id(), street.Id()));
                   // node.AddLink(new Link(ObjectId.NewId(), prevNode.Id(), street.Id()));
                }

                prevNode = node;
            }
        }

        prevNode = null;
        Node firstNode = null;
        
        for(int i = 0; i < 240; i++) {
            StreetType type;
            int t = rand.nextInt(20);
            if(t < 13) type = StreetType.Street;
            else if(t < 16) type = StreetType.Avenue;
            else type = StreetType.Boulevard;
            Street street = new Street(ObjectId.NewId(), type,
                                       names[rand.nextInt(names.length)], null, null);
            streets.add(street);
            prevNode = null;

            for(int j = 0; j < 8; j++) {
                Coordinates coord = new Coordinates(32 + i * (8192/24),
                                                    j* (8192/8) + rand.nextInt(12));
                street.AddCoordinate(coord);
                Node node = new Node(ObjectId.NewId(), coord);
                if(firstNode == null) firstNode = node;
                nodes.put(node.Id(), node);
                if(street.StartNode() == null) {
                    street.SetStartNode(node);
                }

                if(prevNode != null && i % 2 != 0) {
                    prevNode.AddLink(new Link(ObjectId.NewId(), node.Id(), street.Id()));
                    node.AddLink(new Link(ObjectId.NewId(), prevNode.Id(), street.Id()));
                }

                prevNode = node;
            }
        }


        Street street = new Street(ObjectId.NewId(), StreetType.Street,
                                   "a", null, null);
        streets.add(street);
        for(int i = 0; i <= 20; i++) {
            double x = 50*Math.cos(Math.toRadians(360 / 20 * i));
            double y = 50*Math.sin(Math.toRadians(360 /20 * i));
            street.AddCoordinate(new Coordinates(117 + y, 257 + x));
        }

        AddBezierStreet(new Point(150, 250), new Point(150, 500),
                        new Point(0, 250), new Point(0, 500));

        markers1.add(new Marker(ObjectId.NewId(), new Coordinates(52, 48), "McDonalds"));
        markers1.add(new Marker(ObjectId.NewId(), new Coordinates(142, 495), "Wendy’s"));
        markers1.add(new Marker(ObjectId.NewId(), new Coordinates(324, 280), "Taco Bell"));
        markers1.add(new Marker(ObjectId.NewId(), new Coordinates(528, 535), "Pizza Hut"));

        markers2.add(new Marker(ObjectId.NewId(), new Coordinates(52+256, 48+310), "Starbucks"));
        markers2.add(new Marker(ObjectId.NewId(), new Coordinates(324+256, 495+310), "Starbucks"));
        markers2.add(new Marker(ObjectId.NewId(), new Coordinates(528+256, 535+290), "Starbucks"));

        markers3.add(new Marker(ObjectId.NewId(), new Coordinates(200+236, 300+270), "Shell Oil"));
        markers3.add(new Marker(ObjectId.NewId(), new Coordinates(500+151, 500+180), "Shell Oil"));

        for(Marker m : markers1) {
            m.SetNearestNode(firstNode.Id());
        }
    }

    public String Name() {
        return "Test map";
    }

    public int ZoomLevels() {
        return tiles.size();
    }

    public double TileSize() { return 256.0; }

    public Region MapBounds() {
        return new Region(0, 0, 8192, 8192);
    }

    public Region2D MapBounds(int zoomLevel) {
        return new Region2D(Point.Zero, 256 * (1 << (zoomLevel + 1)),
                            256 * (1 << (zoomLevel + 1)));
    }

    public IProjection Projection() {
        return new MockProjection(this);
    }

    public int LayerCount() {
        return layers.size();
    }

    public boolean HasLayer(LayerType layerType) {
        for(int i = 0; i < layers.size(); i++) {
            if(layers.get(i).Type() == layerType) {
                return true;
            }
        }
        return false;
    }

    public ILayer GetLayer(ObjectId layerId) {
        for(int i = 0; i < layers.size(); i++) {
            if(layers.get(i).ID() == layerId) {
                return layers.get(i);
            }
        }
        return null;
    }

    public Iterator<ILayer> GetLayerIterator() {
        return layers.iterator();
    }

    public Street GetStreet(ObjectId streetID) {
        return null;
    }

    public int TileCount(int zoomLevel) {
        return (int)Math.pow(1 << (zoomLevel + 1), 2);
    }

    public TileInfo GetTile(Point coord, int zoomLevel) {
        int colNum = 1 << (zoomLevel + 1);
        int i = (int)(Math.floor(Math.floor(coord.Y()) / 256));
        int j = (int)(Math.floor(Math.floor(coord.X()) / 256));
        return new TileInfo(tiles.get(zoomLevel)[i * colNum + j].Id,
                                      j * 256, i * 256, zoomLevel);
    }

    public void GetTiles(Region2D region, int zoomLevel, List<TileInfo> list) {
        int colNum = 1 << (zoomLevel + 1);
        int startRow = (int)(Math.floor(Math.floor(region.Top()) / 256));
        int startCol = (int)(Math.floor(Math.floor(region.Left()) / 256));
        int endRow = (int)(Math.ceil(Math.floor(region.Bottom()) / 256));
        int endCol = (int)(Math.ceil(Math.floor(region.Right()) / 256));

        for(int i = startRow; i < endRow; i++) {
            for(int j = startCol; j < endCol; j++) {
                list.add(new TileInfo(tiles.get(zoomLevel)[i * colNum + j].Id,
                                      j * 256, i * 256, zoomLevel));
            }
        }
    }

    public Image LoadTile(ObjectId tileId) {
        BufferedImage img = null;
        Tile tile = null;

        for(int i = 0; i < tiles1.length; i++) {
            if(tiles1[i].Id == tileId) {
                tile = tiles1[i];
                break;
            }
        }
        
        if(tile == null) {
            for(int i = 0; i < tiles2.length; i++) {
                if(tiles2[i].Id == tileId) {
                    tile = tiles2[i];
                    break;
                }
            }
        }

        if(tile == null) {
            for(int i = 0; i < tiles3.length; i++) {
                if(tiles3[i].Id == tileId) {
                    tile = tiles3[i];
                    break;
                }
            }
        }

        if(tile == null) {
            for(int i = 0; i < tiles4.length; i++) {
                if(tiles4[i].Id == tileId) {
                    tile = tiles4[i];
                    break;
                }
            }
        }

        if(tile == null) {
            for(int i = 0; i < tiles5.length; i++) {
                if(tiles5[i].Id == tileId) {
                    tile = tiles5[i];
                    break;
                }
            }
        }

        try {
            img = ImageIO.read(new File("D:\\mapImages\\" + tile.Path));
        } catch (IOException e) {
            return null;
        }

        return img;
    }

    public int NodeNumber() {
        return nodes.size();
    }

    public Iterator<Node> GetNodeIterator() {
        return nodes.values().iterator();
    }

    public Node GetNode(ObjectId nodeID) {
        return nodes.get(nodeID);
    }

    public int StreetCount() {
        return streets.size();
    }

    public Iterator<Street> GetStreetIterator() {
        return streets.iterator();
    }

    public Color StreetColor(StreetType streetType) {
        if(streetType == StreetType.Street) {
            return new Color(32, 192, 255, 255);
        }
        else if(streetType == StreetType.Avenue) {
            return new Color(255, 216, 0);
        }
        else return new Color(217, 25, 56);
    }

    public int MarkerCount(ObjectId id) {
        if(GetLayer(id).Name().equals("1")) return markers1.size();
        if(GetLayer(id).Name().equals("2")) return markers2.size();
        if(GetLayer(id).Name().equals("3")) return markers3.size();
        return 0;
    }

    public Image LoadMarkerIcon(ObjectId layerID) {
        try {
            return ImageIO.read(new File("C:\\markerImages\\" + layerID.Id() + ".png"));
        } catch (IOException e) {
            return null;
        }
   }

   public Iterator<Marker> GetMarkerIterator(ObjectId id) {
       if(GetLayer(id).Name().equals("1")) return markers1.iterator();
       if(GetLayer(id).Name().equals("2")) return markers2.iterator();
       if(GetLayer(id).Name().equals("3")) return markers3.iterator();
        return null;
    }

    public Marker GetMarker(ObjectId markerID, ObjectId layerID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void Load(String location) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void Save(String location) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
