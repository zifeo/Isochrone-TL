package ch.epfl.isochrone.timetable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Une table des horaires (ensembles d'arrets et de services)
 * Classe immuable. Possede un constructeur.
 */
public final class TimeTable {

    private final Set<Stop> stops;
    private final Set<Service> services;

    /**
     * Constructeur public d'une table des horaires.
     * 
     * @param   stops
     *          L'ensemble des arrets de la table.
     * @param   services
     *          L'ensemble des services de la table.
     */
    public TimeTable(Set<Stop> stops, Collection<Service> services) {
        this.stops = new HashSet<>(stops);
        this.services = new HashSet<>(services);
    }

    /**
     * Accesseur en lecture des arrets (non-modifiable).
     * 
     * @return  Les arrets de la table.
     */
    public Set<Stop> stops() {
        return Collections.unmodifiableSet(stops);
    }

    /**
     * Retourne les services operationnels pour une date donnee.
     * 
     * @param   date
     *          La date a tester
     * @return  services
     *          Les services operationnels.
     */
    public Set<Service> servicesForDate(Date date) {

        HashSet<Service> activeServices = new HashSet<>();

        for (Service s : services) {
            if (s.isOperatingOn(date))
                activeServices.add(s);
        }
        return activeServices;
    }

    /**
     * Batisseur de table des horaires.
     */
    public static class Builder {

        private final Set<Stop> stops;
        private final Set<Service> services;

        /**
         * Constructeur public d'un batisseur de table des horaires.
         */
        public Builder() {
            stops = new HashSet<>();
            services = new HashSet<>();
        }

        /**
         * Ajoute un arret au batisseur de table.
         * Permet les appels chaines.
         * 
         * @param   newStop
         *          L'arret a ajouter.
         * @return  Le batisseur.
         */
        public Builder addStop(Stop newStop) {
            stops.add(newStop);
            return this;
        }

        /**
         * Ajoute un service au batisseur de table..
         * Permet les appels chaines.

         * @param   newService
         *          Le service a ajouter.
         * @return  Le batisseur.
         */
        public Builder addService(Service newService) {
            services.add(newService);
            return this;
        }

        /**
         * Construit la table des horaires a partir du batisseur.
         * 
         * @return  La table des horaires.
         */
        public TimeTable build() {
            return new TimeTable(stops, services);
        }
    }
}