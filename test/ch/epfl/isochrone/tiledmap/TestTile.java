package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

import org.junit.Test;

public class TestTile {

    @Test(expected=IllegalArgumentException.class)
    public void testTileConstructorWrongZoom() {
        new Tile(-1, 0, 0, null);
    }

    @Test
    public void testTileConstructor() {
        new Tile(0, 0, 0, null);
        assertEquals(1, 1);
    }

    @Test
    public void testImage() {
        BufferedImage i = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Tile t = new Tile(0, 0, 0, i);
        assertEquals(i, t.image());
    }

}
