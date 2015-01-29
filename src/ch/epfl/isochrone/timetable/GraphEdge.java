package ch.epfl.isochrone.timetable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Un arc de graphe.
 * Classe immuable visible uniquement dans son paquetage. Possede un constructeur.
 */
final class GraphEdge {

    private final Stop destination;
    private final int walkingTime;
    private final Integer[] packedTrips;

    /**
     * Encode un temps de depart et d'arrive en un seul entier (temps combine) contenant le temps de depart et la duree du trajet.
     * Les temps sont exprimes en secondes.
     * 
     * @param   departureTime
     *          Le temps de depart.
     * @param   arrivalTime
     *          Le temps d'arrivee.
     * @return  Un entier contenant le temps de depart et la duree du trajet.
     * @throws  IllegalArgumentException
     *          En cas d'heure de départ non comprise dans l'intervalle [0, 107999].
     * @throws  IllegalArgumentException
     *          En cas de duree de trajet non comprise dans l'intervalle [0, 9999].
     */
    public static int packTrip(int departureTime, int arrivalTime) {

        if (departureTime < 0 || departureTime > 107999)
            throw new IllegalArgumentException("l'heure de depart doit etre comprise dans [0, 107999] : "+departureTime);

        int diff = arrivalTime - departureTime;
        if (diff < 0 || diff > 9999)
            throw new IllegalArgumentException("la duree du trajet doit etre comprise dans [0, 9999]: "+diff);

        return departureTime << 14 | diff;
    }

    /**
     * Decode le temps de depart depuis un temps combine.
     * 
     * @param   packedTrip
     *          Le temps combine.
     * @return  Le temps de depart (s).
     */
    public static int unpackTripDepartureTime(int packedTrip) {
        return packedTrip >>> 14;
    }

    /**
     * Decode la duree de trajet depuis un temps combine.
     * 
     * @param   packedTrip
     *          Le temps combine.
     * @return  La duree du trajet (s).
     */
    public static int unpackTripDuration(int packedTrip) {
        return packedTrip & 0x3FFF; // 16383
    }

    /**
     * Decode le temps d'arrivee depuis un temps combine.
     * 
     * @param   packedTrip
     *          Le temps combine.
     * @return  Le temps d'arrivee (s).
     */
    public static int unpackTripArrivalTime(int packedTrip) {
        return unpackTripDepartureTime(packedTrip) + unpackTripDuration(packedTrip);
    }

    /**
     * Constructeur public d'un arc de graphe.
     * 
     * @param   destination
     *          La destination de l'arc.
     * @param   walkingTime
     *          Le temps de marche vers la destination. -1 s'il n'est pas possible d'y acceder a pied.
     * @param   packedTrips
     *          Les temps combines des liaisons de l'arc.
     * @throws  IllegalArgumentException
     *          En cas de temps de marche negatif, pas egal a -1.
     */
    public GraphEdge(Stop destination, int walkingTime, Set<Integer> packedTrips) {

        if (walkingTime < -1)
            throw new IllegalArgumentException("le temps de marche doit etre non-nul ou egal a -1 : "+walkingTime);

        this.destination = destination;
        this.walkingTime = walkingTime;
        this.packedTrips = packedTrips.toArray(new Integer[packedTrips.size()]);
        Arrays.sort(this.packedTrips); // tri pour permettre la recherche dichotomique
    }

    /**
     * Accesseur en lecture de la destination de l'arc.
     * 
     * @return  La destination de l'arc.
     */
    public Stop destination() {
        return destination;
    }

    /**
     * Retourne la premiere heure d'arrivee possible a la destination de l'arc en fonction de l'heure de depart.
     * 
     * @param   departureTime
     *          Le temps de depart.
     * @return  La premiere heure d'arrivee possible ou
     *          ou le temps de marche correspondant
     *          ou SecondsPastMidnight.INFINITE si aucun trajet n'est possible.
     */
    public int earliestArrivalTime(int departureTime) {
        int key = Arrays.binarySearch(packedTrips, departureTime << 14);
        if (key < 0) { // correcteur : si la cle n'est pas trouve (negatif), cle contient (-indice)-1 et ce bloc permet de retrouve la cle du prochain depart
            key *= -1;
            key -= 1;
        }        
        if (walkingTime < 0) { // s'il n'est pas possible d'effectuer le trajet a pied, on renvoie le prochain trajet ou l'infini si aucun trajet n'existe
            return (key < packedTrips.length) ? unpackTripArrivalTime(packedTrips[key]): SecondsPastMidnight.INFINITE;
        } else { // s'il est possible d'effectuer le trajet a pied, on renvoie le minimum entre le temps de trajet et celui a pied ou celui a pied si aucun trajet n'existe
            int walkTime = Math.min(departureTime + walkingTime, SecondsPastMidnight.INFINITE);
            return (key < packedTrips.length) ? Math.min(unpackTripArrivalTime(packedTrips[key]), walkTime): walkTime;
        }
    }

    /**
     * Batisseur d'arc de graphe.
     */
    public final static class Builder {

        private final Stop destination;
        private final Set<Integer> packedTrips;
        private int walkingTime;
        /**
         * Constructeur public d'un batisseur d'arc de graphe.
         * 
         * @param   destination
         *          La destination de l'arc.
         */
        public Builder(Stop destination) {
            this.destination = destination;
            this.walkingTime = -1;
            this.packedTrips = new HashSet<>();
        }

        /**
         * Ajoute un temps de marche a l'arc en construction.
         * Permet les appels chaines.
         * 
         * @param   newWalkingTime
         *          Le temps de marche du trajet de l'arc en construction.
         * @return  Le batisseur.
         * @throws  IllegalArgumentException
         *          En cas de temps de marche negatif, pas egal a -1.
         */
        public Builder setWalkingTime(int newWalkingTime) {

            if (newWalkingTime < -1)
                throw new IllegalArgumentException("le temps de marche doit etre non-nul ou egal a -1 : "+newWalkingTime);

            walkingTime = newWalkingTime;
            return this;
        }

        /**
         * Ajoute un trajet a l'arc en construction.
         * Permet les appels chaines.
         * 
         * @param   departureTime
         *          Le temps de depart de l'arc en construction.
         * @param   arrivalTime
         *          Le temps d'arrive de l'arc en construction.
         * @return  Le batisseur.
         */
        public Builder addTrip(int departureTime, int arrivalTime) {
            packedTrips.add(packTrip(departureTime, arrivalTime)); // lance deja IllegalArgumentException si le temps n'est pas valide 
            return this;
        }

        /**
         * Construit l'arc de graphe a partir du batisseur.
         * 
         * @return  L'arc de graphe.
         */
        public GraphEdge build() {
            return new GraphEdge(destination, walkingTime, packedTrips);
        }
    }
}
