// javamaps
// Copyright (c) 2010 Lup Gratian
package RenderingTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import Rendering.Utils.*;

public class LineUtilsTests {
    @Test
    public void LineIntersection() {
        assertTrue(LineUtils.LinesIntersect(0, 0, 4, 4, 0, 4, 4, 0));
        assertTrue(LineUtils.LinesIntersect(2, 2, 5, 5, 2, 2.5, 4, 4));
        assertFalse(LineUtils.LinesIntersect(1, 1, 4, 4, 0, 0, -1, -8));
    }

    @Test
    public void PointLineDistance() {
        assertEquals(2, LineUtils.PointLineDistance(2, 2, 4, 0, 4, 6), 0.01);
        assertEquals(1.41, LineUtils.PointLineDistance(0, 0, 0, 2, 2, 0), 0.01);
        assertEquals(2, LineUtils.PointLineDistance(2, 8, 2, 2, 2, 6), 0.01);
    }
}