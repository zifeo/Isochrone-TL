package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class TestCachedTileProvider {

    @Test(expected=IllegalArgumentException.class)
    public void testTileAtWrongZoom() throws MalformedURLException {
        OSMTileProvider p = new OSMTileProvider(new URL("http://a.tile.openstreetmap.org/"));
        CachedTileProvider pc = new CachedTileProvider(p);
        pc.tileAt(-1, 0, 0);
    }

    @Test
    public void testTileAt() throws MalformedURLException {

        int howMany = 10;

        OSMTileProvider p = new OSMTileProvider(new URL("http://fakeopenstreetmap.beurre/"));
        CachedTileProvider pc = new CachedTileProvider(p);

        long start = System.nanoTime();
        for (int i = 0; i < howMany; ++i)
            pc.tileAt(i, i*10, i*100);
        long end = System.nanoTime() - start;

        long start2 = System.nanoTime();
        for (int i = 0; i < howMany; ++i)
            pc.tileAt(i, i*10, i*100);
        long end2 = System.nanoTime() - start2;

        assertTrue(end*howMany > end2);
    }
}
