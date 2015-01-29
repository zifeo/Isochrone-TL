package ch.epfl.isochrone.timetable;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.geo.PointWGS84;
import ch.epfl.isochrone.timetable.Date.DayOfWeek;
import ch.epfl.isochrone.timetable.Date.Month;

public class TestTimeTable {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        TimeTable t = new TimeTable(Collections.<Stop> emptySet(),
                Collections.<Service> emptySet());
        t.stops();
        t.servicesForDate(new Date(1, Month.JANUARY, 2000));

        TimeTable.Builder b = new TimeTable.Builder();
        b.addStop(new Stop("s", new PointWGS84(0, 0)));
        Date d = new Date(1, Month.APRIL, 2000);
        b.addService(new Service("s", d, d, Collections.<DayOfWeek> emptySet(),
                Collections.<Date> emptySet(), Collections.<Date> emptySet()));
        b.build();
    }

    @Test
    public void testStops() {
        Set<Stop> stops = new HashSet<Stop>();
        stops.add(new Stop("Test", new PointWGS84(0, 0)));
        Collection<Service> services = new ArrayList<Service>();

        TimeTable t = new TimeTable(stops, services);
        assertEquals(stops, t.stops());
        stops.clear();
        assertNotEquals(stops, t.stops());
    }

    @Test(expected = java.lang.UnsupportedOperationException.class)
    public void testStopsMutable() {
        Set<Stop> stops = new HashSet<Stop>();
        stops.add(new Stop("Test", new PointWGS84(0, 0)));
        Collection<Service> services = new ArrayList<Service>();

        TimeTable t = new TimeTable(stops, services);        
        t.stops().clear();
    }

    @Test
    public void testServicesForDate() {
        Set<Stop> stops = new HashSet<Stop>();
        stops.add(new Stop("Test", new PointWGS84(0, 0)));
        Collection<Service> services = new ArrayList<Service>();

        Date start = new Date(24, Month.FEBRUARY, 2014);
        Date end = new Date(10, Month.MARCH, 2014);
        Date end2 = new Date(25, Month.FEBRUARY, 2014);

        Service.Builder b = new Service.Builder("Test", start, end);

        b.addOperatingDay(DayOfWeek.MONDAY);
        b.addOperatingDay(DayOfWeek.TUESDAY);
        b.addOperatingDay(DayOfWeek.THURSDAY);
        b.addOperatingDay(DayOfWeek.FRIDAY);
        b.addOperatingDay(DayOfWeek.SATURDAY);

        b.addExcludedDate(new Date(27, Month.FEBRUARY, 2014));
        b.addExcludedDate(new Date(6, Month.MARCH, 2014));

        b.addIncludedDate(new Date(5, Month.MARCH, 2014));
        b.addIncludedDate(new Date(9, Month.MARCH, 2014));

        Service s = b.build();

        HashSet<Service> testList = new HashSet<Service>();
        TimeTable t = new TimeTable(stops, services);

        assertEquals(testList, t.servicesForDate(null));

        services.add(s);
        testList.add(s);

        TimeTable t2 = new TimeTable(stops, services);

        assertEquals(testList, t2.servicesForDate(new Date(5, Month.MARCH, 2014)));
        assertNotEquals(testList, t2.servicesForDate(new Date(6, Month.MARCH, 2014)));

        Service.Builder b2 = new Service.Builder("Test2", start, end2);
        Service s2 = b2.build();

        services.add(s2);

        TimeTable t3 = new TimeTable(stops, services);
        assertEquals(testList, t3.servicesForDate(new Date(5, Month.MARCH, 2014)));
    }

    @Test
    public void testBuilder() {

        TimeTable.Builder bt = new TimeTable.Builder();

        bt.addStop(new Stop("Test", new PointWGS84(0, 0)));

        Date start = new Date(24, Month.FEBRUARY, 2014);
        Date end = new Date(10, Month.MARCH, 2014);
        Date end2 = new Date(25, Month.FEBRUARY, 2014);

        Service.Builder b = new Service.Builder("Test", start, end);

        b.addOperatingDay(DayOfWeek.MONDAY);
        b.addOperatingDay(DayOfWeek.TUESDAY);
        b.addOperatingDay(DayOfWeek.THURSDAY);
        b.addOperatingDay(DayOfWeek.FRIDAY);
        b.addOperatingDay(DayOfWeek.SATURDAY);

        b.addExcludedDate(new Date(27, Month.FEBRUARY, 2014));
        b.addExcludedDate(new Date(6, Month.MARCH, 2014));

        b.addIncludedDate(new Date(5, Month.MARCH, 2014));
        b.addIncludedDate(new Date(9, Month.MARCH, 2014));

        Service s = b.build();

        HashSet<Service> testList = new HashSet<Service>();
        TimeTable t = bt.build();

        assertEquals(testList, t.servicesForDate(null));

        bt.addService(s);
        testList.add(s);

        TimeTable t2 = bt.build();

        assertEquals(testList, t2.servicesForDate(new Date(5, Month.MARCH, 2014)));
        assertNotEquals(testList, t2.servicesForDate(new Date(6, Month.MARCH, 2014)));

        Service.Builder b2 = new Service.Builder("Test2", start, end2);
        Service s2 = b2.build();

        bt.addService(s2);

        TimeTable t3 = bt.build();
        assertEquals(testList, t3.servicesForDate(new Date(5, Month.MARCH, 2014)));
    }
}
