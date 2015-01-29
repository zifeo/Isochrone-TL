package ch.epfl.isochrone.timetable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.epfl.isochrone.geo.PointWGS84;

/**
 * Un lecteur d'horaire depuis les donnees CSV.
 * Classe immuable.
 */
public final class TimeTableReader {

    private final static int SERVICE_ACTIF = 1;
    private final static String SERVICE_ON_THIS_DAY = "1";
    private final String baseResourceName;

    /**
     * Constructeur public d'un lecteur d'horaire en fonction du chemin donne.
     * 
     * @param   baseResourceName
     *          Le chemin du dossier parent des fichiers CSV avec un slash avant et après.
     */
    public TimeTableReader(String baseResourceName) {
        this.baseResourceName = baseResourceName;
    }

    /**
     * Lit et retourne les arrets et services (exceptions de services compris) dans une table des horaires.
     * 
     * @return  La table des horaires correspondant aux donnees cvs
     * @throws  IOException
     *          En cas d'erreur de lecture.
     */
    public TimeTable readTimeTable() throws IOException {

        TimeTable.Builder builder = new TimeTable.Builder();

        addStopsFromFile(builder);
        addServicesFromFile(builder);

        return builder.build();
    }

    // lit et ajoute les arrets de stops.csv dans le constructeur de table des horaires
    private void addStopsFromFile(TimeTable.Builder timetable) throws IOException {

        // Nom;latitude;longitude
        String line;
        final BufferedReader reader = createReader("stops.csv");

        while ((line = reader.readLine()) != null) {

            String args[] = line.split(";");
            assert args.length == 3;

            String name = args[0];
            double longitude = Math.toRadians(Double.parseDouble(args[2]));
            double latitude = Math.toRadians(Double.parseDouble(args[1]));
            PointWGS84 position = new PointWGS84(longitude, latitude); 

            timetable.addStop(new Stop(name, position));
        }
        reader.close();
    }

    // lit et ajoute les services (et exceptions) de calendar.csv (et calendar_dates.csv) dans le constructeur de table des horaires
    private void addServicesFromFile(TimeTable.Builder timetable) throws IOException {

        // SERVICES : Nom;Lu;Ma;Me;Je;Ve;Sa;Di;Debut;Fin(format:20130913)
        String line;
        final Map<String, Service.Builder> builders = new HashMap<>();
        final BufferedReader calendarReader = createReader("calendar.csv");

        while ((line = calendarReader.readLine()) != null) {

            String args[] = line.split(";");
            assert args.length == 10;

            String name = args[0];
            Date startingDate = dateFromText(args[8]);
            Date endingDate = dateFromText(args[9]);
            Service.Builder b = new Service.Builder(name, startingDate, endingDate);

            for (int day = 0; day < 7; ++day) {
                if (args[day+1].equals(SERVICE_ON_THIS_DAY)) {
                    b.addOperatingDay(Date.DayOfWeek.values()[day]); // ajoute un jour d'operation en recuperant le jour dans Date.DayOfWeek
                }
            }        

            builders.put(name, b);
        }
        calendarReader.close();

        // EXCEPTIONS : Nom;Date;Type
        final BufferedReader exceptionReader = createReader("calendar_dates.csv");

        while ((line = exceptionReader.readLine()) != null) {

            String args[] = line.split(";");
            assert args.length == 3;

            String name = args[0];
            Date date = dateFromText(args[1]);
            int type = Integer.parseInt(args[2]);

            if (type == SERVICE_ACTIF) {
                builders.get(name).addIncludedDate(date);
            } else {
                builders.get(name).addExcludedDate(date);
            }
        }  
        exceptionReader.close();

        for (Service.Builder b : builders.values()) {
            timetable.addService(b.build());
        }
    }

    /**
     * Lit et retourne le graphe correspondant aux arrets et services donnes selon un temps et une vitesse de marche.
     * 
     * @param   stops
     *          Les arrets inclus dans le graphe.
     * @param   services
     *          Les services actifs dans le graphe.
     * @param   walkingTime
     *          Le temps de marche maximal.
     * @param   walkingSpeed
     *          La vitesse de marche (m/s).
     * @return  Le graphe.
     * @throws  IOException
     *          En cas d'erreur de lecture.
     */
    public Graph readGraphForServices(Set<Stop> stops, Set<Service> services, int walkingTime, double walkingSpeed) throws IOException {

        String line;
        final Graph.Builder builder = new Graph.Builder(stops);
        final Map<String, Stop> stopsMap = new HashMap<>();
        final Set<String> servicesNames = new HashSet<>();

        for (Stop s : stops) { // association de chaque arret a son nom dans une table pour facilitier la verification future
            stopsMap.put(s.name(), s);
        }

        for (Service s : services) { // recuperation des noms de services pour faciliter la verification future
            servicesNames.add(s.name());
        }

        // TRAJETS : Nom;Arret1;Depart;Arret2;Arrivee
        final BufferedReader reader = createReader("stop_times.csv");         
        while ((line = reader.readLine()) != null) {

            String args[] = line.split(";");
            assert args.length == 5;

            String name = args[0];
            String fromStop = args[1];
            String toStop = args[3];

            // !fromStop.equals(toStop) sert a enlever les arcs qui pointent sur eux meme (mauvaises donnees de TL en cause, source professeur)
            if (servicesNames.contains(name) && !fromStop.equals(toStop) && stopsMap.containsKey(fromStop) && stopsMap.containsKey(toStop)) {

                int departureTime = Integer.parseInt(args[2]);
                int arrivalTime = Integer.parseInt(args[4]);

                builder.addTripEdge(stopsMap.get(fromStop), stopsMap.get(toStop), departureTime, arrivalTime);
            }
        }
        reader.close();

        return builder.addAllWalkEdges(walkingTime, walkingSpeed).build();
    }

    // cree un lecteur selon le chemin des donnees et un fichier
    private BufferedReader createReader(String file) throws IOException {
        InputStream stream = getClass().getResourceAsStream(baseResourceName+file);
        assert stream != null : "fichier introuvable : "+baseResourceName+file;
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    // convertit les dates du format texte (2000:01:01) en Date
    private Date dateFromText(String texte) {
        int year = Integer.parseInt(texte.substring(0, 4));
        int month = Integer.parseInt(texte.substring(4, 6));
        int day = Integer.parseInt(texte.substring(6, 8));
        return new Date(day, month, year);
    }
}
