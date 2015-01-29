package ch.epfl.isochrone.timetable;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class TestFastestPathTree {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Stop stop = null;
        Map<Stop, Integer> arrivalTimes = null;
        Map<Stop, Stop> predecessors = null;
        FastestPathTree f = new FastestPathTree(stop, arrivalTimes, predecessors);
        Stop s = f.startingStop();
        int i = f.startingTime();
        Set<Stop> ss = f.stops();
        i = f.arrivalTime(stop);
        List<Stop> p = f.pathTo(stop);
        System.out.println("" + s + i + ss + p);

        FastestPathTree.Builder fb = new FastestPathTree.Builder(stop, 0);
        fb.setArrivalTime(stop, 0, stop);
        i = fb.arrivalTime(stop);
        f = fb.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWrongStopCorrespondance() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        Map<Stop, Integer> time = new HashMap<>();
        Map<Stop, Stop> before = new HashMap<>();
        time.put(s1, 10);
        time.put(s2, 15);
        time.put(s3, 20);
        before.put(s3, s2); 
        new FastestPathTree(s1, time, before);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathToWrongStop() {
        Stop s1 = new Stop("Test1", null);
        FastestPathTree t = create();
        t.pathTo(s1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderConstructorWrongTime() {
        Stop s1 = new Stop("Test1", null);
        new FastestPathTree.Builder(s1, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderSetArrivalTimeWrongTime() {
        Stop s1 = new Stop("Test1", null);
        FastestPathTree.Builder b = new FastestPathTree.Builder(s1, 10);
        b.setArrivalTime(s1, 9, s1);
    }

    @Test
    public void testStartingTime() {
        FastestPathTree t = create();
        assertEquals(10, t.startingTime());
    }

    @Test
    public void testStop() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        Map<Stop, Integer> time = new HashMap<>();
        Map<Stop, Stop> before = new HashMap<>();
        time.put(s1, 10);
        time.put(s2, 15);
        time.put(s3, 20);
        before.put(s3, s2);
        before.put(s2, s1);
        FastestPathTree t = new FastestPathTree(s1, time, before);
        assertEquals(time.keySet(), t.stops());
    }

    @Test
    public void testArrivalTime() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        Map<Stop, Integer> time = new HashMap<>();
        Map<Stop, Stop> before = new HashMap<>();
        time.put(s1, 10);
        time.put(s3, 20);
        before.put(s3, s2);
        FastestPathTree t = new FastestPathTree(s1, time, before);
        assertEquals(10, t.arrivalTime(s1));
        assertEquals(SecondsPastMidnight.INFINITE, t.arrivalTime(s2));
        assertEquals(20, t.arrivalTime(s3));
    }

    @Test
    public void testPathTo() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        Map<Stop, Integer> time = new HashMap<>();
        Map<Stop, Stop> before = new HashMap<>();
        time.put(s1, 10);
        time.put(s2, 15);
        time.put(s3, 20);
        before.put(s3, s2);
        before.put(s2, s1);
        FastestPathTree t = new FastestPathTree(s1, time, before);
        t.pathTo(s3);
        List<Stop> path = new LinkedList<>();
        path.add(s1);
        path.add(s2);
        path.add(s3);
        List<Stop> path2 = new LinkedList<>();
        path2.add(s1);
        assertEquals(path, t.pathTo(s3));
        assertEquals(s1, t.pathTo(s3).get(0));
        assertEquals(s3, t.pathTo(s3).get(2));
        assertEquals(path2, t.pathTo(s1));
    }

    @Test
    public void testBuilderConstructorBuilder() {
        Stop s1 = new Stop("Test1", null);
        FastestPathTree.Builder b = new FastestPathTree.Builder(s1, 10);
        Map<Stop, Integer> time = new HashMap<>();
        time.put(s1, 10);
        assertEquals(time.keySet(), b.build().stops());
    }

    @Test
    public void testBuilderSetArrivalTime() {
        Stop s1 = new Stop("Test1", null);
        FastestPathTree.Builder b = new FastestPathTree.Builder(s1, 10);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        b.setArrivalTime(s3, 20, s1);
        FastestPathTree t = b.build();
        assertEquals(10, t.arrivalTime(s1));
        assertEquals(SecondsPastMidnight.INFINITE, t.arrivalTime(s2));
        assertEquals(20, t.arrivalTime(s3));
    }

    @Test
    public void testBuilderArrivalTime() {
        Stop s1 = new Stop("Test1", null);
        FastestPathTree.Builder b = new FastestPathTree.Builder(s1, 10);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        b.setArrivalTime(s3, 20, s2);
        FastestPathTree t = b.build();
        assertEquals(10, t.arrivalTime(s1));
        assertEquals(SecondsPastMidnight.INFINITE, t.arrivalTime(s2));
        assertEquals(20, t.arrivalTime(s3));
    }

    private FastestPathTree create() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        Map<Stop, Integer> time = new HashMap<>();
        Map<Stop, Stop> before = new HashMap<>();
        time.put(s1, 10);
        time.put(s2, 15);
        time.put(s3, 20);
        before.put(s3, s2);
        before.put(s2, s1);
        return new FastestPathTree(s1, time, before);
    }
}
