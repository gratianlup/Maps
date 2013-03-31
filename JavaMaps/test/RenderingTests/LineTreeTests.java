
package RenderingTests;
import Core.*;
import Rendering.Utils.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class LineTreeTests {
    private static ObjectId[] objects_;

    @BeforeClass
    public static void setUpClass() throws Exception {
        objects_ = new ObjectId[] {
            ObjectId.NewId(),
            ObjectId.NewId(),
            ObjectId.NewId(),
            ObjectId.NewId(),
            ObjectId.NewId(),
            ObjectId.NewId()
        };
    }

    @Test
    public void Add() {
        LineTree tree = new LineTree(100, 100, 2);
        tree.Add(new Line(10, 10, 20, 20, objects_[0]), 0);
        tree.Add(new Line(10, 10, 80, 80, objects_[1]), 0);
        tree.Add(new Line(60, 60, 70, 70, objects_[2]), 0);
        tree.Add(new Line(20, 80, 30, 90, objects_[3]), 0);
        tree.Add(new Line(45, 45, 55, 55, objects_[4]), 0);
        tree.Add(new Line(0, 0, 0, 100, objects_[5]), 0);
    }

    private boolean LineInList(LineCollection list, ObjectId id) {
        Object[] items = list.Objects().toArray();
        for(int i = 0; i < items.length; i++) {
            if(((Line)items[i]).Value() == id) return true;
        }
        return false;
    }

    @Test
    public void Intersect() {
        LineTree tree = new LineTree(100, 100, 3);
        tree.Add(new Line(10, 10, 20, 20, objects_[0]), 0);
        tree.Add(new Line(15, 15, 25, 25, objects_[1]), 1);
        tree.Add(new Line(60, 60, 80, 80, objects_[2]), 1);
        tree.Add(new Line(10, 10, 20, 80, objects_[3]), 0);
        tree.Add(new Line(70, 10, 20, 60, objects_[4]), 1);
        tree.Add(new Line(80, 80, 75, 70, objects_[5]), 2);

        // Colt NE.
        LineCollection list1 = new LineCollection();
        tree.Intersect(new Region2D(50, 0, 50, 50), 0, list1);
        assertEquals(0, list1.Count());

        // Colt NW, zoom 0.
        list1.Clear();
        tree.Intersect(new Region2D(0, 0, 50, 50), 0, list1);
        assertEquals(2, list1.Count());
        assertTrue(LineInList(list1, objects_[0]));
        assertTrue(LineInList(list1, objects_[3]));

        // Colt NW, zoom 2.
        list1.Clear();
        tree.Intersect(new Region2D(0, 0, 50, 50), 2, list1);
        assertEquals(0, list1.Count());

        list1.Clear();
        tree.Intersect(new Region2D(60, 20, 85, 90), 2, list1);
        assertEquals(1, list1.Count());
        assertTrue(LineInList(list1, objects_[5]));
    }

    @Test
    public void NearestLine() {
        LineTree tree = new LineTree(100, 100, 3);
        assertNull(tree.NearestLine(Point.Zero, 0));

        tree.Add(new Line(10, 10, 20, 20, objects_[0]), 0);
        tree.Add(new Line(60, 60, 80, 80, objects_[1]), 0);
        tree.Add(new Line(80, 80, 75, 70, objects_[2]), 0);

        LineTree.NearestInfo nearest = tree.NearestLine(new Point(0, 0), 0);
        assertEquals(nearest.Line().Value(), objects_[0]);
        nearest = tree.NearestLine(new Point(70, 70), 0);
        assertEquals(nearest.Line().Value(), objects_[1]);
        nearest = tree.NearestLine(new Point(50, 50), 0);
        assertEquals(nearest.Line().Value(), objects_[1]);
    }
}