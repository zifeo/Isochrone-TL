package ch.epfl.isochrone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.epfl.isochrone.timetable.Date;
import ch.epfl.isochrone.timetable.FastestPathTree;
import ch.epfl.isochrone.timetable.Graph;
import ch.epfl.isochrone.timetable.SecondsPastMidnight;
import ch.epfl.isochrone.timetable.Stop;
import ch.epfl.isochrone.timetable.TimeTable;
import ch.epfl.isochrone.timetable.TimeTableReader;

/**
 * Recherche les relations a partir d'un arret, d'une date et d'une heure de depart.
 * Les arguments doivent etre rentre dans la console selon cet ordre : arret yyyy-mm-dd hh:mm:ss.
 */
public final class TimeTableSearch {

    public static void main(String[] args) {

        final int walkingTime = 300;
        final double walkingSpeed = 1.25;
        final String arret = args[0];
        final String[] dateStr = args[1].split("-"), tempsStr = args[2].split(":");
        final Date date = new Date(Integer.parseInt(dateStr[2]),
                Integer.parseInt(dateStr[1]),
                Integer.parseInt(dateStr[0]));
        final int temps = SecondsPastMidnight.fromHMS(Integer.parseInt(tempsStr[0]),
                Integer.parseInt(tempsStr[1]), 
                Integer.parseInt(tempsStr[2]));

        try {

            final TimeTableReader reader = new TimeTableReader("/time-table/");
            final TimeTable timetable = reader.readTimeTable();
            Stop depart = null;

            for (Stop s : timetable.stops()) { // recuperation d'un arret en fonction de son nom
                if (s.name().equals(arret)) {
                    depart = s;
                    break;
                }
            }

            final Graph graph = reader.readGraphForServices(timetable.stops(), timetable.servicesForDate(date), walkingTime, walkingSpeed);
            final FastestPathTree tree = graph.fastestPaths(depart, temps);

            final List<Stop> allStops = new ArrayList<>(tree.stops());
            Collections.sort(allStops, new Comparator<Stop>(){ // tri alphabetique des arrets
                @Override
                public int compare(Stop s1, Stop s2) {
                    return s1.name().compareTo(s2.name());
                }
            });

            for (Stop s : allStops) {

                int time = tree.arrivalTime(s);

                System.out.print(s+" : ");
                System.out.printf("%02d:%02d:%02d \n", SecondsPastMidnight.hours(time), SecondsPastMidnight.minutes(time), SecondsPastMidnight.seconds(time));
                System.out.println("via: "+tree.pathTo(s)+"\n");
            }

        } catch (IOException e) { 
            System.out.println("Erreur IO : "+e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur : "+e.getMessage());
        }
    }
}
