package ch.epfl.isochrone.timetable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Un arbre du chemin le plus rapide.
 * Classe immuable. Possede un constructeur.
 */
public final class FastestPathTree {

    private final Stop startingStop;
    private final Map<Stop, Integer> arrivalTime;
    private final Map<Stop, Stop> predecessor;

    /**
     * Constructeur public d'un arbre du chemin le plus rapide. La table des arrives doit correspondre avec celle des predecesseurs (sans compter l'arret de depart).
     * 
     * @param   startingStop
     *          L'arret de depart (racine).
     * @param   arrivalTime
     *          Les temps d'arrivee aux arrets.
     * @param   predecessor
     *          Les predecesseurs des arrets.
     * @throws  IllegalArgumentException
     *          En cas de pair (temps d'arrivee et arret precedent) ne correspondant pas.
     */
    public FastestPathTree(Stop startingStop, Map<Stop, Integer> arrivalTime, Map<Stop, Stop> predecessor) {

        for (Stop s : arrivalTime.keySet()) {
            if (!predecessor.containsKey(s) && !s.equals(startingStop))
                throw new IllegalArgumentException("chaque entree dans la table des temps d'arrivee doit correspondre a une entre dans la table des predecesseurs");
        }
        assert arrivalTime.size() == predecessor.size() + 1;

        this.startingStop = startingStop;
        this.arrivalTime = Collections.unmodifiableMap(arrivalTime);
        this.predecessor = Collections.unmodifiableMap(predecessor);
    }

    /**
     * Accesseur en lecture de l'arret de depart.
     * 
     * @return  L'arret de depart.
     */
    public Stop startingStop() {
        return startingStop;
    }

    /**
     * Accesseur en lecture du temps de depart.
     * 
     * @return  Le temps de depart de l'arret de depart (racine).
     */
    public int startingTime() {
        return arrivalTime.get(startingStop);
    }

    /**
     * Retourne l'ensemble des arrets de l'arbre.
     * 
     * @return  Les arrets de l'arbre.
     */
    public Set<Stop> stops() {
        return arrivalTime.keySet();
    }

    /**
     * Retourne le temps d'arrivee a un arret donne.
     * 
     * @param   stop
     *          L'arret.
     * @return  Le temps d'arrivee a l'arret ou
     *          SecondsPastMidnight.INFINITE si l'arret n'est pas dans l'arbre.
     */
    public int arrivalTime(Stop stop) {
        return arrivalTime.containsKey(stop) ? arrivalTime.get(stop): SecondsPastMidnight.INFINITE;
    }

    /**
     * Retourne une liste des arrets decrivant un chemin le plus rapide depuis de l'arret de depart (racine) jusqu'a l'arret donne.
     * 
     * @param   stop
     *          L'arret d'arrivee.
     * @return  La liste des arrets avec en premier l'arret de depart, puis tous les arrets jusqu'a l'arret donne.
     * @throws  IllegalArgumentException
     *          En cas d'arret d'arrivee ne faisant pas partie de l'arbre du chemin le plus rapide.
     */
    public List<Stop> pathTo(Stop stop) {

        if (!arrivalTime.containsKey(stop))
            throw new IllegalArgumentException("l'arret d'arrivee doit faire partie de l'arbre : "+stop);

        final LinkedList<Stop> temp = new LinkedList<>();
        Stop currentStop = stop;

        while (currentStop != null) {
            temp.addFirst(currentStop);
            currentStop = predecessor.get(currentStop);
        }

        return Collections.unmodifiableList(temp);
    }

    /**
     * Batisseur de l'arbre du chemin le plus rapide.
     */
    public final static class Builder {

        private final Stop startingStop;
        private final int startingTime;
        private final Map<Stop, Integer> arrivalTime;
        private final Map<Stop, Stop> predecessor;

        /**
         * Constructeur public d'un batisseur d'arbre du chemin le plus rapide et ajoute directement l'arret de depart et le temps de depart.
         * 
         * @param   startingStop
         *          L'arret de depart (racine).
         * @param   startingTime
         *          Le temps de depart.
         * @throws  IllegalArgumentException
         *          En cas de temps de depart negatif.
         */
        public Builder(Stop startingStop, int startingTime) {
            if (startingTime < 0)
                throw new IllegalArgumentException("l'heure de depart ne doit pas etre negative : "+startingTime);

            this.startingStop = startingStop;
            this.startingTime = startingTime;
            this.arrivalTime = new HashMap<>();
            this.predecessor = new HashMap<>();
            this.arrivalTime.put(startingStop, startingTime);
        }

        /**
         * Ajoute ou remplace un arret existant avec le temps de depart et l'arret precedent correspondant.
         * Permet les appels chaines.
         * 
         * @param   stop
         *          L'arret ajoute ou remplace.
         * @param   time
         *          Le temps de depart a l'arret.
         * @param   predecessor
         *          L'arret precedent (noeud).
         * @return  Le batisseur.
         * @throws  IllegalArgumentException
         *          En cas de temps d'arrivee anterieure au temps de depart de la racine.
         */
        public Builder setArrivalTime(Stop stop, int time, Stop predecessor) {
            if (time < startingTime)
                throw new IllegalArgumentException("l'heure d'arrive doit etre posterieure a celle de depart : "+time+" et "+startingTime);

            arrivalTime.put(stop, time);
            this.predecessor.put(stop, predecessor);
            return this;
        }

        /**
         * Retourne le temps d'arrivee a un arret donne.
         * 
         * @param   stop
         *          L'arret
         * @return  Le temps d'arrivee a l'arret ou
         *          SecondsPastMidnight.INFINITE si l'arret n'est pas dans l'arbre.
         */
        public int arrivalTime(Stop stop) {
            return arrivalTime.containsKey(stop) ? arrivalTime.get(stop): SecondsPastMidnight.INFINITE;
        }

        /**
         * Construit un arbre du chemin le plus rapide a partir du batisseur.
         * 
         * @return  L'arbre du chemin le plus rapide
         */
        public FastestPathTree build() {
            return new FastestPathTree(startingStop, arrivalTime, predecessor);
        }
    }
}
