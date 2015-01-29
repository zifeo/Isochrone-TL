package ch.epfl.isochrone.timetable;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;

public class TestGraphEdge {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        int i1 = GraphEdge.packTrip(0, 0);
        i1 = GraphEdge.unpackTripDepartureTime(0);
        i1 = GraphEdge.unpackTripDuration(0);
        i1 = GraphEdge.unpackTripArrivalTime(0) + i1;
        Stop s = null;
        GraphEdge e = new GraphEdge(s, 0, Collections.<Integer>emptySet());
        s = e.destination();
        i1 = e.earliestArrivalTime(0);

        GraphEdge.Builder b = new GraphEdge.Builder(s);
        b.setWalkingTime(0);
        b.addTrip(0, 0);
        e = b.build();
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testPackTripNegative() {
        GraphEdge.packTrip(-1, 10);        
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testPackTripTooLong() {
        GraphEdge.packTrip(1000000, 1000000);        
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testPackTripDiffNegative() {
        GraphEdge.packTrip(1000, 10);        
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testPackTripDiffTooLong() {
        GraphEdge.packTrip(10, 100000);        
    }

    @Test
    public void testPackTrip() {
        Random rng = new Random();
        for (int i = 0; i < 100; ++i) {
            int start = rng.nextInt(107999);
            int end = start + rng.nextInt(9999);
            assertEquals(start*16384+(end-start), GraphEdge.packTrip(start, end));
        }
    }

    @Test
    public void testUnpackTripDepartureTime() {
        Random rng = new Random();
        for (int i = 0; i < 100; ++i) {
            int start = rng.nextInt(107999);
            int end = start + rng.nextInt(9999);
            int pack = GraphEdge.packTrip(start, end);
            assertEquals(start, GraphEdge.unpackTripDepartureTime(pack));
        }        
    }

    @Test
    public void testUnpackTripDuration() {
        Random rng = new Random();
        for (int i = 0; i < 100; ++i) {
            int start = rng.nextInt(107999);
            int end = start + rng.nextInt(9999);
            int pack = GraphEdge.packTrip(start, end);
            assertEquals(end-start, GraphEdge.unpackTripDuration(pack));
        }    
    }

    @Test
    public void testUnpackTripArrivalTime() {
        Random rng = new Random();
        for (int i = 0; i < 100; ++i) {
            int start = rng.nextInt(107999);
            int end = start + rng.nextInt(9999);
            int pack = GraphEdge.packTrip(start, end);
            assertEquals(end, GraphEdge.unpackTripArrivalTime(pack));
        }    
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testConstructorNegative() {
        Stop s = new Stop("Test", new PointWGS84(0, 0));
        Set<Integer> i = new HashSet<>();
        new GraphEdge(s, -243, i);       
    }

    @Test
    public void testDestination() {
        Stop s = new Stop("Test", new PointWGS84(0, 0));
        Set<Integer> i = new HashSet<>();
        GraphEdge g = new GraphEdge(s, 234, i);
        assertEquals(s, g.destination());
    }

    @Test
    public void testEarliestArrivalTime() {
        Stop s = new Stop("Test", new PointWGS84(0, 0));
        Set<Integer> i = new HashSet<>();
        i.add(GraphEdge.packTrip(1000,1050));
        i.add(GraphEdge.packTrip(3000,3050));
        i.add(GraphEdge.packTrip(2000,2050));
        i.add(GraphEdge.packTrip(4000,4050));
        GraphEdge g = new GraphEdge(s, 250, i);
        assertEquals(251, g.earliestArrivalTime(1));
        assertEquals(1050, g.earliestArrivalTime(990));
        assertEquals(1050, g.earliestArrivalTime(1000));
        assertEquals(1251, g.earliestArrivalTime(1001));
        assertEquals(4251, g.earliestArrivalTime(4001));
        GraphEdge g2 = new GraphEdge(s, -1, i);
        assertEquals(SecondsPastMidnight.INFINITE, g2.earliestArrivalTime(4001));
    }

    @Test
    public void testBuilderDestination() {
        Stop s = new Stop("Test", new PointWGS84(0, 0));
        GraphEdge.Builder b = new GraphEdge.Builder(s);
        GraphEdge g = b.build();
        assertEquals(s, g.destination());
    }
}
