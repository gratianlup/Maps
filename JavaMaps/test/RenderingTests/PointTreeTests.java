// javamaps
// Copyright (c) 2010 Lup Gratian
package RenderingTests;
import Rendering.IVisual;
import Rendering.Utils.*;
import Core.*;
import Rendering.*;
import java.awt.Graphics2D;
import java.util.*;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class PointTreeTests {
    static class TestObject implements IVisual {
        private ObjectId objId_;
        private Point position_;

        public TestObject(ObjectId id, Point point) {
            objId_ = id;
            position_ = point;
        }

        public ObjectId Id() { return objId_; }
        public Point Position() { return position_; }
        public ObjectId LayerId() { return objId_; }
        public int ZoomLevel() { return 0; }

        public void Draw(Graphics2D g, View view) {}
    }

    private static TestObject[] objects_;

    @BeforeClass
    public static void setUpClass() throws Exception {
        objects_ = new TestObject[] {
            new TestObject(ObjectId.NewId(), new Point(20, 20)),
            new TestObject(ObjectId.NewId(), new Point(35, 20)),
            new TestObject(ObjectId.NewId(), new Point(70, 70)),
            new TestObject(ObjectId.NewId(), new Point(8, 8)),
            new TestObject(ObjectId.NewId(), new Point(15, 15)),
            new TestObject(ObjectId.NewId(), new Point(40, 40))
        };
    }

    @Test
    public void Constructors() {
        // Constructor implicit.
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 200);
        assertEquals(0, tree.Count());
        assertEquals(100, tree.Width(), 0.0001);
        assertEquals(200, tree.Height(), 0.0001);
        
        // Constructor care primeste o lista.
    }

    @Test
    public void Add() {
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 100);

        tree.Add(objects_[0]);
        assertEquals(1, tree.Count());
        tree.Add(objects_[1]);
        assertEquals(2, tree.Count()); // Nu se pot adauga obiecte cu aceasi coord.
        tree.Add(objects_[1]);
        assertEquals(3, tree.Count());
        tree.Add(objects_[2]);
        assertEquals(4, tree.Count()); // Nu se pot adauga obiecte cu aceasi coord.
        tree.Add(objects_[3]);
        tree.Add(objects_[4]);
        tree.Add(objects_[5]);
    }

    @Test
    public void Intersect() {
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 100);
        tree.Add(objects_[0]);
        tree.Add(objects_[1]);
        tree.Add(objects_[2]);
        tree.Add(objects_[3]);
        tree.Add(objects_[4]);
        tree.Add(objects_[5]);

        // Suprafata vida.
        BasicCollection<TestObject> list = new BasicCollection<TestObject>();
        tree.Intersect(new Region2D(2, 2, 0, 0), list);
        assertEquals(0, list.Count());

        // Toata suprafata.
        tree.Intersect(new Region2D(0, 0, 100, 100), list);
        assertEquals(6, list.Count());
        assertTrue(list.Objects().contains(objects_[0]));
        assertTrue(list.Objects().contains(objects_[1]));
        assertTrue(list.Objects().contains(objects_[2]));
        assertTrue(list.Objects().contains(objects_[3]));
        assertTrue(list.Objects().contains(objects_[4]));
        assertTrue(list.Objects().contains(objects_[5]));

        // Colt NE.
        list.Clear();
        tree.Intersect(new Region2D(50, 0, 50, 50), list);
        assertEquals(0, list.Count());

        // Colt SW.
        list.Clear();
        tree.Intersect(new Region2D(0, 50, 50, 50), list);
        assertEquals(0, list.Count());

        // Colt SE.
        list.Clear();
        tree.Intersect(new Region2D(50, 50, 50, 50), list);
        assertEquals(1, list.Count());
        assertTrue(list.Objects().contains(objects_[2]));

        // Colt NW.
        list.Clear();
        tree.Intersect(new Region2D(0, 0, 50, 50), list);
        assertEquals(5, list.Count());
        assertTrue(list.Objects().contains(objects_[0]));
        assertTrue(list.Objects().contains(objects_[1]));
        assertTrue(list.Objects().contains(objects_[3]));
        assertTrue(list.Objects().contains(objects_[4]));
        assertTrue(list.Objects().contains(objects_[5]));
    }

    @Test
    public void Find() {
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 100);
        tree.Add(objects_[0]);
        tree.Add(objects_[1]);
        tree.Add(objects_[2]);

        // Puncte inexistente.
        assertNull(tree.Find(new TestObject(ObjectId.NewId(), Point.Zero)));
        assertNull(tree.Find(new TestObject(ObjectId.NewId(), new Point(60, 60))));

        // Puncte existente.
        assertNotNull(tree.Find(objects_[0]));
        assertNotNull(tree.Find(objects_[1]));
        assertNotNull(tree.Find(objects_[2]));
    }

    private boolean PointInList(BasicCollection<TestObject> list,
                                TestObject value) {
        return list.Objects().contains(value);
    }

    @Test
    public void Near() {
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 100);
        tree.Add(objects_[0]);
        tree.Add(objects_[1]);
        tree.Add(objects_[2]);
        tree.Add(objects_[3]);
        tree.Add(objects_[4]);

        BasicCollection<TestObject> list = new BasicCollection<TestObject>();
        tree.Near(new Point(21, 21), 9, list);
        assertEquals(2, list.Count());
        assertTrue(PointInList(list, objects_[0]));
        assertTrue(PointInList(list, objects_[4]));

        list.Clear();
        tree.Near(new Point(21, 21), 6, list);
        assertEquals(1, list.Count());
        assertTrue(PointInList(list, objects_[0]));

        // Intreaga suprafata.
        list.Clear();
        tree.Near(Point.Zero, 200, list);
        assertEquals(5, list.Count());
        assertTrue(PointInList(list, objects_[0]));
        assertTrue(PointInList(list, objects_[1]));
        assertTrue(PointInList(list, objects_[2]));
        assertTrue(PointInList(list, objects_[3]));
        assertTrue(PointInList(list, objects_[4]));

        // Nici un punct suficient de apropiat
        list.Clear();
        tree.Near(new Point(50, 50), 5, list);
        assertEquals(0, list.Count());
    }

    @Test
    public void NearestPoint() {
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 100);
        tree.Add(objects_[0]);
        tree.Add(objects_[1]);
        tree.Add(objects_[2]);

        assertEquals(0.0, tree.NearestPoint(new Point(20, 20)).Distance(), 0.01);
        assertEquals(28.28, tree.NearestPoint(new Point(0, 0)).Distance(), 0.01);
    }

    @Test
    public void Remove() {
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 100);
        tree.Add(objects_[0]);
        tree.Add(objects_[1]);
        tree.Add(objects_[2]);
        tree.Add(objects_[3]);
        tree.Add(objects_[4]);
        
        // Punct care nu este in arbore.
        tree.Remove(new TestObject(ObjectId.NewId(), Point.Zero));
        assertEquals(5, tree.Count());

        tree.Remove(objects_[0]);
        assertEquals(4, tree.Count());
        tree.Remove(objects_[1]);
        assertEquals(3, tree.Count());
        tree.Remove(objects_[2]);
        assertEquals(2, tree.Count());
        tree.Remove(objects_[3]);
        assertEquals(1, tree.Count());
        tree.Remove(objects_[4]);
        assertEquals(0, tree.Count());
    }

    @Test
    public void Clear() {
        PointTree<TestObject> tree = new PointTree<TestObject>(100, 100);
        tree.Add(objects_[0]);

        tree.Clear();
        assertEquals(0, tree.Count());
    }
}