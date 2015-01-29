package ch.epfl.isochrone.tiledmap;

import static org.junit.Assert.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class TestColorTable {

    private final Random r = new Random();

    @Test(expected = IllegalArgumentException.class)
    public void testColorTableWrongTime() {
        new ColorTable(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColorTableWrongTime2() {
        new ColorTable(-1, null);
    }

    @Test
    public void testGetSliceDuration() {
        for (int i = 1; i < 100; ++i) {
            ColorTable c = new ColorTable(i, new ArrayList<Color>());
            assertEquals(i, c.getTimeFrame());
        }
    }

    @Test
    public void testGetSliceCount() {
        List<Color> l = new ArrayList<>();
        int max = r.nextInt()%100 + 100;
        for (int i = 0; i < max; ++i) {
            l.add(new Color(0,0,0));
        }
        ColorTable c = new ColorTable(10, l);
        assertEquals(max, c.getSliceCount());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetSliceColorWrongId() {
        List<Color> l = new ArrayList<>();
        l.add(new Color(0,0,0));
        ColorTable c = new ColorTable(10, l);
        c.getSliceColor(3);
    }

    @Test
    public void testGetSliceColor() {
        List<Color> l = new ArrayList<>();
        int max = r.nextInt()%100 + 100;
        for (int i = 0; i < max; ++i) {
            l.add(new Color(0,i%255,0));
        }
        ColorTable c = new ColorTable(10, l);
        for (int i = 0; i < max; ++i) {
            assertEquals(l.get(i), c.getSliceColor(i));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderColorTableWrongTime() {
        new ColorTable.Builder(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddColorWrongRColor() {
        ColorTable.Builder b = new ColorTable.Builder(5);
        b.addColor(2, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddColorWrongGColor() {
        ColorTable.Builder b = new ColorTable.Builder(5);
        b.addColor(0, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddColorWrongBColor() {
        ColorTable.Builder b = new ColorTable.Builder(5);
        b.addColor(0, 0, 2);
    }

    @Test
    public void testAddColor() {
        ColorTable.Builder b = new ColorTable.Builder(5);
        b.addColor(0, 0.25, 0.5);
        ColorTable c = b.build();
        assertEquals(0, c.getSliceColor(0).getRed());
        assertEquals(64, c.getSliceColor(0).getGreen());
        assertEquals(128, c.getSliceColor(0).getBlue());
    }

}
