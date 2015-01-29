package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class TestOSMTileProvider {

    @Test(expected=IllegalArgumentException.class)
    public void testTileAtWrongZoom() throws MalformedURLException {
        OSMTileProvider p = new OSMTileProvider(new URL("http://a.tile.openstreetmap.org/"));
        p.tileAt(-1, 0, 0);
    }

    @Test
    public void testTileAt() throws MalformedURLException {
        OSMTileProvider p = new OSMTileProvider(new URL("http://a.tile.openstreetmap.org/"));
        Tile t = p.tileAt(0, 0, 0);
        assertNotNull(t);
    }

}
