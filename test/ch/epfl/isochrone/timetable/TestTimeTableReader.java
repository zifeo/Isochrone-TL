package ch.epfl.isochrone.timetable;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class TestTimeTableReader {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() throws IOException {
        TimeTableReader r = new TimeTableReader("");
        TimeTable t = r.readTimeTable();
        @SuppressWarnings("unused")
        Graph g = r.readGraphForServices(t.stops(), Collections.<Service>emptySet(), 0, 0d);
    }

    @Test
    @Ignore
    public void testReadTimeTable() throws IOException {
        TimeTableReader reader = new TimeTableReader("/time-table-test/");
        TimeTable t = reader.readTimeTable();
        Map<String, Stop> stopMap = new HashMap<>();

        Set<Stop> stops = t.stops();
        for (Stop s : stops) {
            stopMap.put(s.name(), s);
        }

        assertEquals(Math.toRadians(46.5411232548), stopMap.get("Stop").position().latitude(), 0.0001);
        assertEquals(Math.toRadians(6.64799239616), stopMap.get("Stop").position().longitude(), 0.0001);
        assertEquals(Math.toRadians(46.5277146591), stopMap.get("Stop").position().latitude(), 0.0001);
        assertEquals(Math.toRadians(6.63388978363), stopMap.get("Stop").position().longitude(), 0.0001);

        Date d = new Date(11, 12, 2013);        
        Map<String, Service> serviceMap = new HashMap<>();

        Set<Service> services = t.servicesForDate(d);
        for (Service s : services) {
            serviceMap.put(s.name(), s);
        }

        assertEquals("service", serviceMap.get("service").name());
        assertEquals(2, serviceMap.size());

        Date d2 = new Date(24, 10, 2013);        
        assertFalse(serviceMap.get("service").isOperatingOn(d2));
    }

    @Test
    @Ignore
    public void testReadGraphForServices() throws IOException {
        TimeTableReader reader = new TimeTableReader("/time-table-test/");
        TimeTable t = reader.readTimeTable();
        Set<Service> services = t.servicesForDate(new Date(6, 12, 2013));

        Map<String, Stop> stopMap = new HashMap<>();
        Set<Stop> allStops = t.stops();
        for (Stop s : allStops) {
            stopMap.put(s.name(), s);
        }

        Set<Stop> stops = new HashSet<>();
        stops.add(stopMap.get("Croisettes"));
        stops.add(stopMap.get("Vennes"));
        stops.add(stopMap.get("Fourmi"));
        stops.add(stopMap.get("Sallaz"));
        stops.add(stopMap.get("CHUV"));

        reader.readGraphForServices(stops, services, 10, 10);
        
    }
}
