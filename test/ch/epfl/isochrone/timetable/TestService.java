package ch.epfl.isochrone.timetable;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;

import ch.epfl.isochrone.timetable.Date.DayOfWeek;
import ch.epfl.isochrone.timetable.Date.Month;

public class TestService {
    // Le "test" suivant n'en est pas un à proprement parler, raison pour
    // laquelle il est ignoré (annotation @Ignore). Son seul but est de garantir
    // que les noms des classes et méthodes sont corrects.
    @Test
    @Ignore
    public void namesAreOk() {
        Date d = new Date(1, Month.JANUARY, 2000);
        Service s = new Service("s",
                d, d,
                Collections.<Date.DayOfWeek> emptySet(),
                Collections.<Date> emptySet(),
                Collections.<Date> emptySet());
        s.name();
        s.isOperatingOn(d);

        Service.Builder sb = new Service.Builder("s", d, d);
        sb.name();
        sb.addOperatingDay(DayOfWeek.MONDAY);
        sb.addExcludedDate(d);
        sb.addIncludedDate(d);
        sb.build();
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testConstructorWrongDate() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, -1);
        HashSet<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
        HashSet<Date> exclude = new HashSet<Date>();
        HashSet<Date> include = new HashSet<Date>();
        new Service("Test", start, end, days, exclude, include);
    }

    @Test
    public void testConstructor() {
        Date start = new Date(24, Month.FEBRUARY, 2014);
        Date end = new Date(10, Month.MARCH, 2014);

        HashSet<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
        days.add(DayOfWeek.FRIDAY);

        HashSet<Date> exclude = new HashSet<Date>();
        HashSet<Date> include = new HashSet<Date>();

        Service s = new Service("Test", start, end, days, exclude, include);

        assertTrue(s.isOperatingOn(new Date(28, Month.FEBRUARY, 2014)));
        exclude.add(new Date(28, Month.FEBRUARY, 2014));
        assertTrue(s.isOperatingOn(new Date(28, Month.FEBRUARY, 2014)));

        assertFalse(s.isOperatingOn(new Date(27, Month.FEBRUARY, 2014)));
        include.add(new Date(27, Month.FEBRUARY, 2014));
        assertFalse(s.isOperatingOn(new Date(27, Month.FEBRUARY, 2014)));

        assertTrue(s.isOperatingOn(new Date(28, Month.FEBRUARY, 2014)));
        days.clear();
        assertTrue(s.isOperatingOn(new Date(28, Month.FEBRUARY, 2014)));
    }

    @Test
    public void testName() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, 1);
        HashSet<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
        HashSet<Date> exclude = new HashSet<Date>();
        HashSet<Date> include = new HashSet<Date>();
        Service s = new Service("Test", start, end, days, exclude, include);
        assertEquals("Test", s.name());
    }

    @Test
    public void testIsOperatingOn() {
        Date start = new Date(24, Month.FEBRUARY, 2014);
        Date end = new Date(10, Month.MARCH, 2014);

        HashSet<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
        days.add(DayOfWeek.MONDAY);
        days.add(DayOfWeek.TUESDAY);
        days.add(DayOfWeek.THURSDAY);
        days.add(DayOfWeek.FRIDAY);
        days.add(DayOfWeek.SATURDAY);

        HashSet<Date> exclude = new HashSet<Date>();
        exclude.add(new Date(27, Month.FEBRUARY, 2014));
        exclude.add(new Date(6, Month.MARCH, 2014));

        HashSet<Date> include = new HashSet<Date>();
        include.add(new Date(5, Month.MARCH, 2014));
        include.add(new Date(9, Month.MARCH, 2014));

        Service s = new Service("Test", start, end, days, exclude, include);

        // avant
        assertFalse(s.isOperatingOn(new Date(22, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(23, Month.FEBRUARY, 2014)));

        // date limite
        assertTrue(s.isOperatingOn(new Date(24, Month.FEBRUARY, 2014)));
        assertTrue(s.isOperatingOn(new Date(8, Month.MARCH, 2014)));

        // apres
        assertFalse(s.isOperatingOn(new Date(10, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(12, Month.MARCH, 2014)));

        // date jour valide
        assertTrue(s.isOperatingOn(new Date(1, Month.MARCH, 2014)));
        assertTrue(s.isOperatingOn(new Date(3, Month.MARCH, 2014)));

        // date jour non valide
        assertFalse(s.isOperatingOn(new Date(26, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(2, Month.MARCH, 2014)));

        // date exclue
        assertFalse(s.isOperatingOn(new Date(27, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(6, Month.MARCH, 2014)));

        // date inclue
        assertTrue(s.isOperatingOn(new Date(5, Month.MARCH, 2014)));
        assertTrue(s.isOperatingOn(new Date(9, Month.MARCH, 2014)));
    }

    @Test
    public void testToString() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, 1);
        HashSet<Date.DayOfWeek> days = new HashSet<Date.DayOfWeek>();
        HashSet<Date> exclude = new HashSet<Date>();
        HashSet<Date> include = new HashSet<Date>();
        Service s = new Service("Test", start, end, days, exclude, include);
        assertEquals("Test", s.toString());
    }


    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testBuilderWrongDate() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, -1);
        new Service.Builder("Test", start, end);
    }

    @Test
    public void testBuilderName() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, 1);
        Service.Builder s = new Service.Builder("Test", start, end);
        assertEquals("Test", s.name());
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testAddExcludedDateWrongDate() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, 1);
        Service.Builder s = new Service.Builder("Test", start, end);
        s.addExcludedDate(new Date(1, Month.JANUARY, -1));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testAddExcludedDateAlreadyIncluded() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, 1);
        Service.Builder s = new Service.Builder("Test", start, end);
        s.addIncludedDate(new Date(1, Month.JANUARY, 1));
        s.addExcludedDate(new Date(1, Month.JANUARY, 1));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testAddIncludedDateWrongDate() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, 1);
        Service.Builder s = new Service.Builder("Test", start, end);
        s.addIncludedDate(new Date(1, Month.JANUARY, -1));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testAddIncludedDateAlreadyIncluded() {
        Date start = new Date(1, Month.JANUARY, 1);
        Date end = new Date(1, Month.JANUARY, 1);
        Service.Builder s = new Service.Builder("Test", start, end);
        s.addExcludedDate(new Date(1, Month.JANUARY, 1));
        s.addIncludedDate(new Date(1, Month.JANUARY, 1));
    }

    @Test
    public void testBuilder() {
        Date start = new Date(24, Month.FEBRUARY, 2014);
        Date end = new Date(10, Month.MARCH, 2014);

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

        // avant
        assertFalse(s.isOperatingOn(new Date(22, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(23, Month.FEBRUARY, 2014)));

        // date limite
        assertTrue(s.isOperatingOn(new Date(24, Month.FEBRUARY, 2014)));
        assertTrue(s.isOperatingOn(new Date(8, Month.MARCH, 2014)));

        // apres
        assertFalse(s.isOperatingOn(new Date(10, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(12, Month.MARCH, 2014)));

        // date jour valide
        assertTrue(s.isOperatingOn(new Date(1, Month.MARCH, 2014)));
        assertTrue(s.isOperatingOn(new Date(3, Month.MARCH, 2014)));

        // date jour non valide
        assertFalse(s.isOperatingOn(new Date(26, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(2, Month.MARCH, 2014)));

        // date exclue
        assertFalse(s.isOperatingOn(new Date(27, Month.FEBRUARY, 2014)));
        assertFalse(s.isOperatingOn(new Date(6, Month.MARCH, 2014)));

        // date inclue
        assertTrue(s.isOperatingOn(new Date(5, Month.MARCH, 2014)));
        assertTrue(s.isOperatingOn(new Date(9, Month.MARCH, 2014)));
    }
}
