package RenderingTests;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.*;
import javax.swing.*;
import Rendering.Utils.*;
import Rendering.IViewerHost;
import Rendering.MapViewer;
import Core.*;
import Data.FileMapProvider;
import Data.RoadLayer;
import Rendering.ActionType;
import Rendering.Editor.LineEditor;
import Rendering.Editor.NodeEditor;
import Rendering.Editor.MarkerEditor;
import Rendering.IObjectInfo;
import Rendering.IRenderer;
import Rendering.Info.MarkerInfo; 
import Rendering.Info.LinkInfo;
import Rendering.Info.NodeInfo;
import Rendering.Info.StreetInfo;
import Rendering.Info.StreetPointInfo;
import Rendering.InfoType;
import Rendering.Renderers.StreetRenderer;
import java.awt.event.KeyListener;
import java.lang.Thread;
import Rendering.Renderers.MarkerRenderer;
import com.sun.corba.se.impl.encoding.CodeSetConversion.CTBConverter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Gratian
 */
public class ImageRendererTests {
    class TestHost extends JFrame implements IViewerHost, KeyListener {
        MapViewer viewer_;
        boolean debug_;
        IObjectInfo selected_;
        LineEditor lineEditor_;
        NodeEditor nodeEditor_;
        StreetRenderer streetRend_;
        MarkerEditor markerEditor_;
        RoadLayer road_;
        boolean prefetch_;
        
        void SetViewer(MapViewer viewer) {
            viewer_ = viewer;
            this.addKeyListener(this);
        }

        private void CreateLineEditor() {
            if (lineEditor_ == null) {
                Iterator<IRenderer> rendIt = viewer_.Renderers();
                while (rendIt.hasNext()) {
                    IRenderer r = rendIt.next();
                    if (r.Layer().Type() == LayerType.Street) {
                        streetRend_ = (StreetRenderer) r;
                        road_ = (RoadLayer)r.Layer();
                        lineEditor_ = new LineEditor(streetRend_, viewer_);
                        viewer_.AddRenderer(lineEditor_);
                        break;
                    }
                }
            }
            
            HideEditors();
            streetRend_.SetOpacity(1.0);
            lineEditor_.SetVisible(true);
            viewer_.Repaint();
        }

        private void CreateNodeEditor() {
            HideEditors();
            if (nodeEditor_ == null) {
                CreateLineEditor();
                nodeEditor_ = new NodeEditor(lineEditor_, viewer_);
                viewer_.AddRenderer(nodeEditor_);
            }

            HideEditors();
            nodeEditor_.SetOpacity(1.0);
            nodeEditor_.SetVisible(true);
            viewer_.Repaint();
        }

        private void HideEditors() {
            if(lineEditor_ != null) lineEditor_.SetVisible(false);
            if(nodeEditor_ != null) nodeEditor_.SetVisible(false);
            if(markerEditor_ != null) markerEditor_.SetVisible(false);
        }

        public void keyTyped(KeyEvent e) {
            
        }

        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                viewer_.Pan(new Core.Point(-512, 0), 500);
            }
            else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                viewer_.Pan(new Core.Point(512, 0), 500);
            }
            else if(e.getKeyCode() == KeyEvent.VK_UP) {
                viewer_.Pan(new Core.Point(0, -512), 500);
            }
            else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                viewer_.Pan(new Core.Point(0, 512), 500);
            }
            else if(e.getKeyCode() == KeyEvent.VK_D) {
                viewer_.SetDebug(!debug_);
                debug_ = !debug_;
            }
            else if(e.getKeyCode() == KeyEvent.VK_P) {
                viewer_.EnablePrefetchers(prefetch_);
                prefetch_ = !prefetch_;
            }
            else if(e.getKeyCode() == KeyEvent.VK_S) {
                CreateLineEditor();
            }
            else if(e.getKeyCode() == KeyEvent.VK_N) {
                CreateNodeEditor();
            }
            else if(e.getKeyCode() == KeyEvent.VK_M) {                
                if(markerEditor_ == null) {
                    CreateNodeEditor();
                    
                    ArrayList<MarkerRenderer> markerRend = new ArrayList<MarkerRenderer>();
                    Iterator<IRenderer> rendIt = viewer_.Renderers();
                    while(rendIt.hasNext()) {
                        IRenderer test = rendIt.next();
                        if(test.Layer() != null) {
                            if(test.Layer().Type() == LayerType.Marker) {
                                markerRend.add((MarkerRenderer)test);
                            }
                        }
                    }

                    markerEditor_ = new MarkerEditor(markerRend, nodeEditor_, viewer_);
                    viewer_.AddRenderer(markerEditor_);
                }

                HideEditors();
                markerEditor_.SetVisible(true);
                markerEditor_.SetOpacity(1.0);
                viewer_.Repaint();
            }
            else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
                if(selected_ == null) return;
                if(selected_.Type() == InfoType.StreetPoint) {
                    StreetPointInfo spi = (StreetPointInfo)selected_;
                    spi.Street().Coordinates().remove(spi.PointIndex());
                    lineEditor_.RemovePoint(spi.Street(), spi.PointIndex());
                }
                else if(selected_.Type() == InfoType.Street) {
                    StreetInfo s = (StreetInfo)selected_;
                    road_.DeleteStreet(s.Street().Id());
                    lineEditor_.RemoveStreet(s.Street());
                }
                else if(selected_.Type() == InfoType.Node) {
                    NodeInfo n = (NodeInfo)selected_;
                    road_.DeleteNode(n.Node().Id());
                    nodeEditor_.RemoveNode(n.Node());
                }
                else if(selected_.Type() == InfoType.Link) {
                    LinkInfo l = (LinkInfo)selected_;
                    
                    nodeEditor_.RemoveLink(l.Link(), l.ParentNode());
                }
                else if(selected_.Type() == InfoType.Marker) {
                    MarkerInfo m = (MarkerInfo)selected_;
                    markerEditor_.RemoveMarker(m.Marker(), m.Layer());
                }
            }
            else if(e.getKeyCode() == KeyEvent.VK_B) {
                try {
                    // Salveaza harta.
                    ((FileMapProvider) viewer_.MapProvider()).Save("C:\\test.map");
                } catch (IOException ex) {
                    Logger.getLogger(ImageRendererTests.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else if(e.getKeyCode() == KeyEvent.VK_K) {
                if(selected_ != null && selected_.Type() == InfoType.Street) {
                    StreetInfo si = (StreetInfo)selected_;
                    if(si.Street().Type() == StreetType.Street) {
                        si.Street().SetType(StreetType.Avenue);
                    }
                    else if(si.Street().Type() == StreetType.Avenue) {
                        si.Street().SetType(StreetType.Boulevard);
                    }
                    else if(si.Street().Type() == StreetType.Boulevard) {
                        si.Street().SetType(StreetType.Street);
                    }
                    viewer_.Repaint();
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            
        }

        public void ActionPerformed(Action action) {
        }

        // TODO: Poate ar trebui alt nume, intra in conflict cu Swing...
        public void ActionPerformed(Rendering.Action action) {
            //System.out.println(action.toString());

            if(action.Type() == ActionType.ObjectMoved) {
                if(action.ObjectInfo().Type() == InfoType.StreetPoint) {
                    StreetPointInfo spi = (StreetPointInfo)action.ObjectInfo();
                    spi.Street().Coordinates().set(spi.PointIndex(), spi.Coordinates());
                }
                else if(action.ObjectInfo().Type() == InfoType.Node) {
                    NodeInfo ni=  (NodeInfo)action.ObjectInfo();
                    ni.Node().SetCoordinates(ni.Coordinates());
                }
            }
            if(action.Type() == ActionType.ObjectAdded) {
                if(action.ObjectInfo().Type() == InfoType.StreetPoint) {
                    StreetPointInfo spi = (StreetPointInfo)action.ObjectInfo();
                    spi.Street().Coordinates().add(spi.PointIndex(), spi.Coordinates());
                }
                else if(action.ObjectInfo().Type() == InfoType.Street) {
                    StreetInfo si = (StreetInfo)action.ObjectInfo();
                    road_.AddStreet(si.Street());
                }
                else if(action.ObjectInfo().Type() == InfoType.Node) {
                    NodeInfo ni = (NodeInfo)action.ObjectInfo();
                    road_.AddNode(ni.Node());
                }
                else if(action.ObjectInfo().Type() == InfoType.Link) {
                    LinkInfo li = (LinkInfo)action.ObjectInfo();
                    li.ParentNode().AddLink(li.Link());
                }
            }
            else if(action.Type() == ActionType.ObjectSelected) {
                 selected_ = action.ObjectInfo();
            }
            //action.SetValid(false);
        }
    }

    @Test
    public void Test() throws IOException {
        FileMapProvider p = new FileMapProvider();
        p.Load("C:\\test.map");
        TestHost t = new TestHost();

        // Adauga nume.
        String[] names = new String[] {
            "Andrei Saguna",
            "Independentei",
            "Iasilor",
            "Cuza voda",
            "Bdul. Memorandumului",
            "Croitorilor",
            "Crisan",
            "George Baritiu",
            "Closca",
            "Avram Iancu",
            "M. Kogalniceanu",
            "Ion I.C. Bratianu",
            "Nicolae Titulescu",
            "Minerilor",
            "Baladei",
            "Nasaud",
            "Aurel Suciu",
            "Actorului",
            "Rasaritului",
            "Patriciu Barbu",
            "Gorunului",
            "Buftea",
            "Calan",
            "Anina",
            "Garoafelor",
            "Bulgarilor",
            "Teleorman",
            "Oltului",
            "Dunarii",
            "Traian Vuia",
            "Plevnei",
            "Morii",
            "Luncii",
            "Oasului",
            "Corneliu Coposu",
            "Grigore Alexandrescu",
            "Bucium",
            "Ravasului",
            "Floresti",
            "Paraului",
            "Plopilor",
            "Stadionului",
            "George Cosbuc",
            "Hasdeu",
            "Ion Creanga",
            "Averlna",
            "Constantin Brancusi",
            "Fragului",
            "Becas",
            "Crizantemelor",
            "Vasile Lupu",
            "Valeriu Bologa",
            "Tarnavelor",
            "Muresului",
            "Oltului",
            "Cojocnel"
        };

//        Iterator<Street> streetIt = p.GetStreetIterator();
//        int ct = 0;
//
//        while(streetIt.hasNext()) {
//            streetIt.next().SetName(names[ct++]);
//        }

        MapViewer viewer = new MapViewer(t);
        t.SetViewer(viewer);
        viewer.LoadMap(p);
        JFrame frame = WindowUtilities.openInJFrame(t, viewer, 1024, 768);
        boolean moved = false;
        
        while(frame.isVisible()) {
            try {
                Thread.sleep(500);
            } catch(InterruptedException ex) {
                Logger.getLogger(ImageRendererTests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}