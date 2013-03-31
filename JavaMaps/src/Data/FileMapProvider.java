// Copyright (c) 2010 Elena Bebeselea, Ramona Maris. All rights reserved.
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

package Data;
import Core.*;
import Rendering.Utils.MockProjection;
import java.io.*;
import java.util.*;
import java.lang.Exception;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.*;

public class FileMapProvider implements Serializable, IMapProvider {
    Map map;
    RoadLayer road;
    ImageLayer image;
    String parentDir;
    String imagesPath;
    String markersPath;
    String separator;

    /*
     ** Constructors.
     */
    public FileMapProvider() {
        map = new Map();
    }

    /*
     ** Public methods.
     */
    public Map Map() { return map; }

    // Map operations.
    // Deschide harta si verifica daca exista layere de anumit tip.
    // Daca exista, acestea vor fi salvate in membri interni pentru a nu fi
    // cautate de fiecare data.

    // Incarcare harta din fisier.
    public void Load(String location) throws IOException, IllegalStateException {
        try {
            FileInputStream fis = new FileInputStream(location);
            ObjectInputStream ois = new ObjectInputStream(fis);
            map = (Map)ois.readObject();
            
            File file = new File(location);
            parentDir = file.getParent();
            //Testam daca exista dosarul cu imagini ce formeaza harta.
            separator = File.separator;
            imagesPath = location.substring(0,location.lastIndexOf(separator)+1)
                          + "mapImages";
             markersPath = location.substring(0,location.lastIndexOf(separator)+1)
                           + "mapMarkers";
            File imagesDir = new File(imagesPath);
            if(!imagesDir.exists())
                 throw new IOException();
        }
        catch(Exception e){
            throw new IOException();
        }
        Iterator<ILayer> it = map.Iterator();
        while(it.hasNext()) {
            ILayer layer = it.next();
            if(layer.Type() == LayerType.Image)
                image = (ImageLayer)layer;
            if(layer.Type() == LayerType.Street)
                road = (RoadLayer)layer;
        }
        if(image == null) throw new IllegalStateException();
    }

    // Salvare harta in fisier.
    public void Save(String location) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(location);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject((Object)map);
        }
        catch(Exception e) {
            throw new IOException();
        }
    }


    // Map information.
    // Numele hartii.
    public String Name() { return map.Name(); }

    // Numarul maxim de nivele de zoom.
    public int ZoomLevels() { return image.ZoomLevels(); }

    // Regiunea acoperita de harta (in coordonate long/lat).
    public Region MapBounds() {
        Coordinates topLeft = new Coordinates(image.Start().Latitude(),
                                              image.Start().Longitude());
        Coordinates bottomRight = new Coordinates(image.End().Latitude(),
                                                 image.End().Longitude());
        return new Region(topLeft, bottomRight);
    }

    // Regiunea acoperita de harta (in pixeli) la nivelul de zoom dat.
    public Region2D MapBounds(int level) {
         return new Region2D(Point.Zero, 256 * image.Level(level).Columns(),
                            256 * image.Level(level).Rows());
    }

    // TODO
    // Proiectia folosita de harta.
    public IProjection Projection() {
        //return new MercatorProjection();
        return new MockProjection(this);
    }

    // Layers.

    // Numarul de layere.
    public int LayerCount() { return map.LayerNumber(); }

    // Exsitenta unui anumit tip de layer.
    public boolean HasLayer(LayerType layerType) {
        for(int i = 0; i < map.LayerNumber(); i++) {
            if( map.Layers().get(i).Type() == layerType) {
                return true;
            }
        }
        return false;
    }

    // Returneaza layer-ul corespondent Id-ului dat sau null daca nu exista.
    public ILayer GetLayer(ObjectId layerID) {
          for(int i = 0; i < map.LayerNumber(); i++) {
            ILayer layer = map.Layers().get(i);
            if(layer.ID() == layerID) {
                return layer;
            }
        }
        return null;
    }

    // Iterator pentru toate layerele hartii.
    public Iterator<ILayer> GetLayerIterator() { return map.Iterator(); }


    // Tiles.

    // Numarul de layere.
    public int TileCount(int level) throws IndexOutOfBoundsException {
        if(level >= 0 || level <= ZoomLevels())
            return image.Level(level).Tiles().length;
        else throw new IndexOutOfBoundsException();
    }
 
    // Imaginea (tile) care contine punctul dat, la nivelul de zoom specificat.
    public TileInfo GetTile(Point point, int zoomLevel) throws IndexOutOfBoundsException {
        if(zoomLevel >= 0 || zoomLevel <= ZoomLevels()) {
            int pointCol = (int)point.X() / 256;
            int pointRow = (int)point.Y() / 256;
            Tile tile = image.Level(zoomLevel).GetTile(pointRow, pointCol);
            return new TileInfo(tile.Id(), pointCol * 256, pointRow * 256);
        }
        else throw new IndexOutOfBoundsException();
    }
    
    // Imaginea asociata identificatorului.
    public Image LoadTile(ObjectId tileID) {
        BufferedImage img  = null;
        Tile tile = null;
        boolean found = false;
        // Se parcurg array-urile de Tiles[] pentru toate nivelurile de zoom
        // disponibile.
        for(int i = 0; i < ZoomLevels() && !found; i++) {
            for(int j = 0; j < image.Level(i).Tiles().length; j++) {
                if(image.Level(i).GetTile(j).Id().equals(tileID)) {
                    tile = image.Level(i).GetTile(j);
                    found = true;
                    break;
                }
            }
        }
        try {
            img = ImageIO.read(new File(imagesPath + separator +
                               tile.FileName()));
        }
        catch (IOException e) {
            return null;
        }
        return img;

    }
    
    // Adauga in lista specificata obiecte TileInfo pentru toate imaginile care
    // se intersecteaza cu regiunea si se afla la nivelul de zoom dat.
    public void GetTiles(Region2D region, int zoomLevel, List<TileInfo> list) {
        int startRow = (int)(Math.floor(Math.floor(region.Top()) / 256));
        int startCol = (int)(Math.floor(Math.floor(region.Left()) / 256));
        int endRow = (int)(Math.ceil(Math.floor(region.Bottom()) / 256));
        int endCol = (int)(Math.ceil(Math.floor(region.Right()) / 256));

       for(int i = startRow; i < endRow; i++) {
            for(int j = startCol; j < endCol; j++) {
                Tile tile = image.Level(zoomLevel).GetTile(i, j);
                list.add(new TileInfo(tile.Id(), j * 256, i * 256, zoomLevel));
            }
        }
    }

    // Dimensiunea in pixeli a unei imagini.
    public double TileSize() {
        return 256.0;
    }
   

    // Nodes.
    
    // Numarul de noduri.
    public int NodeNumber() throws UnsupportedOperationException {
       if(road != null) {
            return road.NodeCount();
       }
       else throw new UnsupportedOperationException();
    }
    // Iterator pentru toate nodurile hartii.
    public Iterator<Node> GetNodeIterator() {
        if(road != null) {
            return road.IteratorNode();
        }
        else throw new UnsupportedOperationException();
    }
    // Nodul asociat identificatorului primit.
    public Node GetNode(ObjectId nodeId) {
         if(road != null) {
            Iterator<Node> i = road.IteratorNode();
            while(i.hasNext()){
                Node node = i.next();
                if(node.Id().Id() == nodeId.Id()) return node;
            }
            return null;
         }
        else throw new UnsupportedOperationException();
     }

    
    // Streets.

    //Numarul de strazi
    public int StreetCount() throws UnsupportedOperationException{
        if(road != null){
            return road.StreetCount();
        }
        else throw new UnsupportedOperationException();
    }

    // Iterator pentru toate strazile hartii.
    public Iterator<Street> GetStreetIterator(){
        if(road != null){
            return road.IteratorStreet();
        }
        else throw new UnsupportedOperationException();
    }

    // Strada asociata identificatorului primit.
    public Street GetStreet(ObjectId id){
        if(road != null){
            Iterator<Street> i = road.IteratorStreet();
            while(i.hasNext()){
                Street street = i.next();
                if(street.Id().Id() == id.Id()) return street;
            }
            return null;
        }
        else throw new UnsupportedOperationException();
    }

    // Culoarea asociata tipului de strada.
    public Color StreetColor(StreetType streetType){
        if(road != null){
            if(streetType == StreetType.Street) {
                return road.StreetColor();
            }
            if(streetType == StreetType.Avenue) {
                return road.AvenueColor();
            }
            if(streetType == StreetType.Boulevard){
                return road.BoulevardColor();
            }
            return null;
        }
        else throw new UnsupportedOperationException();
    }

    // Markers.

    // Numarul de markere asociate layer-ului avand asociat un anumit
    // identifcator.
    public int MarkerCount(ObjectId layerID) throws IllegalArgumentException {
        ILayer layer = GetLayer(layerID);
        if(layer != null && layer.Type() == LayerType.Marker) {
            MarkerLayer markerLayer = (MarkerLayer)layer;
            return markerLayer.MarkerNumber();
        }
        else throw new IllegalArgumentException();
    }

    // Iterator pentru toate marker-ele definite in layer-ul specificat.
    public Iterator<Marker> GetMarkerIterator(ObjectId layerID) throws
                                             IllegalArgumentException {
        ILayer layer = GetLayer(layerID);
        if(layer != null && layer.Type() == LayerType.Marker) {
            MarkerLayer markerLayer = (MarkerLayer)layer;
            return markerLayer.Iterator();
        }
        else throw new IllegalArgumentException();
    }

    // Marker-ul de pe un anumit layer, asociat identificatorului.
    public Marker GetMarker(ObjectId markerID, ObjectId layerID) throws
                                             IllegalArgumentException {
        ILayer layer = GetLayer(layerID);
        if((layer != null) && (layer.Type() == LayerType.Marker)) {
            MarkerLayer markerLayer = (MarkerLayer)layer;
            return markerLayer.GetMarker(markerID);
        }
        else throw new IllegalArgumentException();
    }

    // Imaginea asociata marker-ului avand identificatorul specificat.
    public Image LoadMarkerIcon(ObjectId layerID) throws IOException {
        BufferedImage img = null;
        try {
            ILayer layer = GetLayer(layerID);
            if(layer != null && layer.Type() == LayerType.Marker) {
                MarkerLayer markerLayer = (MarkerLayer)layer;
                img = ImageIO.read(new File(markersPath + separator
                                            + markerLayer.Icon()));
            }
        }
        catch(Exception e) {
            throw new IOException();
        }
        return img;
    }

    
    // Pentru verificare.
    public void AddLayer(ILayer element) {
        map.AddLayer(element);
    }        
}
