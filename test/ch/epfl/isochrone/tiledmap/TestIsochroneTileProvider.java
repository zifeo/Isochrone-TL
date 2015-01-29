package ch.epfl.isochrone.tiledmap;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;

public class TestIsochroneTileProvider {

    @Test(expected = IllegalArgumentException.class)
    public void TestIsochroneTileProviderWrongWalkingSpeed() {
        new IsochroneTileProvider(null, null, -2);
    }

    @Test
    public void testTileAt() {

        final int walkingTime = 300;
        final double walkingSpeed = 1.25;
        final String arret = "Lausanne-Flon";
        final Date date = new Date(1, 10, 2013);
        final int temps = SecondsPastMidnight.fromHMS(6, 8, 0);

        FastestPathTree tree = null;

        try {

            final TimeTableReader reader = new TimeTableReader("/time-table-test/");
            final TimeTable timetable = reader.readTimeTable();
            Stop depart = null;

            for (Stop s : timetable.stops()) { // recuperation d'un arret en fonction de son nom
                if (s.name().equals(arret)) {
                    depart = s;
                    break;
                }
            }

            final Graph graph = reader.readGraphForServices(timetable.stops(), timetable.servicesForDate(date), walkingTime, walkingSpeed);
            tree = graph.fastestPaths(depart, temps);

        } catch (IOException e) { 
            System.out.println("Erreur IO : "+e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur : "+e.getMessage());
        }

        ColorTable.Builder colorsBuilder = new ColorTable.Builder(300);
        colorsBuilder.addColor(0, 0, 0)
        .addColor(0, 0, 0.5)
        .addColor(0, 0, 1)
        .addColor(0, 0.5, 0.5)
        .addColor(0, 1, 0)
        .addColor(0.5, 1, 0)
        .addColor(1, 1, 0)
        .addColor(1, 0.5, 0)
        .addColor(1, 0, 0);
        ColorTable colors = colorsBuilder.build();

        IsochroneTileProvider p = new IsochroneTileProvider(tree, colors, walkingSpeed);

        Tile t = p.tileAt(11, 1061, 724);

        try {
            ImageIO.write(t.image(), "png", new File("image.png"));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}
