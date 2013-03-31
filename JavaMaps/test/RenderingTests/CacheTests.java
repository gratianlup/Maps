// javamaps
// Copyright (c) 2010 Lup Gratian
package RenderingTests;

import Rendering.Utils.Cache;
import Core.*;
import Rendering.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CacheTests {
    class TestObject {
        private ObjectId objId_;
        
        public TestObject(ObjectId id) {
            objId_ = id;
        }

        public ObjectId Id() { return objId_; }
        public Point Position() { return Point.Zero; }
        public ObjectId LayerId() { return objId_; }
        public int ZoomLevel() { return 0; }
    }

    private static ObjectId[] ids_;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ids_ = new ObjectId[] {
            ObjectId.NewId(), ObjectId.NewId(),
            ObjectId.NewId(), ObjectId.NewId(),
            ObjectId.NewId(), ObjectId.NewId()
        };
    }

    @Test
    public void Add() {
        Cache<TestObject> cache = new Cache<TestObject>(4);
        assertEquals(4, cache.Capacity());

        // Obiect care nu este in cache.
        cache.Add(new TestObject(ids_[0]), ids_[0]);
        assertEquals(1, cache.Count());
        cache.Add(new TestObject(ids_[1]), ids_[1]);
        assertEquals(2, cache.Count());

        // Obiect care este in cache.
        cache.Add(new TestObject(ids_[0]), ids_[0]);
        assertEquals(2, cache.Count());

        // Cache plin; primul element adaugat sters.
        cache.Add(new TestObject(ids_[2]), ids_[2]);
        cache.Add(new TestObject(ids_[3]), ids_[3]);
        cache.Add(new TestObject(ids_[4]), ids_[4]);
        assertEquals(4, cache.Count());
    }

    @Test
    public void Get() {
        Cache<TestObject> cache = new Cache<TestObject>(4);
        cache.Add(new TestObject(ids_[0]), ids_[0]);
        cache.Add(new TestObject(ids_[1]), ids_[1]);
        cache.Add(new TestObject(ids_[2]), ids_[2]);

        // Obiecte in cache.
        assertNotNull(cache.Get(ids_[0]));
        assertNotNull(cache.Get(ids_[2]));

        // Obiect care nu este in cache.
        assertNull(cache.Get(ids_[5]));
    }

    @Test
    public void Contains() {
        Cache<TestObject> cache = new Cache<TestObject>(4);
        cache.Add(new TestObject(ids_[0]), ids_[0]);

        assertTrue(cache.Contains(ids_[0]));
        assertFalse(cache.Contains(ids_[3]));
    }

    @Test
    public void Remove() {
        Cache<TestObject> cache = new Cache<TestObject>(4);
        cache.Add(new TestObject(ids_[0]), ids_[0]);
        cache.Add(new TestObject(ids_[1]), ids_[1]);

        cache.Remove(ids_[1]);
        assertEquals(1, cache.Count());
        cache.Remove(ids_[0]);
        assertEquals(0, cache.Count());
    }

    @Test
    public void CacheEviction() {
        Cache<TestObject> cache = new Cache<TestObject>(4);
        cache.Add(new TestObject(ids_[0]), ids_[0]);
        cache.Add(new TestObject(ids_[1]), ids_[1]);
        cache.Add(new TestObject(ids_[2]), ids_[2]);
        cache.Add(new TestObject(ids_[3]), ids_[3]);

        cache.Add(new TestObject(ids_[4]), ids_[4]);
        assertFalse(cache.Contains(ids_[0]));
        cache.Get(ids_[1]);
        cache.Get(ids_[2]);
        cache.Add(new TestObject(ids_[5]), ids_[5]);
        assertFalse(cache.Contains(ids_[3]));
    }
}