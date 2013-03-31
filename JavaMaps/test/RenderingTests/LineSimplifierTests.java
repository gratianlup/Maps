// javamaps
// Copyright (c) 2010 Lup Gratian
package RenderingTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import Core.*;
import Rendering.Utils.*;
import java.util.*;

public class LineSimplifierTests {
    @Test
    public void Simplification() {
        ArrayList<Point> points = new ArrayList<Point>();
        ArrayList<Point> result = new ArrayList<Point>();

        // Puncte pe aceasi linie.
        points.add(new Point(0, 0));
        points.add(new Point(1, 1));
        points.add(new Point(2, 2));
        points.add(new Point(3, 3));
        LineSimplifier.Simplify(points, 1, result);
        assertEquals(2, result.size());
        assertTrue(result.get(0) == points.get(0));
        assertTrue(result.get(1) == points.get(3));

        // Distanta 2.
        points.clear();
        result.clear();
        points.add(new Point(1, 1));
        points.add(new Point(2, 2.5));
        points.add(new Point(4, 8));
        points.add(new Point(5, 7));
        points.add(new Point(6, 7));
        LineSimplifier.Simplify(points, 2, result);
        assertEquals(3, result.size());
        assertTrue(result.get(0) == points.get(0));
        assertTrue(result.get(1) == points.get(2));
        assertTrue(result.get(2) == points.get(4));

        // Distanta 2, saw-tooth.
        points.clear();
        result.clear();
        points.add(new Point(0, 0));
        points.add(new Point(2, 2));
        points.add(new Point(4, 0));
        points.add(new Point(6, -2));
        points.add(new Point(8, 0));
        points.add(new Point(10, 2));
        points.add(new Point(12, 0));
        LineSimplifier.Simplify(points, 2, result);
        assertEquals(5, result.size());
        assertTrue(result.get(0) == points.get(0));
        assertTrue(result.get(1) == points.get(1));
        assertTrue(result.get(2) == points.get(3));
        assertTrue(result.get(3) == points.get(5));
        assertTrue(result.get(4) == points.get(6));

        // Distanta 6, saw-tooth.
        result.clear();
        LineSimplifier.Simplify(points, 4, result);
        assertEquals(2, result.size());
        assertTrue(result.get(0) == points.get(0));
        assertTrue(result.get(1) == points.get(6));
    }
}