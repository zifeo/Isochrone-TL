package ch.epfl.isochrone.timetable;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;

public class TestStop {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Stop s = new Stop("invalid", new PointWGS84(6.57, 46.52));
        s.name();
        s.position();
    }

    @Test
    public void testName() {
        assertEquals("Test", (new Stop("Test", new PointWGS84(0, 0)).name()));
    }

    @Test
    public void testPosition() {
        PointWGS84 p = new PointWGS84(0, 0);
        assertEquals(p, (new Stop("Test", p)).position());
        PointWGS84 p2 = new PointWGS84(1, 1);
        assertEquals(p2, (new Stop("Test", p2)).position());
    }

    @Test
    public void testToString() {
        assertEquals("Test", (new Stop("Test", new PointWGS84(0, 0)).toString()));
    }
}
