package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

public class TestTransparentTileProvider {

    @Test(expected = IllegalArgumentException.class)
    public void TestTransparentTileProviderWrongOpacity() {
        new TransparentTileProvider(null, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void TestTransparentTileProviderWrongOpacity2() {
        new TransparentTileProvider(null, 2);
    }

    @Test
    public void TestTransformARGB() {

        TileProvider t = new TileProvider() {
            @Override
            public Tile tileAt(int zoom, int x, int y) {

                BufferedImage i = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
                Graphics2D context = i.createGraphics();
                context.setColor(new Color(255, 127, 0));
                context.fillRect(0, 0, 100, 100);

                return new Tile(zoom, x, y, i);
            }
        };

        TileProvider o = new TransparentTileProvider(t, 0.75);

        BufferedImage i1 = t.tileAt(1, 1, 1).image();
        BufferedImage i2 = o.tileAt(1, 1, 1).image();

        for (int dx = 0; dx < 100; ++dx) {  
            for (int dy = 0; dy < 100; ++dy) {

                int c1 = i1.getRGB(dx, dy) >>> 24;
        int c2 = i2.getRGB(dx, dy) >>> 24;

        assertEquals(c1*0.75, c2, 1);
            }
        }


    }


}
