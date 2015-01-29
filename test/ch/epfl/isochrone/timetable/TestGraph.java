package ch.epfl.isochrone.timetable;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;

public class TestGraph {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        // Graph n'a aucune méthode publique à ce stade...

        Set<Stop> stops = null;
        Stop stop = null;
        Graph.Builder gb = new Graph.Builder(stops);
        gb.addTripEdge(stop, stop, 0, 0);
        gb.addAllWalkEdges(0, 0);
        gb.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTripEdgeWrongFromStop() {

        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        Set<Stop> stops = new HashSet<>();
        stops.add(s1);
        stops.add(s2);

        Graph.Builder b = new Graph.Builder(stops);
        b.addTripEdge(s3, s2, 5, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTripEdgeWrongToStop() {

        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Stop s3 = new Stop("Test3", null);
        Set<Stop> stops = new HashSet<>();
        stops.add(s1);
        stops.add(s2);

        Graph.Builder b = new Graph.Builder(stops);
        b.addTripEdge(s2, s3, 5, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTripEdgeNegativeTime1() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Set<Stop> stops = new HashSet<>();
        stops.add(s1);
        stops.add(s2);
        Graph.Builder b = new Graph.Builder(stops);
        b.addTripEdge(s1, s2, -5, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTripEdgeNegativeTime2() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Set<Stop> stops = new HashSet<>();
        stops.add(s1);
        stops.add(s2);
        Graph.Builder b = new Graph.Builder(stops);
        b.addTripEdge(s1, s2, 5, -10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTripEdgeWrongTime() {
        Stop s1 = new Stop("Test1", null);
        Stop s2 = new Stop("Test2", null);
        Set<Stop> stops = new HashSet<>();
        stops.add(s1);
        stops.add(s2);
        Graph.Builder b = new Graph.Builder(stops);
        b.addTripEdge(s1, s2, 10, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAllWalkEdgesNegativeTime1() {
        Set<Stop> stops = new HashSet<>();
        Graph.Builder b = new Graph.Builder(stops);
        b.addAllWalkEdges(-5, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAllWalkEdgesNegativeTime2() {
        Set<Stop> stops = new HashSet<>();
        Graph.Builder b = new Graph.Builder(stops);
        b.addAllWalkEdges(5, -10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFastestPathsWrongStop() {
        Set<Stop> stops = new HashSet<>();
        PointWGS84 p = new PointWGS84(0, 0); 
        Stop a1 = new Stop("A", p);
        Stop a2 = new Stop("B", p);
        stops.add(a1);
        stops.add(a2);
        Graph.Builder b = new Graph.Builder(stops);
        b.addTripEdge(a1, a2, 10, 20);
        b.addAllWalkEdges(5, 10);
        Graph g = b.build();
        g.fastestPaths(new Stop("C", p), 20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFastestPathsWrongTime() {
        Set<Stop> stops = new HashSet<>();
        PointWGS84 p = new PointWGS84(0, 0); 
        Stop a1 = new Stop("A", p);
        Stop a2 = new Stop("B", p);
        stops.add(a1);
        stops.add(a2);
        Graph.Builder b = new Graph.Builder(stops);
        b.addTripEdge(a1, a2, 10, 20);
        b.addAllWalkEdges(5, 10);
        Graph g = b.build();
        g.fastestPaths(a1, -20);
    }

    @Test
    @Ignore // Worked on the real dataset
    public void testFastestPaths() throws IOException {

        int walkingTime = 300;
        double walkingSpeed = 1.25;
        String arret = "Lausanne-Flon";

        TimeTableReader reader = new TimeTableReader("/time-table-test/");
        TimeTable timetable = reader.readTimeTable();
        Stop depart = null;

        for (Stop s : timetable.stops()) {
            if (s.name().equals(arret)) {
                depart = s;
                break;
            }
        }

        Date jour = new Date(1, 11, 2013);
        int secondes = SecondsPastMidnight.fromHMS(6, 8, 0);
        Graph graph = reader.readGraphForServices(timetable.stops(), timetable.servicesForDate(jour), walkingTime, walkingSpeed);
        FastestPathTree tree = graph.fastestPaths(depart, secondes);

        List<Stop> allStops = new ArrayList<>(tree.stops());
        Collections.sort(allStops, new Comparator<Stop>(){
            @Override
            public int compare(Stop s1, Stop s2) {
                return s1.name().compareTo(s2.name());
            }
        });

        String[] arrivals = {"6:27:48"};

        String[] routes = {"[Lausanne-Flon, Port-Franc, EPSIC, Ecole des Métiers, Couchirard, Prélaz-les-Roses, Galicien, Perrelet, Renens-Village, Sous l'Eglise, Hôtel-de-Ville, Avenir, 1er Août]"};

        for (int i = 0; i < arrivals.length; ++i) {
            int arrival = tree.arrivalTime(allStops.get(i));
            System.out.print(i);
            assertEquals(arrivals[i], SecondsPastMidnight.hours(arrival)+":"+
                    SecondsPastMidnight.minutes(arrival)+":"+
                    SecondsPastMidnight.seconds(arrival));
            assertEquals(routes[i], tree.pathTo(allStops.get(i)).toString());
        }

    }
}
