package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class TestTileCache {


    @Test
    public void testCache() {

        int howMany = 10;

        TileCache c = new TileCache(howMany);
        ArrayList<Tile> tiles = new ArrayList<>();

        assertNull(c.get(1, 1*10, 1*100));

        // put
        for (int i = 0; i < howMany; ++i)
            tiles.add(new Tile(i, i*10, i*100, null));

        for (int i = 0; i < howMany; ++i)
            c.put(tiles.get(i));

        for (int i = 0; i < howMany; ++i)
            assertEquals(tiles.get(i), c.get(i, i*10, i*100));

        assertNotNull(c.get(9, 9*10, 9*100));
        assertNull(c.get(10, 10*10, 10*100));
    }
}
